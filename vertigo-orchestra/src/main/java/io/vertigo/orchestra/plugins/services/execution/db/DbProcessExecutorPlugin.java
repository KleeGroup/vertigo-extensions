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
package io.vertigo.orchestra.plugins.services.execution.db;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.process.AProcess;
import io.vertigo.core.analytics.process.AProcessBuilder;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.orchestra.dao.definition.OActivityDAO;
import io.vertigo.orchestra.dao.execution.ExecutionPAO;
import io.vertigo.orchestra.dao.execution.OActivityExecutionDAO;
import io.vertigo.orchestra.dao.execution.OActivityLogDAO;
import io.vertigo.orchestra.dao.execution.OActivityWorkspaceDAO;
import io.vertigo.orchestra.dao.execution.OProcessExecutionDAO;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.domain.DtDefinitions.OActivityExecutionFields;
import io.vertigo.orchestra.domain.definition.OActivity;
import io.vertigo.orchestra.domain.execution.OActivityExecution;
import io.vertigo.orchestra.domain.execution.OActivityLog;
import io.vertigo.orchestra.domain.execution.OActivityWorkspace;
import io.vertigo.orchestra.domain.execution.OProcessExecution;
import io.vertigo.orchestra.impl.node.ONodeManager;
import io.vertigo.orchestra.impl.services.execution.AbstractActivityEngine;
import io.vertigo.orchestra.impl.services.execution.ActivityLogger;
import io.vertigo.orchestra.impl.services.execution.ProcessExecutorPlugin;
import io.vertigo.orchestra.plugins.services.MapCodec;
import io.vertigo.orchestra.services.execution.ActivityEngine;
import io.vertigo.orchestra.services.execution.ActivityExecutionWorkspace;
import io.vertigo.orchestra.services.execution.ExecutionState;

/**
 * Executeur des processus orchestra sous la forme d'une séquence linéaire d'activités.
 *
 * @author mlaroche.
 * @version $Id$
 */
public final class DbProcessExecutorPlugin implements ProcessExecutorPlugin, Activeable, SimpleDefinitionProvider {

	private static final Logger LOGGER = LogManager.getLogger(DbProcessExecutorPlugin.class);

	@Inject
	private OProcessExecutionDAO processExecutionDAO;
	@Inject
	private OActivityExecutionDAO activityExecutionDAO;
	@Inject
	private OActivityWorkspaceDAO activityWorkspaceDAO;
	@Inject
	private ExecutionPAO executionPAO;
	@Inject
	private OActivityLogDAO activityLogDAO;
	@Inject
	private OActivityDAO activityDAO;
	@Inject
	private EntityStoreManager entityStoreManager;
	@Inject
	private AnalyticsManager analyticsManager;

	private final int workersCount;
	private final String nodeName;
	private Long nodId = null;
	private final ExecutorService workers;
	private final int executionPeriodSeconds;

	private final ONodeManager nodeManager;
	private final VTransactionManager transactionManager;

	private final MapCodec mapCodec = new MapCodec();

