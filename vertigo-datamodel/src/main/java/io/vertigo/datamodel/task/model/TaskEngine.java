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
package io.vertigo.datamodel.task.model;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.task.definitions.TaskDefinition;

/**
 * Moteur précisant le mode d'exécution d'une définition de tache.
 * Attention ce moteur est avec état ; il est donc nécessaire de le recréer avant chaque utilisation.
 *
 * @author fconstantin, pchretien
 * @see io.vertigo.datamodel.task.model.Task
 */
public abstract class TaskEngine {
	private Task input;
	private Object result = Void.TYPE;

	/**
	 * Réalise l'exécution d'une tache.
	 * L'implémentation n'est pas responsable de la gestion de la transaction.
	 * Un rollback de la transaction sera automatiquement exécuté au cas où
	 * une exception survient.
	 * La tache permet d'accéder à la définition des paramètres d'entrée-sortie
	 * ainsi qu'à la chaine de configuration de la tache.
	 */
	protected abstract void execute();

	/**
	 * Exécute le travail.
	 * Le travail s'exécute dans la transaction courante si elle existe.
	 *  - Le moteur n'est pas responsable de de créer une transaction.
	 *  - En revanche si une telle transaction existe elle est utilisée.
	 * @param task Task to process
	 * @return TaskResult contenant les résultats
	 */
	public final TaskResult process(final Task task) {
		Assertion.check().isNotNull(task);
		//-----
		input = task;
		execute();
		return new TaskResult(task.getDefinition(), result == Void.TYPE ? null : result);
	}

	/**
	* Getter avec un type générique.
	* Retourne la valeur d'un paramètre (INPUT)
	*
	* @param <J> Type java de l'objet recherché
	* @param attributeName Nom du paramètre
	* @return Valeur
	*/
	protected final <J> J getValue(final String attributeName) {
		return input.getValue(attributeName);
	}

	/**
	 * Return the value of a property in the excecution context of the task
	 *
	 * @param contextParam name of the context param
	 * @return Valeur
	 */
	public String getContextProperty(final String contextParam) {
		return input.getContextProperty(contextParam);
	}

	/**
	 * Setter générique
	 * Affecte la valeur d'un paramètre (OUTPUT)
	 *
	 * @param o Valeur
	 */
	protected final void setResult(final Object o) {
		Assertion.check()
				.isTrue(o != Void.TYPE, "you can't  invoke setResult with Void, use null instead")
				.isTrue(result == Void.TYPE, "you can't  invoke setResult more than one time");
		//-----
		result = o;
	}

	/**
	 * Retourne la définition de la tache.
	 * taskDataSet est non visible (Framework).
	 *
	 * @return Définition de la tache
	 */
	protected final TaskDefinition getTaskDefinition() {
		return input.getDefinition();
	}

}
