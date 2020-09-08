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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.util.ListBuilder;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.orchestra.services.execution.ActivityEngine;

/**
 * Builder d'une définition de processus Orchestra.
 *
 * @author mlaroche.
 * @version $Id$
 */
public final class ProcessDefinitionBuilder implements Builder<ProcessDefinition> {
	private final String name;
	private final String label;
	private ProcessType myType;
	private boolean myActive;
	private Optional<String> myCronExpression = Optional.empty();
	private final MapBuilder<String, String> myInitialParams = new MapBuilder<>();
	private boolean multiExecution;
	private boolean needUpdate;
	private int myRescuePeriod;
	private final ListBuilder<ActivityDefinition> activitiesBuilder = new ListBuilder<>();

	private final Map<String, String> myMetadatas = new HashMap<>();

	/**
	 * Constructor.
	 * @param processName le nom du processus
	 * @param processLabel le libellé du processus
	 */
	ProcessDefinitionBuilder(final String processName, final String processLabel) {
		Assertion.check().isNotBlank(processName);
		//-----
		name = processName;
		label = processLabel;
		// active by default
		myActive = true;
	}

	/**
	 * Processus actif.
	 * @return this
	 */
	public ProcessDefinitionBuilder inactive() {
		myActive = false;
		return this;
	}

	/**
	 *
	 * @param type
	 * @return this
	 */
	public ProcessDefinitionBuilder withProcessType(final ProcessType type) {
		Assertion.check().isNotNull(type);
		myType = type;
		return this;
	}

	/**
	 * Processus autorisant la multi-execution.
	 * @return this
	 */
	public ProcessDefinitionBuilder withMultiExecution() {
		multiExecution = true;
		return this;
	}

	/**
	 * Durée pendant laquelle une planification peut être restaurée (durée de validité).
	 * @param rescuePeriod la durée en secondes
	 * @return this
	 */
	public ProcessDefinitionBuilder withRescuePeriod(final int rescuePeriod) {
		myRescuePeriod = rescuePeriod;
		return this;
	}

	/**
	 * Adds params used to start the first activity.
	 * @param initialParams the params definened as a map of key-value
	 * @return this
	 */
	public ProcessDefinitionBuilder addInitialParams(final Map<String, String> initialParams) {
		myInitialParams.putAll(initialParams);
		return this;
	}

	/**
	 * Adds param used to start the first activity.
	 * @param paramName the name of the param
	 * @param paramValue the value of the param
	 * @return this
	 */
	public ProcessDefinitionBuilder addInitialParam(final String paramName, final String paramValue) {
		myInitialParams.put(paramName, paramValue);
		return this;
	}

	/**
	 * Définit l'expression cron du process.
	 * @param cronExpression l'expression cron de recurrence
	 * @return this
	 */
	public ProcessDefinitionBuilder withCronExpression(final String cronExpression) {
		Assertion.check().isNotNull(cronExpression);
		// ---
		myCronExpression = Optional.of(cronExpression);
		return this;
	}

	/**
	 * Ajoute une activité à un processus.
	 * @param activityName le nom de l'activité (Code)
	 * @param activityLabel Le libelle de l'activité (Ihm)
	 * @param engineClass Le moteur d'exécution de l'activité
	 * @return this
	 */
	public ProcessDefinitionBuilder addActivity(final String activityName, final String activityLabel, final Class<? extends ActivityEngine> engineClass) {
		final ActivityDefinition activity = new ActivityDefinition(activityName, activityLabel, engineClass);
		activitiesBuilder.add(activity);
		return this;
	}

	/**
	 * Définit le informations du process.
	 * @param metadatas les métadonnées sous format JSON
	 * @return this
	 */
	public ProcessDefinitionBuilder withMetadatas(final Map<String, String> metadatas) {
		Assertion.check().isNotNull(metadatas);
		// ---
		myMetadatas.putAll(metadatas);
		return this;
	}

	/**
	 * Définit si au prochain démarrage de l'application la définition doit être mise à jour.
	 * @return this
	 */
	public ProcessDefinitionBuilder withNeedUpdate() {
		needUpdate = true;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ProcessDefinition build() {

		if (myType == null) {
			myType = ProcessType.SUPERVISED;
		}

		return new ProcessDefinition(
				name,
				label,
				myActive,
				myType,
				myMetadatas,
				needUpdate,
				new ProcessTriggeringStrategy(
						myCronExpression,
						myInitialParams.unmodifiable().build(),
						multiExecution,
						myRescuePeriod),
				activitiesBuilder.unmodifiable().build());
	}

}
