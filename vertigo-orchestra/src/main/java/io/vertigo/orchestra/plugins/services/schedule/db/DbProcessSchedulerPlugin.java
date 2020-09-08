/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.orchestra.plugins.services.schedule.db;

import java.text.ParseException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.param.ParamValue;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.orchestra.dao.execution.OProcessExecutionDAO;
import io.vertigo.orchestra.dao.planification.OProcessPlanificationDAO;
import io.vertigo.orchestra.dao.planification.PlanificationPAO;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.domain.definition.OProcess;
import io.vertigo.orchestra.domain.planification.OProcessPlanification;
import io.vertigo.orchestra.impl.node.ONodeManager;
import io.vertigo.orchestra.impl.services.schedule.CronExpression;
import io.vertigo.orchestra.impl.services.schedule.ProcessSchedulerPlugin;
import io.vertigo.orchestra.plugins.services.MapCodec;
import io.vertigo.orchestra.services.execution.ProcessExecutor;
import io.vertigo.orchestra.services.schedule.SchedulerState;

/**
 * Plugin de gestion de la planification.
 *
 * @author mlaroche.
 * @version $Id$
 */
public class DbProcessSchedulerPlugin implements ProcessSchedulerPlugin, Activeable, SimpleDefinitionProvider {

	private static final Logger LOGGER = LogManager.getLogger(DbProcessSchedulerPlugin.class);

	@Inject
	private OProcessPlanificationDAO processPlanificationDAO;
	@Inject
	private PlanificationPAO planificationPAO;
	@Inject
	private OProcessExecutionDAO processExecutionDAO;
	@Inject
	private EntityStoreManager entityStoreManager;

	private final String nodeName;
	private Long nodId;
	private final Integer planningPeriodSeconds;
	private final Integer forecastDurationSeconds;
	private final ONodeManager nodeManager;
	private ProcessExecutor myProcessExecutor;

	private final VTransactionManager transactionManager;
	private final OrchestraDefinitionManager definitionManager;

	private final MapCodec mapCodec = new MapCodec();

