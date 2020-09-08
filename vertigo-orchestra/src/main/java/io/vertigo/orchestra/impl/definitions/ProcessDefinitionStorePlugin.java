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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;

/**
 * Plugin de gestion des définitions de processus.
 * @author mlaroche
 *
 */
public interface ProcessDefinitionStorePlugin extends Plugin {

	/**
	 * @see io.vertigo.orchestra.definitions.OrchestraDefinitionManager#createOrUpdateDefinition(ProcessDefinition)
	 */
	void createOrUpdateDefinition(final ProcessDefinition processDefinition);

	/**
	 * Retourne si une definition existe
	 * @param processName le nom du processus
	 * @return vrai si la definition existe
	 */
	boolean processDefinitionExists(final String processName);

	/**
	 * @see io.vertigo.orchestra.definitions.OrchestraDefinitionManager#getProcessDefinition(String)
	 */
	ProcessDefinition getProcessDefinition(final String processName);

	/**
	 * @see io.vertigo.orchestra.definitions.OrchestraDefinitionManager#getAllProcessDefinitions()
	 */
	List<ProcessDefinition> getAllProcessDefinitions();

	/**
	 * @see io.vertigo.orchestra.definitions.OrchestraDefinitionManager#updateProcessDefinitionProperties(String, Optional, boolean, int, boolean)
	 */
	void updateProcessDefinitionProperties(final ProcessDefinition processDefinition, final Optional<String> cronExpression, final boolean multiExecution, final int rescuePeriod,
			final boolean active);

	/**
	 * @see io.vertigo.orchestra.definitions.OrchestraDefinitionManager#updateProcessDefinitionInitialParams(String, Map)
	 */
	void updateProcessDefinitionInitialParams(final ProcessDefinition processDefinition, final Map<String, String> initialParams);

	/**
	 * Retourne le type de processus géré par le plugin
	 * @return le type de processus géré
	 */
	ProcessType getHandledProcessType();

}
