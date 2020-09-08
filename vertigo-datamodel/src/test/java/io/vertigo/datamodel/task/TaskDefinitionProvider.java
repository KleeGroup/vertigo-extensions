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
package io.vertigo.datamodel.task;

import static io.vertigo.datamodel.task.TaskEngineMock.ATTR_IN_INT_1;
import static io.vertigo.datamodel.task.TaskEngineMock.ATTR_IN_INT_2;
import static io.vertigo.datamodel.task.TaskEngineMock.ATTR_IN_INT_3;
import static io.vertigo.datamodel.task.TaskEngineMock.ATTR_OUT;
import static io.vertigo.datamodel.task.TaskEngineMock2.ATTR_IN_INTEGERS;

import java.util.List;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.task.definitions.TaskDefinition;

public final class TaskDefinitionProvider implements SimpleDefinitionProvider {
	public static String TK_MULTIPLICATION = "TkMultiplication";
	public static String TK_ADDITION = "TkAddition";

	public static String TK_MULTIPLICATION_2 = "TkMultiplication2";
	public static String TK_ADDITION_2 = "TkAddition2";

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return List.of(
				buildTaskDefinition(definitionSpace, TK_MULTIPLICATION, "*"),
				buildTaskDefinition(definitionSpace, TK_ADDITION, "+"),
				buildTaskDefinition2(definitionSpace, TK_MULTIPLICATION_2, "*"),
				buildTaskDefinition2(definitionSpace, TK_ADDITION_2, "+"));
	}

	private TaskDefinition buildTaskDefinition(
			final DefinitionSpace definitionSpace,
			final String taskDefinitionName,
			final String params) {
		final SmartTypeDefinition smartTypeInteger = definitionSpace.resolve("STyInteger", SmartTypeDefinition.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineMock.class)
				.withRequest(params)
				.withPackageName(TaskEngineMock.class.getPackage().getName())
				.addInAttribute(ATTR_IN_INT_1, smartTypeInteger, Cardinality.ONE)
				.addInAttribute(ATTR_IN_INT_2, smartTypeInteger, Cardinality.ONE)
				.addInAttribute(ATTR_IN_INT_3, smartTypeInteger, Cardinality.ONE)
				.withOutAttribute(ATTR_OUT, smartTypeInteger, Cardinality.ONE)
				.build();
	}

	private TaskDefinition buildTaskDefinition2(
			final DefinitionSpace definitionSpace,
			final String taskDefinitionName,
			final String params) {
		final SmartTypeDefinition smartTypeInteger = definitionSpace.resolve("STyInteger", SmartTypeDefinition.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineMock2.class)
				.withRequest(params)
				.withPackageName(TaskEngineMock.class.getPackage().getName())
				.addInAttribute(ATTR_IN_INTEGERS, smartTypeInteger, Cardinality.MANY)
				.withOutAttribute(ATTR_OUT, smartTypeInteger, Cardinality.ONE)
				.build();
	}
}
