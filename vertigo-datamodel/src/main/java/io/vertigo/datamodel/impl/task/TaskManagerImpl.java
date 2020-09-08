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
package io.vertigo.datamodel.impl.task;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.lang.Tuple;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.core.util.Selector;
import io.vertigo.core.util.Selector.MethodConditions;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.definitions.TaskDefinitionBuilder;
import io.vertigo.datamodel.task.model.Task;
import io.vertigo.datamodel.task.model.TaskEngine;
import io.vertigo.datamodel.task.model.TaskResult;
import io.vertigo.datamodel.task.proxy.TaskAnnotation;
import io.vertigo.datamodel.task.proxy.TaskInput;
import io.vertigo.datamodel.task.proxy.TaskOutput;

/**
 * @author pchretien
 */
public final class TaskManagerImpl implements TaskManager, SimpleDefinitionProvider {
	private final AnalyticsManager analyticsManager;

	/**
	 * @param analyticsManager Manager analytics
	 */
	@Inject
	public TaskManagerImpl(final AnalyticsManager analyticsManager) {
		Assertion.check().isNotNull(analyticsManager);
		//-----
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public TaskResult execute(final Task task) {
		return analyticsManager
				.traceWithReturn(
						"tasks",
						"/execute/" + task.getDefinition().getName(),
						tracer -> doExecute(task));
	}

	private static TaskResult doExecute(final Task task) {
		final TaskEngine taskEngine = InjectorUtil.newInstance(task.getDefinition().getTaskEngineClass());
		return taskEngine.process(task);
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		final List<Class> componenentClasses = Node.getNode().getComponentSpace().keySet()
				.stream()
				.map(componentId -> Node.getNode().getComponentSpace().resolve(componentId, Object.class).getClass())
				.collect(Collectors.toList());

		return Selector
				.from(componenentClasses)
				.filterMethods(MethodConditions.annotatedWith(TaskAnnotation.class))
				.findMethods()
				.stream()
				.map(Tuple::getVal2)
				.map(TaskManagerImpl::createTaskDefinition)
				.collect(Collectors.toList());
	}

	private static TaskDefinition createTaskDefinition(final Method method) {
		final io.vertigo.datamodel.task.proxy.TaskAnnotation taskAnnotation = method.getAnnotation(io.vertigo.datamodel.task.proxy.TaskAnnotation.class);

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
}
