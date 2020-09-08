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

import java.time.Instant;
import java.util.Collections;
import java.util.TimerTask;

import io.vertigo.core.lang.Assertion;
import io.vertigo.orchestra.definitions.ProcessDefinition;

/**
 * Timer permettant de reprogrammer un job.
 * @author npiedeloup
 */
final class ReschedulerTimerTask extends TimerTask {
	private final MemoryProcessSchedulerPlugin simpleSchedulerPlugin;
	private final ProcessDefinition processDefinition;
	private final Instant nextExecutionDate;

	/**
	 * Constructeur.
	 * @param simpleSchedulerPlugin Scheduler
	 * @param processDefinition Définition du job à reprogrammer
	 * @param hour Heure du prochaine lancement
	 * @param minute Minute du prochaine lancement
	 */
	ReschedulerTimerTask(final MemoryProcessSchedulerPlugin simpleSchedulerPlugin, final ProcessDefinition processDefinition, final Instant nextExecutionDate) {
		Assertion.check()
				.isNotNull(simpleSchedulerPlugin)
				.isNotNull(processDefinition)
				.isNotNull(nextExecutionDate);
		//-----
		this.simpleSchedulerPlugin = simpleSchedulerPlugin;
		this.processDefinition = processDefinition;
		this.nextExecutionDate = nextExecutionDate;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		// pour un job s'exécutant tous les jours, on schedule chaque jour
		// pour éviter que l'exécution se décale d'une heure lors des changements d'heure été-hiver

		// On rappel le scheduleAtRecurrent qui reprogrammera à la fois la prochaine task du Job et celle du ReschedulerTimerTask.
		simpleSchedulerPlugin.scheduleAtRecurrent(processDefinition, nextExecutionDate, Collections.emptyMap());
	}

}
