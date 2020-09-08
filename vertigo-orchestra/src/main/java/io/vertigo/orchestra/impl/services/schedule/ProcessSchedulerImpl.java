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
package io.vertigo.orchestra.impl.services.schedule;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.services.execution.ProcessExecutor;
import io.vertigo.orchestra.services.schedule.ProcessScheduler;

/**
 * Implémentation du manager de la planification.
 *
 * @author mlaroche.
 * @version $Id$
 */
public final class ProcessSchedulerImpl implements ProcessScheduler {
	private final Map<ProcessType, ProcessSchedulerPlugin> schedulerPluginsMap = new EnumMap<>(ProcessType.class);

	/**
	 * Constructeur.
	 * @param schedulerPlugins la liste des plugins de gestion de la planification
	 */
	public ProcessSchedulerImpl(final List<ProcessSchedulerPlugin> schedulerPlugins, final ProcessExecutor processExecutor) {
		Assertion.check()
				.isNotNull(schedulerPlugins)
				.isNotNull(processExecutor);
		//---
		for (final ProcessSchedulerPlugin schedulerPlugin : schedulerPlugins) {
			//-1-- start
			schedulerPlugin.setProcessExecutor(processExecutor);
			//-2-- register
			Assertion.check().isFalse(schedulerPluginsMap.containsKey(schedulerPlugin.getHandledProcessType()), "Only one plugin can manage the processType {0}", schedulerPlugin.getHandledProcessType());
			schedulerPluginsMap.put(schedulerPlugin.getHandledProcessType(), schedulerPlugin);
		}
	}

	//--------------------------------------------------------------------------------------------------
	//--- Public
	//--------------------------------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public void scheduleAt(final ProcessDefinition processDefinition, final Instant planifiedTime, final Map<String, String> initialParams) {
		Assertion.check()
				.isNotNull(processDefinition)
				.isNotNull(planifiedTime)
				.isNotNull(initialParams);
		// ---
		getPluginByType(processDefinition.getProcessType())
				.scheduleAt(processDefinition, planifiedTime, initialParams);
	}

	private ProcessSchedulerPlugin getPluginByType(final ProcessType processType) {
		final ProcessSchedulerPlugin schedulerPlugin = schedulerPluginsMap.get(processType);
		Assertion.check().isNotNull(schedulerPlugin, "No plugin found for managing processType {0}", processType.name());
		return schedulerPlugin;
	}

}
