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
package io.vertigo.orchestra.plugins.services.schedule.memory;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.impl.services.schedule.CronExpression;
import io.vertigo.orchestra.impl.services.schedule.ProcessSchedulerPlugin;
import io.vertigo.orchestra.services.execution.ProcessExecutor;

public class MemoryProcessSchedulerPlugin implements ProcessSchedulerPlugin, Activeable {
	private ProcessExecutor myProcessExecutor;
	/**
	 * Pool de timers permettant l'exécution des Jobs.
	 */
	private final TimerPool timerPool = new TimerPool();

	@Inject
	public MemoryProcessSchedulerPlugin(
			final OrchestraDefinitionManager orchestraDefinitionManager) {
		Assertion.check().isNotNull(orchestraDefinitionManager);
		//---
		Node.getNode().registerPreActivateFunction(() -> {
			orchestraDefinitionManager.getAllProcessDefinitionsByType(getHandledProcessType())
					.stream()
					.filter(processDefinition -> processDefinition.getTriggeringStrategy().getCronExpression().isPresent())
					.forEach(this::scheduleWithCron);
		});
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//
	}

	@Override
	public void setProcessExecutor(final ProcessExecutor processExecutor) {
		Assertion.check().isNotNull(processExecutor);
		//---
		myProcessExecutor = processExecutor;
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		timerPool.close();
	}

	private void scheduleWithCron(final ProcessDefinition processDefinition) {
		scheduleAtRecurrent(processDefinition, Instant.now(), Collections.emptyMap());

	}

	void scheduleAtRecurrent(final ProcessDefinition processDefinition, final Instant planifiedTime, final Map<String, String> initialParams) {
		//a chaque exécution il est nécessaire de reprogrammer l'execution.
		final Instant nextExecutionDate = getNextExecutionDateFrom(processDefinition, planifiedTime);
		scheduleAt(processDefinition, nextExecutionDate, Collections.emptyMap());

		//a chaque exécution il est nécessaire de reprogrammer l'execution.
		final Instant nextReschedulerDate = nextExecutionDate.plusSeconds(1); //on reprogramme à l'heure dite + 1seconde (comme on est sur le m^me timer elle passera après
		final TimerTask task = createRescheduledTimerTask(processDefinition, nextReschedulerDate);
		timerPool.getTimer(processDefinition.getName()).schedule(task, new Date(nextReschedulerDate.toEpochMilli()));
		log("Tache de reprogrammation du Job ", processDefinition, nextReschedulerDate);

	}

	/** {@inheritDoc} */
	@Override
	public void scheduleAt(final ProcessDefinition processDefinition, final Instant planifiedTime, final Map<String, String> initialParams) {
		Assertion.check()
				.isNotNull(processDefinition)
				.isNotNull(planifiedTime)
				.isNotNull(initialParams);
		//---
		final TimerTask task = createTimerTask(processDefinition, initialParams);
		timerPool.getTimer(processDefinition.getName()).schedule(task, new Date(planifiedTime.toEpochMilli()));
		log("Job ", processDefinition, planifiedTime);

	}

	@Override
	public ProcessType getHandledProcessType() {
		return ProcessType.UNSUPERVISED;
	}

	private static Instant getNextExecutionDateFrom(final ProcessDefinition processDefinition, final Instant fromDate) {
		try {
			final CronExpression cronExpression = new CronExpression(processDefinition.getTriggeringStrategy().getCronExpression().get());
			return Optional.of(cronExpression.getNextValidTimeAfter(fromDate))
					.orElseThrow(() -> new IllegalStateException("Cannot find a next execution date for process :" + processDefinition));
		} catch (final ParseException e) {
			throw WrappedException.wrap(e, "Process' cron expression is not valid, process cannot be planned");
		}
	}

	private TimerTask createTimerTask(final ProcessDefinition processDefinition, final Map<String, String> additionalParams) {
		return new BasicTimerTask(processDefinition, additionalParams, myProcessExecutor);
	}

	private TimerTask createRescheduledTimerTask(final ProcessDefinition processDefinition, final Instant nextExecutionDate) {
		return new ReschedulerTimerTask(this, processDefinition, nextExecutionDate);
	}

	private static void log(final String info, final ProcessDefinition processDefinition, final Instant date) {
		final String instantAsString = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withLocale(Locale.FRANCE).withZone(ZoneOffset.UTC).format(date);
		getLogger(processDefinition.getName()).info(info + processDefinition.getName() + " programmé pour " + instantAsString);
	}

	private static Logger getLogger(final String jobName) {
		return LogManager.getLogger(jobName);
	}

	//=========================================================================
	//=============================POOL de TIMER===============================
	//=========================================================================
	static class TimerPool {
		// cette implémentation est simplement basée sur la classe java.util.Timer du JDK
		private final Map<String, Timer> timerMap = new HashMap<>();

		// pour interrupt
		private final Map<String, Thread> threadMap = new HashMap<>();

		synchronized Timer getTimer(final String jobName) {
			//Synchronized car appelée lors de la programation des Timers,
			//la plupart sont programmés dans lors de l'initialisation,
			//mais il est possible de programmer sur des evenements métiers.
			//Utilisé QUE lors des programmations, pas à l'exec.
			Timer timer = timerMap.get(jobName);
			if (timer == null) {
				// le timer est démon pour ne pas empêcher l'arrêt de la jvm,
				// timerName est utilisé comme nom du thread java
				timer = new Timer(jobName, true);
				timerMap.put(jobName, timer);
				final TimerTask registrerThreadTimerTask = new TimerTask() {
					/** {@inheritDoc} */
					@Override
					public void run() {
						registrerTimerThread(Thread.currentThread());
					}
				};
				timer.schedule(registrerThreadTimerTask, new Date());
			}
			return timer;
		}

		void registrerTimerThread(final Thread thread) {
			threadMap.put(thread.getName(), thread);
		}

		void close() {
			//La méthode close est appelée par le gestionnaire des managers.
			//Elle n'a pas besoin d'être synchronisée.
			// on cancel les timers pour qu'ils n'aient plus de schedule
			for (final Timer timer : timerMap.values()) {
				timer.cancel();
			}
			timerMap.clear();
			// on appelle interrupt() sur les threads pour qu'un job en cours
			// puisse tester Thread.currentThread().isInterrupted() et s'arrêter promptement
			for (final Thread thread : threadMap.values()) {
				thread.interrupt();
			}
			threadMap.clear();
		}
	}

}
