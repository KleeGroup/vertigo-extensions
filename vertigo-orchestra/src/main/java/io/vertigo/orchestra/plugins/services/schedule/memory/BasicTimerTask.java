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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimerTask;

import io.vertigo.core.lang.Assertion;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.plugins.services.MapCodec;
import io.vertigo.orchestra.services.execution.ProcessExecutor;

/**
 * Timer permettant l'exécution d'un Job.
 * @author npiedeloup
 */
final class BasicTimerTask extends TimerTask {
	private final ProcessDefinition processDefinition;
	private final Map<String, String> params;
	private final ProcessExecutor processExecutor;

	private final MapCodec mapCodec = new MapCodec();

	/**
	 * Constructeur.
	 * @param jobManager Manager des jobs.
	 */
	BasicTimerTask(final ProcessDefinition processDefinition, final Map<String, String> params, final ProcessExecutor processExecutor) {
		Assertion.check()
				.isNotNull(processDefinition)
				.isNotNull(params)
				.isNotNull(processExecutor);
		//-----
		this.processDefinition = processDefinition;
		this.params = params;
		this.processExecutor = processExecutor;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		final Optional<String> initialParamsOpt = getActualParams(processDefinition.getTriggeringStrategy().getInitialParams(), params, mapCodec);
		processExecutor.execute(processDefinition, initialParamsOpt);
	}

	private static Optional<String> getActualParams(final Map<String, String> definitionParams, final Map<String, String> planificationParams, final MapCodec mapCodec) {
		if (definitionParams.isEmpty()) {
			if (planificationParams.isEmpty()) {
				// no params
				return Optional.<String> empty();
			}
			// just planif
			return Optional.<String> of(mapCodec.encode(planificationParams));
		}
		// we merge all the params
		final Map<String, String> mergedParams = new HashMap<>(definitionParams);
		mergedParams.putAll(planificationParams); // actualMerge
		return Optional.<String> of(mapCodec.encode(mergedParams));
	}
}
