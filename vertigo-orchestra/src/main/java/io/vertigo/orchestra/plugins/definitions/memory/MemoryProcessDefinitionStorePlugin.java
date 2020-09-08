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
package io.vertigo.orchestra.plugins.definitions.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.vertigo.core.lang.Assertion;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.impl.definitions.ProcessDefinitionStorePlugin;

/**
 * Plugin de gestion des définitions en mémoire dans le DefinitionSpace.
 * @author mlaroche
 *
 */
public class MemoryProcessDefinitionStorePlugin implements ProcessDefinitionStorePlugin {

	private final Map<String, ProcessDefinition> processDefinitions = new ConcurrentHashMap<>();

	@Override
	public void createOrUpdateDefinition(final ProcessDefinition processDefinition) {
		processDefinitions.put(processDefinition.getName(), processDefinition);
	}

	@Override
	public boolean processDefinitionExists(final String processName) {
		Assertion.check().isNotBlank(processName);
		// ---
		return processDefinitions.containsKey(processName);
	}

	@Override
	public ProcessDefinition getProcessDefinition(final String processName) {
		Assertion.check().isNotBlank(processName);
		// ---
		final ProcessDefinition processDefinition = processDefinitions.get(processName);
		Assertion.check().isNotNull(processDefinition, "ProcessDefinition '{0}' not found in ({1})", processName, processDefinitions.keySet());
		return processDefinition;
	}

	@Override
	public List<ProcessDefinition> getAllProcessDefinitions() {
		return new ArrayList<>(processDefinitions.values());
	}

	@Override
	public void updateProcessDefinitionProperties(final ProcessDefinition processDefinition, final Optional<String> cronExpression, final boolean multiExecution, final int rescuePeriod,
			final boolean active) {
		// unsupported
		throw new UnsupportedOperationException("An in memory definition is immutable");
	}

	@Override
	public void updateProcessDefinitionInitialParams(final ProcessDefinition processDefinition, final Map<String, String> initialParams) {
		throw new UnsupportedOperationException("An in memory definition is immutable");
	}

	@Override
	public ProcessType getHandledProcessType() {
		return ProcessType.UNSUPERVISED;
	}

}