	/**
	 * Constructeur.
	 * @param nodeManager le gestionnaire de noeud
	 * @param transactionManager le gestionnaire de transaction
	 * @param workersCountOpt le nombre de worker du noeud (10 by default)
	 * @param executionPeriodSecondsOpt le timer du long-polling (30 seconds by default)
	 */
	@Inject
	public DbProcessExecutorPlugin(
			final ONodeManager nodeManager,
			final VTransactionManager transactionManager,
			@ParamValue("nodeName") final String nodeName,
			@ParamValue("workersCount") final Optional<Integer> workersCountOpt,
			@ParamValue("executionPeriodSeconds") final Optional<Integer> executionPeriodSecondsOpt) {
		Assertion.check()
				.isNotNull(nodeManager)
				.isNotNull(transactionManager);
		// ---
		this.nodeManager = nodeManager;
		this.transactionManager = transactionManager;
		this.nodeName = nodeName;
		workersCount = workersCountOpt.orElse(10);
		// ---
		Assertion.check().isTrue(workersCount >= 1, "We need at least 1 worker");
		// ---
		executionPeriodSeconds = executionPeriodSecondsOpt.orElse(30);
		// ---
		workers = Executors.newFixedThreadPool(workersCount);
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new DaemonDefinition("DmnODbProcessExecutorDaemon", () -> this::executeProcesses, executionPeriodSeconds));
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		handleDeadNodeProcesses();
		nodId = nodeManager.registerNode(nodeName);
		handleNodeDeadProcesses(nodId);
	}

	private void executeProcesses() {
		ThreadContext.put("module", "orchestra");
		try {
			Assertion.check().isNotNull(nodId, "Node not already registered");
			executeToDo();
			nodeManager.updateHeartbeat(nodId);
			handleDeadNodeProcesses();
		} catch (final Throwable t) {
			LOGGER.error("Exception launching activities to executes", t);
			// if it's an interrupted we rethrow it because we are asked to stop by the jvm
			if (t instanceof InterruptedException) {
				throw t;
			}
		} finally {
			ThreadContext.remove("module");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		workers.shutdownNow();
	}

	/** {@inheritDoc} */
	@Override
	public ProcessType getHandledProcessType() {
		return ProcessType.SUPERVISED;
	}

	//--------------------------------------------------------------------------------------------------
	//--- Package
	//--------------------------------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public void execute(final ProcessDefinition processDefinition, final Optional<String> initialParams) {
		Assertion.check().isNotNull(processDefinition);
		// ---
		// We need to be as short as possible for the commit
		if (transactionManager.hasCurrentTransaction()) {
			doExecute(processDefinition, initialParams);
		} else {
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				doExecute(processDefinition, initialParams);
				transaction.commit();
			}
		}
	}

	private void doExecute(final ProcessDefinition processDefinition, final Optional<String> initialParams) {
		final OProcessExecution processExecution = initProcessExecution(processDefinition);
		initFirstActivityExecution(processExecution, initialParams);

	}

	/** {@inheritDoc} */
	@Override
	public void endPendingActivityExecution(final Long activityExecutionId, final String token, final ExecutionState executionState, final Optional<String> errorMessageOpt) {
		Assertion.check()
				.isNotNull(activityExecutionId)
				.isNotNull(token);
		// ---
		OActivityExecution activityExecution;
		ActivityExecutionWorkspace workspace;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			activityExecution = activityExecutionDAO.getActivityExecutionByToken(activityExecutionId, token);
			Assertion.check().isNotNull(activityExecution, "Activity token and id are not compatible");
			workspace = getWorkspaceForActivityExecution(activityExecution.getAceId(), false);
			transaction.commit();
		}
		// ---
		Assertion.check()
				.isTrue(ExecutionState.PENDING.name().equals(activityExecution.getEstCd()), "Only pending executions can be ended remotly")
				.isTrue(workspace != null, "Workspace for activityExecution not found");

		// We execute the postTreatment of the pending activity when it's released
		// ---
		final ActivityEngine activityEngine = InjectorUtil.newInstance(
				ClassUtil.classForName(activityExecution.getEngine(), ActivityEngine.class));

		try {
			switch (executionState) {
				case DONE:
					workspace = activityEngine.successfulPostTreatment(workspace);
					break;
				case ERROR:
					workspace = activityEngine.errorPostTreatment(workspace, new RuntimeException(errorMessageOpt.orElse("ThirdPartyException")));
					break;
				case PENDING:
				case RUNNING:
				case SUBMITTED:
				case WAITING:
				case ABORTED:
				default:
					throw new UnsupportedOperationException();
			}

		} catch (final Exception e) {
			LOGGER.info("Unknow error ending a pending activity", e);
			endActivityExecution(activityExecution, ExecutionState.ERROR);

		} finally {
			handleOtherServices(activityEngine, activityExecution, workspace);
		}

		// we continue with the standard workflow for ending executions
		endActivityExecution(activityExecution, executionState);

	}

	/** {@inheritDoc} */
	@Override
	public void setActivityExecutionPending(final Long activityExecutionId, final ActivityExecutionWorkspace workspace) {
		// We need to be as short as possible for the commit
		if (transactionManager.hasCurrentTransaction()) {
			try (final VTransactionWritable transaction = transactionManager.createAutonomousTransaction()) {
				doSetActivityExecutionPending(activityExecutionId, workspace);
				transaction.commit();
			}
		} else {
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				doSetActivityExecutionPending(activityExecutionId, workspace);
				transaction.commit();
			}
		}

	}

	private void doSetActivityExecutionPending(final Long activityExecutionId, final ActivityExecutionWorkspace workspace) {
		Assertion.check().isNotNull(activityExecutionId);
		// ---
		final OActivityExecution activityExecution = activityExecutionDAO.get(activityExecutionId);
		endActivityExecution(activityExecution, ExecutionState.PENDING);
		saveActivityExecutionWorkspace(activityExecutionId, workspace, false);
	}
	//--------------------------------------------------------------------------------------------------
	//--- Private
	//--------------------------------------------------------------------------------------------------

	private void executeToDo() {
		final DtList<OActivityExecution> activitiesToLaunch;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			activitiesToLaunch = getActivitiesToLaunch();
			transaction.commit();
		}
		for (final OActivityExecution activityExecution : activitiesToLaunch) { //We submit only the process we can handle, no queue
			ActivityExecutionWorkspace workspace;
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				workspace = getWorkspaceForActivityExecution(activityExecution.getAceId(), true);
				doChangeExecutionState(activityExecution, ExecutionState.SUBMITTED);
				transaction.commit();
			}
			workers.submit(() -> doRunActivity(activityExecution, workspace));
		}

	}

	private void doRunActivity(final OActivityExecution activityExecution, final ActivityExecutionWorkspace workspace) {
		clearAllThreadLocals();
		ThreadContext.put("module", "orchestra");
		ActivityExecutionWorkspace result = null;
		Throwable throwable = null;
		try {
			result = execute(activityExecution, workspace);
		} catch (final WrappedException e) {
			LOGGER.error("Error executing activity", e);
			throwable = e.getCause();
		} catch (final Throwable t) {
			LOGGER.error("Error executing activity", t);
			if (t instanceof InterruptedException) {
				throw t;
			}
			throwable = t;
		} finally {
			putResult(activityExecution, result, throwable);
			ThreadContext.remove("module");
		}
	}

	private static void clearAllThreadLocals() {
		try {
			final Field threadLocals = Thread.class.getDeclaredField("threadLocals");
			threadLocals.setAccessible(true);
			threadLocals.set(Thread.currentThread(), null);
		} catch (final Exception e) {
			throw new AssertionError(e);
		}
	}

	private ActivityExecutionWorkspace execute(final OActivityExecution activityExecution, final ActivityExecutionWorkspace workspace) {
		ActivityExecutionWorkspace resultWorkspace = workspace;

		try {
			// We set the beginning time of the activity
			activityExecution.setBeginTime(Instant.now());
			changeExecutionState(activityExecution, ExecutionState.RUNNING);
			// ---
			final ActivityEngine activityEngine = InjectorUtil.newInstance(
					ClassUtil.classForName(activityExecution.getEngine(), ActivityEngine.class));

			try {

				// If the engine extends the abstractEngine we can provide the services associated (LOGGING,...) so we log the workspace
				if (activityEngine instanceof AbstractActivityEngine) {
					final String workspaceInLog = new StringBuilder("Workspace in :").append(mapCodec.encode(workspace.asMap())).toString();
					((AbstractActivityEngine) activityEngine).getLogger().info(workspaceInLog);
				}
				// We try the execution and we keep the result
				resultWorkspace = activityEngine.execute(workspace);
				Assertion.check()
						.isNotNull(resultWorkspace)
						.isNotNull(resultWorkspace.getValue("status"), "Le status est obligatoire dans le résultat");
				// if pending we delegated the treatment to a third party so we are not sure that we are successful
				if (!resultWorkspace.isPending()) {
					// we call the posttreament
					resultWorkspace = activityEngine.successfulPostTreatment(resultWorkspace);
				}

			} catch (final Exception e) {
				// In case of failure we keep the current workspace
				resultWorkspace.setFailure();
				LOGGER.error("Erreur de l'activité : " + activityExecution.getEngine(), e);
				// we call the posttreament
				resultWorkspace = activityEngine.errorPostTreatment(resultWorkspace, e);

			} finally {
				handleOtherServices(activityEngine, activityExecution, resultWorkspace);
			}

		} catch (final Exception e) {
			// Informative log
			resultWorkspace.setFailure();
			LOGGER.error("Erreur de l'activité : " + activityExecution.getEngine(), e);
		}

		return resultWorkspace;
	}

	private void putResult(final OActivityExecution activityExecution, final ActivityExecutionWorkspace workspaceOut, final Throwable error) {
		if (error != null) {
			// We log the error and we continue the timer
			LOGGER.info("Error in activity " + activityExecution.getActId() + " execution", error);
			endActivityExecution(activityExecution, ExecutionState.ERROR);
		} else {
			Assertion.check()
					.isNotNull(workspaceOut)
					.isNotNull(workspaceOut.getValue("status"), "Le status est obligatoire dans le résultat");
			//---
			if (workspaceOut.isSuccess()) {
				endActivityExecution(activityExecution, ExecutionState.DONE);
			} else if (workspaceOut.isFinished()) {
				// If finished we tag the whole process as DONE and dont launch next activities
				finishProcessExecution(activityExecution);
			} else if (workspaceOut.isPending()) {
				// We do nothing because we already delegated the change of status in the AbstractActivityEngine
			} else {
				endActivityExecution(activityExecution, ExecutionState.ERROR);
			}
		}

	}

	private void changeExecutionState(final OActivityExecution activityExecution, final ExecutionState executionState) {
		if (transactionManager.hasCurrentTransaction()) {
			doChangeExecutionState(activityExecution, executionState);
		} else {
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				doChangeExecutionState(activityExecution, executionState);
				transaction.commit();
			}
		}
	}

	private OProcessExecution initProcessExecution(final ProcessDefinition processDefinition) {
		Assertion.check().isNotNull(processDefinition);
		// ---
		final OProcessExecution newProcessExecution = new OProcessExecution();
		newProcessExecution.setProId(processDefinition.getId());
		newProcessExecution.setBeginTime(Instant.now());
		changeProcessExecutionState(newProcessExecution, ExecutionState.RUNNING);
		processExecutionDAO.save(newProcessExecution);

		return newProcessExecution;
	}

	private DtList<OActivityExecution> getActivitiesToLaunch() {
		final int maxNumber = getUnusedWorkersCount();
		// ---
		executionPAO.reserveActivitiesToLaunch(nodId, maxNumber);
		return activityExecutionDAO.getActivitiesToLaunch(nodId);
	}

	private void initFirstActivityExecution(final OProcessExecution processExecution, final Optional<String> initialParams) {
		Assertion.check()
				.isNotNull(processExecution.getProId())
				.isNotNull(processExecution.getPreId());
		// ---
		final OActivity firstActivity = activityDAO.getFirstActivityByProcess(processExecution.getProId());
		final OActivityExecution firstActivityExecution = initActivityExecutionWithActivity(firstActivity, processExecution.getPreId());
		activityExecutionDAO.save(firstActivityExecution);

		processExecution.process().load();
		final ActivityExecutionWorkspace initialWorkspace = new ActivityExecutionWorkspace(mapCodec.decode(processExecution
				.process().get()
				.getInitialParams()));
		if (initialParams.isPresent()) {
			// If Plannification specifies initialParams we take them in addition
			initialWorkspace.addExternalParams(mapCodec.decode(initialParams.get()));
		}
		// We set in the workspace essentials params
		initialWorkspace.setProcessName(processExecution.process().get().getName());
		initialWorkspace.setProcessExecutionId(processExecution.getPreId());
		initialWorkspace.setActivityExecutionId(firstActivityExecution.getAceId());
		initialWorkspace.setToken(firstActivityExecution.getToken());
		// ---
		saveActivityExecutionWorkspace(firstActivityExecution.getAceId(), initialWorkspace, true);

	}

	private static OActivityExecution initActivityExecutionWithActivity(final OActivity activity, final Long preId) {
		Assertion.check().isNotNull(preId);
		// ---
		final OActivityExecution activityExecution = new OActivityExecution();

		activityExecution.setPreId(preId);
		activityExecution.setActId(activity.getActId());
		activityExecution.setCreationTime(Instant.now());
		activityExecution.setEngine(activity.getEngine());
		changeActivityExecutionState(activityExecution, ExecutionState.WAITING);
		activityExecution.setToken(ActivityTokenGenerator.getToken());

		return activityExecution;

	}

	private void reserveActivityExecution(final OActivityExecution activityExecution) {
		activityExecution.setEstCd(ExecutionState.SUBMITTED.name());
		activityExecution.setNodId(nodId);
	}

	private void endActivityExecutionAndInitNext(final OActivityExecution activityExecution) {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			endActivity(activityExecution);

			final Optional<OActivity> nextActivity = activityDAO.getNextActivityByActId(activityExecution.getActId());
			if (nextActivity.isPresent()) {
				final OActivityExecution nextActivityExecution;
				final ActivityExecutionWorkspace nextWorkspace;
				nextActivityExecution = initActivityExecutionWithActivity(nextActivity.get(), activityExecution.getPreId());
				// We keep the previous worker (Not the same but the slot) for the next Activity Execution
				reserveActivityExecution(nextActivityExecution);
				activityExecutionDAO.save(nextActivityExecution);

				// We keep the old workspace for the nextTask
				final ActivityExecutionWorkspace previousWorkspace = getWorkspaceForActivityExecution(activityExecution.getAceId(), false);
				// We remove the status and update the activityExecutionId and token
				previousWorkspace.resetStatus();
				previousWorkspace.resetAttachment();
				previousWorkspace.setActivityExecutionId(nextActivityExecution.getAceId());
				previousWorkspace.setToken(nextActivityExecution.getToken());
				// ---
				saveActivityExecutionWorkspace(nextActivityExecution.getAceId(), previousWorkspace, true);
				nextActivityExecution.setBeginTime(Instant.now());
				nextWorkspace = previousWorkspace;
				//we close the transaction now
				transaction.addAfterCompletion(
						succeeded -> {
							if (succeeded) {
								workers.submit(() -> doRunActivity(nextActivityExecution, nextWorkspace));
							}
						});

			} else {
				endProcessExecution(activityExecution.getPreId(), ExecutionState.DONE);
			}
			transaction.commit();
		}
	}

	private void endActivityExecution(final OActivityExecution activityExecution, final ExecutionState executionState) {
		Assertion.check()
				.isNotNull(activityExecution)
				.isNotNull(executionState);
		// ---

		switch (executionState) {
			case DONE:
				endActivityExecutionAndInitNext(activityExecution);
				break;
			case ERROR:
				activityExecution.setEndTime(Instant.now());
				changeExecutionState(activityExecution, ExecutionState.ERROR);
				break;
			case PENDING:
				changeExecutionState(activityExecution, ExecutionState.PENDING);
				break;
			case RUNNING:
			case SUBMITTED:
			case WAITING:
			case ABORTED:
			default:
				throw new IllegalArgumentException("Unknwon case for ending activity execution :  " + executionState.name());
		}

	}

	private ActivityExecutionWorkspace getWorkspaceForActivityExecution(final Long aceId, final Boolean in) {
		Assertion.check()
				.isNotNull(aceId)
				.isNotNull(in);
		// ---
		final OActivityWorkspace activityWorkspace = activityWorkspaceDAO.getActivityWorkspace(aceId, in).get();
		return new ActivityExecutionWorkspace(mapCodec.decode(activityWorkspace.getWorkspace()));
	}

	private void saveActivityExecutionWorkspace(final Long aceId, final ActivityExecutionWorkspace workspace, final Boolean in) {
		Assertion.check()
				.isNotNull(aceId)
				.isNotNull(in)
				.isNotNull(workspace);
		// ---
		lockActivityExecution(aceId);
		// we need at most one workspace in and one workspace out
		final OActivityWorkspace activityWorkspace = activityWorkspaceDAO.getActivityWorkspace(aceId, in).orElseGet(OActivityWorkspace::new);
		activityWorkspace.setAceId(aceId);
		activityWorkspace.setIsIn(in);
		activityWorkspace.setWorkspace(mapCodec.encode(workspace.asMap()));

		activityWorkspaceDAO.save(activityWorkspace);

	}

	private void lockActivityExecution(final Long aceId) {
		final UID<OActivityExecution> activityExecutionURI = UID.of(OActivityExecution.class, aceId);
		entityStoreManager.readOneForUpdate(activityExecutionURI);
	}

	private void doChangeExecutionState(final OActivityExecution activityExecution, final ExecutionState executionState) {
		Assertion.check().isNotNull(activityExecution);
		// ---
		changeActivityExecutionState(activityExecution, executionState);
		activityExecutionDAO.save(activityExecution);

		// If it's an error the entire process is in Error
		if (executionState == ExecutionState.ERROR) {
			endProcessExecution(activityExecution.getPreId(), ExecutionState.ERROR);
		}

	}

	private void finishProcessExecution(final OActivityExecution activityExecution) {
		if (transactionManager.hasCurrentTransaction()) {
			doFinishProcessExecution(activityExecution);
		} else {
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				doFinishProcessExecution(activityExecution);
				transaction.commit();
			}
		}
	}

	private void doFinishProcessExecution(final OActivityExecution activityExecution) {
		Assertion.check().isNotNull(activityExecution);
		// ---
		endActivity(activityExecution);
		endProcessExecution(activityExecution.getPreId(), ExecutionState.DONE);
	}

	private void endActivity(final OActivityExecution activityExecution) {
		activityExecution.setEndTime(Instant.now());
		changeActivityExecutionState(activityExecution, ExecutionState.DONE);
		activityExecutionDAO.save(activityExecution);

	}

	private void handleOtherServices(final ActivityEngine activityEngine, final OActivityExecution activityExecution, final ActivityExecutionWorkspace resultWorkspace) {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			// We save the workspace which is the minimal state
			saveActivityExecutionWorkspace(activityExecution.getAceId(), resultWorkspace, false);
			if (activityEngine instanceof AbstractActivityEngine) {
				// If the engine extends the abstractEngine we can provide the services associated (LOGGING,...)
				saveActivityLogs(activityExecution.getAceId(), ((AbstractActivityEngine) activityEngine).getLogger(), resultWorkspace);
			}
			transaction.commit();
		}
	}

	private void endProcessExecution(final Long preId, final ExecutionState executionState) {
		final OProcessExecution processExecution = processExecutionDAO.get(preId);
		processExecution.process().load();
		processExecution.setEndTime(Instant.now());
		changeProcessExecutionState(processExecution, executionState);
		processExecutionDAO.save(processExecution);
		final DtList<OActivityExecution> activityExecutions = activityExecutionDAO.findAll(Criterions.isEqualTo(OActivityExecutionFields.preId, preId), DtListState.of(100));
		traceProcessExecution(processExecution, activityExecutions);

	}

	private void saveActivityLogs(final Long aceId, final ActivityLogger activityLogger, final ActivityExecutionWorkspace resultWorkspace) {
		Assertion.check()
				.isNotNull(aceId)
				.isNotNull(activityLogger);
		// ---
		// we need at most on log per activityExecution
		final OActivityLog activityLog = activityLogDAO.getActivityLogByAceId(aceId).orElseGet(OActivityLog::new);
		activityLog.setAceId(aceId);
		final String log = new StringBuilder(activityLog.getLog() == null ? "" : activityLog.getLog()).append(activityLogger.getLogAsString())//
				.append("ResultWorkspace : ").append(mapCodec.encode(resultWorkspace.asMap())).append("\n")//
				.toString();
		activityLog.setLog(log);
		if (resultWorkspace.getAttachment() != null) {
			activityLog.setAttachment(resultWorkspace.getAttachment());
		}
		activityLogDAO.save(activityLog);
	}

	private int getUnusedWorkersCount() {
		Assertion.check().isNotNull(workers);
		// ---
		if (workers instanceof ThreadPoolExecutor) {
			final ThreadPoolExecutor workersPool = (ThreadPoolExecutor) workers;
			return workersCount - workersPool.getActiveCount();
		}
		return workersCount;
	}

	private void handleDeadNodeProcesses() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			// We wait two heartbeat to be sure that the node is dead
			final Instant maxDate = Instant.now().minusSeconds(2L * executionPeriodSeconds);
			executionPAO.handleProcessesOfDeadNodes(maxDate);
			transaction.commit();
		}
	}

	private void handleNodeDeadProcesses(final Long nodeId) {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			executionPAO.handleDeadProcessesOfNode(nodeId);
			transaction.commit();
		}
	}

	private static void changeActivityExecutionState(final OActivityExecution activityExecution, final ExecutionState executionState) {
		// we need to check if the transistion is valid
		activityExecution.setEstCd(executionState.name());
	}

	private static void changeProcessExecutionState(final OProcessExecution processExecution, final ExecutionState executionState) {
		// we need to check if the transistion is valid
		processExecution.setEstCd(executionState.name());
	}

	private void traceProcessExecution(final OProcessExecution processExecution, final DtList<OActivityExecution> activityExecutions) {
		final AProcessBuilder processBuilder = AProcess.builder("jobs", processExecution.process().get().getName(), processExecution.getBeginTime(), processExecution.getEndTime())
				.setMeasure("success", ExecutionState.DONE.name().equals(processExecution.getEstCd()) ? 100.0 : 0.0)
				.addTag("nodeName", nodeName)
				.addTag("status", processExecution.getEstCd());
		activityExecutions.forEach(activityExecution -> processBuilder.addSubProcess(
				AProcess.builder("activity", activityExecution.getEngine(), activityExecution.getBeginTime(), activityExecution.getEndTime())
						.build()));
		analyticsManager.addProcess(processBuilder.build());
	}

}
