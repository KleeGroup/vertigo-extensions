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

import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.task.definitions.TaskAttribute;
import io.vertigo.datamodel.task.definitions.TaskDefinition;

/**
 * Résultat de l'exécution d'une tache.
 * @author dchallas
 */
public final class TaskResult {
	/**
	 * Définition de la tache.
	 */
	private final Optional<TaskAttribute> outTaskAttributeOptional;

	private final Object result;

	/**
	 * Constructeur.
	 * Le constructeur est protégé, il est nécessaire de passer par le Builder.
	 */
	TaskResult(final TaskDefinition taskDefinition, final Object result) {
		Assertion.check().isNotNull(taskDefinition);
		//-----
		outTaskAttributeOptional = taskDefinition.getOutAttributeOption();
		outTaskAttributeOptional.ifPresent(outTaskAttribute -> outTaskAttribute.checkAttribute(result));

		this.result = result;
	}

	/**
	 * Getter générique.
	 * Retourne la valeur d'un paramètre conforme au contrat de l'attribut du service
	 *
	 * @param <V> Type de la valeur
	 * @return Result
	 */
	public <V> V getResult() {
		Assertion.check().isTrue(outTaskAttributeOptional.isPresent(), "this task does not provide any result");
		return (V) result;
	}
}
