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
package io.vertigo.orchestra.definitions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.node.component.Manager;

/**
 * Interface (privé) de la gestion des définitions de processus.
 *
 * @author mlaroche.
 * @version $Id$
 */
public interface OrchestraDefinitionManager extends Manager {
	//-----
	//-READ
	//-----
	/**
	 * Récupère une définition de processus par son nom.
	 * @param processName le nom du processus à récupérer
	 * @return la définition du processus
	 */
	ProcessDefinition getProcessDefinition(String processName);

	/**
	 * Récupère l'ensemble des processus gérés par orchestra.
	 * @return la liste des processus
	 */
	List<ProcessDefinition> getAllProcessDefinitions();

	/**
	 * Récupère l'ensemble des processus gérés par orchestra d'un type donné.
	 * @param processType le type de processus recherché
	 * @return la liste des processus
	 */
	List<ProcessDefinition> getAllProcessDefinitionsByType(ProcessType processType);

	//-----
	//-WRITE
	//-----
	/**
	 * Creer ou mettre à jour un processus orchestra.
	 * @param processDefinition la définition à créer ou mettre à jour.
	 */
	void createOrUpdateDefinition(ProcessDefinition processDefinition);

	/**
	 * Met à jour les propriétés d'une définition sans la rendre obsolète.
	 * @param processName le nom du processus à mettre à jour
	 * @param cronExpression la nouvelle expression Cron à utiliser
	 * @param multiExecution le processus autorise-t-il la multi execution
	 * @param rescuePeriod la nouvelle durée de validité d'une planification
	 * @param active le processus est-il actif
	 */
	void updateProcessDefinitionProperties(String processName, Optional<String> cronExpression, boolean multiExecution, int rescuePeriod, boolean active);

	/**
	 * Met à jour les paramètres initiaux d'exécution d'un processus
	 * @param processName le nom du processus à mettre à jour
	 * @param initialParams the params used to start the first activity
	 */
	void updateProcessDefinitionInitialParams(String processName, Map<String, String> initialParams);

}
