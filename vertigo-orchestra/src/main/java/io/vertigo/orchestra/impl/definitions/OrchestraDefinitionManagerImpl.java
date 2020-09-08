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
package io.vertigo.orchestra.impl.definitions;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;

/**
 * Implémentation du manager des définitions de processus Orchestra.
 *
 * @author mlaroche.
 * @version $Id$
 */
public class OrchestraDefinitionManagerImpl implements OrchestraDefinitionManager {
	private final Map<ProcessType, ProcessDefinitionStorePlugin> processDefinitionStorePluginsByProcessType = new EnumMap<>(ProcessType.class);

	/**
	 * Constructeur du gestionnaire de définitions.
	 * @param processDefinitionStorePlugins la liste des plugins gérant des définitions de processus
	 */
	@Inject
	public OrchestraDefinitionManagerImpl(final List<ProcessDefinitionStorePlugin> processDefinitionStorePlugins) {
		Assertion.check()
				.isNotNull(processDefinitionStorePlugins)
				.isFalse(processDefinitionStorePlugins.isEmpty(), "At least one ProcessDefinitionStorePlugin is required");
		// ---
		for (final ProcessDefinitionStorePlugin storePlugin : processDefinitionStorePlugins) {
			Assertion.check().isFalse(processDefinitionStorePluginsByProcessType.containsKey(storePlugin.getHandledProcessType()), "Only one plugin can manage the processType {0}",
					storePlugin.getHandledProcessType());
			processDefinitionStorePluginsByProcessType.put(storePlugin.getHandledProcessType(), storePlugin);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ProcessDefinition getProcessDefinition(final String processName) {
		Assertion.check().isNotBlank(processName);
		// ---
		return processDefinitionStorePluginsByProcessType.values()
				.stream()
				.filter(processDefinitionStorePlugin -> processDefinitionStorePlugin.processDefinitionExists(processName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Cannot find process with name " + processName))
				.getProcessDefinition(processName);
	}

	/** {@inheritDoc} */
	@Override
	public List<ProcessDefinition> getAllProcessDefinitions() {
		return processDefinitionStorePluginsByProcessType.values()
				.stream()
				.flatMap(processDefinitionStorePlugin -> processDefinitionStorePlugin.getAllProcessDefinitions().stream())
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public void createOrUpdateDefinition(final ProcessDefinition processDefinition) {
		Assertion.check().isNotNull(processDefinition);
		//---
		final ProcessDefinitionStorePlugin storePlugin = processDefinitionStorePluginsByProcessType.get(processDefinition.getProcessType());
		Assertion.check().isNotNull(storePlugin, "No plugin found for managing processType {0}", processDefinition.getProcessType());
		// ---
		storePlugin.createOrUpdateDefinition(processDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void updateProcessDefinitionProperties(
			final String processName,
			final Optional<String> cronExpression,
			final boolean multiExecution,
			final int rescuePeriod,
			final boolean active) {
		Assertion.check()
				.isNotBlank(processName)
				.isNotNull(cronExpression)
				.isNotNull(rescuePeriod);
		//---
		final ProcessDefinition processDefinition = getProcessDefinition(processName);
		getPluginByType(processDefinition.getProcessType()).updateProcessDefinitionProperties(processDefinition, cronExpression, multiExecution, rescuePeriod, active);
	}

	/** {@inheritDoc} */
	@Override
	public void updateProcessDefinitionInitialParams(final String processName, final Map<String, String> initialParams) {
		Assertion.check()
				.isNotBlank(processName)
				.isNotNull(initialParams);
		//---
		final ProcessDefinition processDefinition = getProcessDefinition(processName);
		getPluginByType(processDefinition.getProcessType()).updateProcessDefinitionInitialParams(processDefinition, initialParams);
	}

	private ProcessDefinitionStorePlugin getPluginByType(final ProcessType processType) {
		final ProcessDefinitionStorePlugin storePlugin = processDefinitionStorePluginsByProcessType.get(processType);
		Assertion.check().isNotNull(storePlugin, "No plugin found for managing processType {0}", processType.name());
		return storePlugin;
	}

	@Override
	public List<ProcessDefinition> getAllProcessDefinitionsByType(final ProcessType processType) {
		Assertion.check().isNotNull(processType);
		//---
		return getPluginByType(processType).getAllProcessDefinitions();
	}

}
