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
package io.vertigo.dashboard.ui.dynamo.model;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.task.definitions.TaskDefinition;

public final class TaskModel {
	private final TaskDefinition taskDefinition;
	private final Double executionCount; // may be null for UI (displayed as N/A
	private final Double medianDuration; // may be null for UI (displayed as N/A

	public TaskModel(
			final TaskDefinition taskDefinition,
			final Double executionCount,
			final Double medianDuration) {
		Assertion.check().isNotNull(taskDefinition);
		//---
		this.taskDefinition = taskDefinition;
		this.executionCount = executionCount;
		this.medianDuration = medianDuration;
	}

	public String getName() {
		return taskDefinition.getName();
	}

	public Double getExecutionCount() {
		return executionCount;
	}

	public Double getMedianDuration() {
		return medianDuration;
	}

}
