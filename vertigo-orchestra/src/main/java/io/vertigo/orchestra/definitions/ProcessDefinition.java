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

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.orchestra.services.execution.RunnableActivityEngine;

/**
 * Définition d'un processus Orchestra.
 * Une définition doit être créee par le builder associé.
 * @author mlaroche.
 */
@DefinitionPrefix(ProcessDefinition.PREFIX)
public final class ProcessDefinition extends AbstractDefinition {
	public static final String PREFIX = "Pro";

	//TODO : ID doit être immutable!!
	//---immutables
	private long id;
	private final List<ActivityDefinition> activities;
	private final ProcessType processType;

	//---params dev / admin
	private final String label;
	private final boolean active;

	private final ProcessTriggeringStrategy triggeringStrategy;
	private final Map<String, String> metadatas;
	private final boolean needUpdate;

	/**
	 * Constructor only used by its builder.
	 * @param name
	 * @param cronExpression
	 * @param initialParams
	 * @param multiExecution
	 * @param activities
	 */
	ProcessDefinition(
			final String name,
			final String label,
			final boolean active,
			final ProcessType processType,
			final Map<String, String> metadatas,
			final boolean needUpdate,
			final ProcessTriggeringStrategy triggeringStrategy,
			final List<ActivityDefinition> activities) {
		super(name);
		//---
		Assertion.check()
				.isNotBlank(label)
				.isNotNull(processType)
				.isNotNull(metadatas)
				.isNotNull(triggeringStrategy)
				.isNotNull(activities);
		//---
		this.label = label;
		this.active = active;
		this.processType = processType;
		this.activities = activities;
		this.metadatas = metadatas;
		this.needUpdate = needUpdate;
		this.triggeringStrategy = triggeringStrategy;
	}

	/**
	 * Static method factory for ProcessDefinitionBuilder
	 * @param processName le nom du processus
	 * @param processLabel le libellé du processus
	 * @return ProcessDefinitionBuilder
	 */
	public static ProcessDefinitionBuilder builder(final String processName, final String processLabel) {
		return new ProcessDefinitionBuilder(processName, processLabel);
	}

	public static ProcessDefinitionBuilder legacyBuilder(final String processName, final Class<? extends RunnableActivityEngine> engineClass) {
		return new ProcessDefinitionBuilder(processName, processName)
				.withProcessType(ProcessType.UNSUPERVISED)
				.withMultiExecution()
				.addActivity("main", "Main", engineClass);

	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public ProcessType getProcessType() {
		return processType;
	}

	public boolean isActive() {
		return active;
	}

	public Map<String, String> getMetadatas() {
		return metadatas;
	}

	public List<ActivityDefinition> getActivities() {
		return activities;
	}

	public boolean getNeedUpdate() {
		return needUpdate;
	}

	public ProcessTriggeringStrategy getTriggeringStrategy() {
		return triggeringStrategy;
	}

}