	/**
	 * Constructeur.
	 * @param nodeManager le gestionnaire de noeud
	 * @param transactionManager vertigo transaction manager
	 * @param definitionManager orchestra definitions manager
	 * @param nodeName le nom du noeud
	 * @param planningPeriodSecondsOpt le timer de planfication (30 seconds by default)
	 * @param forecastDurationSecondsOpt la durée de prévision des planifications (3600 seconds by default)
	 */
	@Inject
	public DbProcessSchedulerPlugin(
			final ONodeManager nodeManager,
			final VTransactionManager transactionManager,
			final OrchestraDefinitionManager definitionManager,
			@ParamValue("nodeName") final String nodeName,
			@ParamValue("planningPeriodSeconds") final Optional<Integer> planningPeriodSecondsOpt,
			@ParamValue("forecastDurationSeconds") final Optional<Integer> forecastDurationSecondsOpt) {
		Assertion.check()
				.isNotNull(nodeManager)
				.isNotNull(transactionManager)
				.isNotNull(definitionManager)
				.isNotBlank(nodeName)
				.isNotNull(planningPeriodSecondsOpt)
				.isNotNull(forecastDurationSecondsOpt);
		//-----
		this.nodeManager = nodeManager;
		this.transactionManager = transactionManager;
		this.definitionManager = definitionManager;
		planningPeriodSeconds = planningPeriodSecondsOpt.orElse(30);
		forecastDurationSeconds = forecastDurationSecondsOpt.orElse(3600);
		this.nodeName = nodeName;
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new DaemonDefinition("DmnODbProcessSchedulerDaemon", () -> this::scheduleAndInit, planningPeriodSeconds));
	}

	private void scheduleAndInit() {
		ThreadContext.put("module", "orchestra");
		try {
			plannRecurrentProcesses();
			initToDo(myProcessExecutor);
		} catch (final Throwable t) {
			LOGGER.error("Exception planning recurrent processes", t);
			// if it's an interrupted we rethrow it because we are asked to stop by the jvm
			if (t instanceof InterruptedException) {
				throw t;
			}
		} finally {
			ThreadContext.remove("module");
		}

	}

	@Override
	public void start() {
		// We register the node
		nodId = nodeManager.registerNode(nodeName);
		// We clean the planification
		cleanPastPlanification();
	}

	@Override
	public void setProcessExecutor(final ProcessExecutor processExecutor) {
		Assertion.check().isNotNull(processExecutor);
		//---
		myProcessExecutor = processExecutor;
	}

	@Override
	public void stop() {
		// rien
	}

	@Override
	public ProcessType getHandledProcessType() {
		return ProcessType.SUPERVISED;
	}
	//--------------------------------------------------------------------------------------------------
	//--- Package
	//--------------------------------------------------------------------------------------------------

	private void doScheduleWithCron(final ProcessDefinition processDefinition) {
		final Optional<Instant> nextPlanification = findNextPlanificationTime(processDefinition);
		if (nextPlanification.isPresent()) {
			scheduleAt(processDefinition, nextPlanification.get(), processDefinition.getTriggeringStrategy().getInitialParams());
		}
	}

	@Override
	public void scheduleAt(final ProcessDefinition processDefinition, final Instant planifiedTime, final Map<String, String> initialParams) {
		Assertion.check()
				.isNotNull(processDefinition)
				.isNotNull(planifiedTime)
				.isNotNull(initialParams);
		//---
		if (transactionManager.hasCurrentTransaction()) {
			doScheduleAt(processDefinition, planifiedTime, initialParams);
		} else {
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				doScheduleAt(processDefinition, planifiedTime, initialParams);
				transaction.commit();
			}
		}
	}

	//--------------------------------------------------------------------------------------------------
	//--- Private
	//--------------------------------------------------------------------------------------------------

	private void doScheduleAt(final ProcessDefinition processDefinition, final Instant planifiedTime, final Map<String, String> initialParams) {
		Assertion.check().isNotNull(processDefinition);
		// ---
		final OProcessPlanification processPlanification = new OProcessPlanification();
		processPlanification.setProId(processDefinition.getId());
		processPlanification.setExpectedTime(planifiedTime);
		changeState(processPlanification, SchedulerState.WAITING);
		processPlanification.setInitialParams(mapCodec.encode(initialParams));
		processPlanificationDAO.save(processPlanification);

	}

	private void initToDo(final ProcessExecutor processExecutor) {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			initNewProcessesToLaunch(processExecutor);
			transaction.commit();
		}
	}

	private void initNewProcessesToLaunch(final ProcessExecutor processExecutor) {
		for (final OProcessPlanification processPlanification : getPlanificationsToTrigger()) {
			processPlanification.processus().load();
			final ProcessDefinition processDefinition = definitionManager.getProcessDefinition(processPlanification.processus().get().getName());
			lockProcess(processDefinition);

			if (canExecute(processDefinition)) {
				triggerPlanification(processPlanification);
				processExecutor.execute(processDefinition, Optional.ofNullable(processPlanification.getInitialParams()));
			} else {
				misfirePlanification(processPlanification);
			}
		}
	}

	private void lockProcess(final ProcessDefinition processDefinition) {
		final UID<OProcess> processURI = UID.of(OProcess.class, processDefinition.getId());
		entityStoreManager.readOneForUpdate(processURI);
	}

	private void plannRecurrentProcesses() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final ProcessDefinition processDefinition : getAllScheduledProcesses()) {
				doScheduleWithCron(processDefinition);
			}
			transaction.commit();
		}

	}

	private DtList<OProcessPlanification> getPlanificationsToTrigger() {
		final GregorianCalendar lowerLimit = new GregorianCalendar(Locale.FRANCE);
		lowerLimit.add(Calendar.MILLISECOND, -planningPeriodSeconds * 1000 * 5 / 4); //Just to be sure that nothing will be lost

		final GregorianCalendar upperLimit = new GregorianCalendar(Locale.FRANCE);

		planificationPAO.reserveProcessToExecute(lowerLimit.toInstant(), upperLimit.toInstant(), nodId);
		return processPlanificationDAO.getProcessToExecute(nodId);
	}

	private boolean canExecute(final ProcessDefinition processDefinition) {
		// We check if process allow multiExecutions
		if (!processDefinition.getTriggeringStrategy().isMultiExecution()) {
			// TODO we are in the case of a process that allows a single execution at the time
			//      -> the previous was too long so we kill it (mark has aborted) and keep the new one
			return processExecutionDAO.getActiveProcessExecutionByProId(processDefinition.getId()).isEmpty();
		}
		return true;
	}

	private Optional<OProcessPlanification> getLastPlanificationsByProcess(final Long proId) {
		Assertion.check().isNotNull(proId);
		// ---
		return processPlanificationDAO.getLastPlanificationByProId(proId);
	}

	private Optional<Instant> findNextPlanificationTime(final ProcessDefinition processDefinition) {
		final Optional<OProcessPlanification> lastPlanificationOption = getLastPlanificationsByProcess(processDefinition.getId());

		try {
			final CronExpression cronExpression = new CronExpression(processDefinition.getTriggeringStrategy().getCronExpression().get());

			if (lastPlanificationOption.isEmpty()) {
				final Instant compatibleNow = Instant.now().plusMillis(planningPeriodSeconds / 2L * 1000);// Normalement ca doit être bon quelque soit la synchronisation entre les deux timers (même fréquence)
				return Optional.of(cronExpression.getNextValidTimeAfter(compatibleNow));
			}
			final OProcessPlanification lastPlanification = lastPlanificationOption.get();
			final Instant nextPotentialPlainification = cronExpression.getNextValidTimeAfter(lastPlanification.getExpectedTime());
			if (nextPotentialPlainification.isBefore(Instant.now().plusSeconds(forecastDurationSeconds))) {
				return Optional.of(nextPotentialPlainification);
			}
		} catch (final ParseException e) {
			throw WrappedException.wrap(e, "Process' cron expression is not valid, process cannot be planned");
		}

		return Optional.<Instant> empty();

	}

	private List<ProcessDefinition> getAllScheduledProcesses() {
		return definitionManager.getAllProcessDefinitionsByType(getHandledProcessType()).stream()
				.filter(ProcessDefinition::isActive)// We only want actives
				.filter(processDefinition -> processDefinition.getTriggeringStrategy().getCronExpression().isPresent())// We only want the processes to schedule
				.collect(Collectors.toList());
	}

	private static void changeState(final OProcessPlanification processPlanification, final SchedulerState planificationState) {
		Assertion.check()
				.isNotNull(processPlanification)
				.isNotNull(planificationState);
		// ---
		processPlanification.setSstCd(planificationState.name());
	}

	private void triggerPlanification(final OProcessPlanification processPlanification) {
		changeState(processPlanification, SchedulerState.TRIGGERED);
		processPlanificationDAO.save(processPlanification);
	}

	private void misfirePlanification(final OProcessPlanification processPlanification) {
		changeState(processPlanification, SchedulerState.MISFIRED);
		processPlanificationDAO.save(processPlanification);
	}

	// clean Planification on startup

	private void cleanPastPlanification() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			doCleanPastPlanification();
			transaction.commit();
		}
	}

	private void doCleanPastPlanification() {
		final Instant now = Instant.now();
		planificationPAO.cleanPlanificationsOnBoot(now);
		// ---
		for (final OProcessPlanification planification : processPlanificationDAO.getAllLastPastPlanifications(now)) {
			// We check the process policy of validity
			planification.processus().load();
			final OProcess process = planification.processus().get();
			final long ageOfPlanification = (now.toEpochMilli() - planification.getExpectedTime().toEpochMilli()) / (60 * 1000L);// in seconds
			if (ageOfPlanification < process.getRescuePeriod()) {
				changeState(planification, SchedulerState.RESCUED);
			} else {
				changeState(planification, SchedulerState.MISFIRED);
			}
			processPlanificationDAO.save(planification);
		}

	}

}
