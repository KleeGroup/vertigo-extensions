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
import io.vertigo.core.lang.Builder;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.datamodel.task.definitions.TaskAttribute;
import io.vertigo.datamodel.task.definitions.TaskDefinition;

/**
 * Builder to build a task.
 * @author  pchretien
 */
public final class TaskBuilder implements Builder<Task> {
	private final MapBuilder<TaskAttribute, Object> taskAttributesBuilder = new MapBuilder<>();
	private final MapBuilder<String, String> contextBuilder = new MapBuilder<>();
	private final TaskDefinition taskDefinition;

	/**
	 * Constructor.
	 *
	 * @param taskDefinition the definition of the task
	 */
	TaskBuilder(final TaskDefinition taskDefinition) {
		Assertion.check().isNotNull(taskDefinition);
		//-----
		this.taskDefinition = taskDefinition;
	}

	/**
	 * adds a value to an attribute.
	 *
	 * @param attributeName the name of the attribute
	 * @param value the values
	 * @return this builder
	 */
	public TaskBuilder addValue(final String attributeName, final Object value) {
		final TaskAttribute taskAttribute = taskDefinition.getInAttribute(attributeName);
		taskAttributesBuilder.putNullable(taskAttribute, value);
		return this;
	}

	/**
	 * adds a property to the execution context.
	 *
	 * @param contextProperty the name of the context property
	 * @param value the values
	 * @return this builder
	 */
	public TaskBuilder addContextProperty(final String contextProperty, final String value) {
		contextBuilder.putNullable(contextProperty, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Task build() {
		return new Task(taskDefinition, taskAttributesBuilder.unmodifiable().build(), contextBuilder.unmodifiable().build());
	}
}
