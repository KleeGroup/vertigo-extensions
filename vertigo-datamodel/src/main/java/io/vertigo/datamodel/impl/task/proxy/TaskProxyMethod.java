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
package io.vertigo.datamodel.impl.task.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.amplifier.ProxyMethod;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.definitions.TaskDefinitionBuilder;
import io.vertigo.datamodel.task.model.Task;
import io.vertigo.datamodel.task.model.TaskBuilder;
import io.vertigo.datamodel.task.model.TaskResult;
import io.vertigo.datamodel.task.proxy.TaskContextProperty;
import io.vertigo.datamodel.task.proxy.TaskInput;
import io.vertigo.datamodel.task.proxy.TaskOutput;

public final class TaskProxyMethod implements ProxyMethod {

	@Override
	public Class<io.vertigo.datamodel.task.proxy.TaskProxyAnnotation> getAnnotationType() {
		return io.vertigo.datamodel.task.proxy.TaskProxyAnnotation.class;
	}

	private static SmartTypeDefinition resolveSmartTypeDefinition(final String smartTypeName) {
		return Node.getNode().getDefinitionSpace().resolve(smartTypeName, SmartTypeDefinition.class);
	}

	private static boolean hasOut(final Method method) {
		return !void.class.equals(method.getReturnType());
	}

	private static Cardinality getCardinality(final Class type) {
		if (Optional.class.isAssignableFrom(type)) {
			return Cardinality.OPTIONAL_OR_NULLABLE;
		} else if (List.class.isAssignableFrom(type)) {
			return Cardinality.MANY;
		} else {
			return Cardinality.ONE;
		}
	}

	private static SmartTypeDefinition findOutSmartType(final Method method) {
		final TaskOutput taskOutput = method.getAnnotation(TaskOutput.class);
		Assertion.check().isNotNull(taskOutput, "The return method '{0}' must be annotated with '{1}'", method, TaskOutput.class);
		return resolveSmartTypeDefinition(taskOutput.smartType());
	}

	private static TaskManager getTaskManager() {
		return Node.getNode().getComponentSpace().resolve(TaskManager.class);
	}

	@Override
	public Object invoke(final Method method, final Object[] args) {
		final TaskDefinition taskDefinition = createTaskDefinition(method);
		final Task task = createTask(taskDefinition, method, args);
		final TaskResult taskResult = getTaskManager().execute(task);
		if (taskDefinition.getOutAttributeOption().isPresent()) {
			return taskResult.getResult();
		}
		return Void.TYPE;
	}

	private static TaskDefinition createTaskDefinition(final Method method) {
		final io.vertigo.datamodel.task.proxy.TaskProxyAnnotation taskAnnotation = method.getAnnotation(io.vertigo.datamodel.task.proxy.TaskProxyAnnotation.class);

		final TaskDefinitionBuilder taskDefinitionBuilder = TaskDefinition.builder(taskAnnotation.name())
				.withEngine(taskAnnotation.taskEngineClass())
				.withRequest(taskAnnotation.request())
				.withDataSpace(taskAnnotation.dataSpace().isEmpty() ? null : taskAnnotation.dataSpace());

		if (hasOut(method)) {
			final SmartTypeDefinition outSmartType = findOutSmartType(method);
			final Cardinality outCardinality = getCardinality(method.getReturnType());
			taskDefinitionBuilder.withOutAttribute("out", outSmartType, outCardinality);
		}
		for (final Parameter parameter : method.getParameters()) {
			final TaskInput taskAttributeAnnotation = parameter.getAnnotation(TaskInput.class);
			final Cardinality inAttributeCardinality = getCardinality(parameter.getType());

			//test if the parameter is an optional type
			taskDefinitionBuilder.addInAttribute(
					taskAttributeAnnotation.name(),
					resolveSmartTypeDefinition(taskAttributeAnnotation.smartType()),
					inAttributeCardinality);
		}

		return taskDefinitionBuilder.build();
	}

	private static Task createTask(final TaskDefinition taskDefinition, final Method method, final Object[] args) {
		final TaskBuilder taskBuilder = Task.builder(taskDefinition);
		for (int i = 0; i < method.getParameters().length; i++) {
			final Parameter parameter = method.getParameters()[i];
			final boolean optional = Optional.class.isAssignableFrom(parameter.getType());
			final TaskInput taskAttributeAnnotation = parameter.getAnnotation(TaskInput.class);

			final Object arg;
			if (optional) {
				arg = ((Optional) args[i]).orElse(null);
			} else {
				arg = args[i];
			}
			taskBuilder.addValue(taskAttributeAnnotation.name(), arg);
		}
		Stream.of(method.getAnnotationsByType(TaskContextProperty.class))
				.forEach(taskContextProperty -> taskBuilder.addContextProperty(taskContextProperty.name(), taskContextProperty.value()));
		return taskBuilder.build();
	}
}
