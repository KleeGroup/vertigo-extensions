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
import static io.vertigo.datamodel.task.TaskEngineMock2.ATTR_IN_INTEGERS;

import java.util.Arrays;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.task.data.TestSmartTypes;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.model.Task;

/**
 * This class tests the usage of a task from its registration to its execution.
 * @author dchallas
 */
public final class TaskManagerTest {

	@Inject
	private TaskManager taskManager;
	@Inject
	private TestComponentTaskAnnotation testComponentTaskAnnotation;

	private AutoCloseableNode node;

	@BeforeEach
	public void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DataModelFeatures().build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(TestComponentTaskAnnotation.class)
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
								.build())
						.addDefinitionProvider(TaskDefinitionProvider.class)
						.build())
				.build();
	}

	private TaskDefinition getTaskDefinition(final String taskDefinitionName) {
		final DefinitionSpace definitionSpace = node.getDefinitionSpace();
		return definitionSpace.resolve(taskDefinitionName, TaskDefinition.class);
	}

	/**
	 * Checks if the task-definition is registered.
	 */
	@Test
	public void testRegistry() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_ADDITION);
		Assertions.assertNotNull(taskDefinition);
	}

	/**
	 * Checks when the task-definition is not registered (an exception must be thrown).
	 */
	@Test
	public void testRegistryWithNull() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final DefinitionSpace definitionSpace = node.getDefinitionSpace();
			//L'appel à la résolution doit remonter une assertion
			final TaskDefinition taskDefinition = definitionSpace.resolve(null, TaskDefinition.class);
			Assertions.assertNotNull(taskDefinition);
		});
	}

	/***
	 * Checks the use case of an addition  with several inputs and an output
	 */
	@Test
	public void testExecuteAdd() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_ADDITION);
		Assertions.assertEquals(Integer.valueOf(10), executeTask(taskDefinition, 5, 2, 3));
	}

	/***
	 * Checks the use case of an multiplication with several inputs and an output
	 */
	@Test
	public void testExecuteMulti() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_MULTIPLICATION);
		Assertions.assertEquals(Integer.valueOf(30), executeTask(taskDefinition, 5, 2, 3));
	}

	/**
	 * Checks that an exception is thrown
	 * when a null is given to a required task
	 */
	@Test
	public void testExecuteNull() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_MULTIPLICATION);
			//on vérifie que le passage d'un paramètre null déclenche une assertion
			executeTask(taskDefinition, null, 2, 3);
		});
	}

	/**
	 * Checks that an exception is thrown
	 * when a task is executed twice
	 */
	@Test
	public void testExecuteAddAdd() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_ADDITION);

		final Task task = Task.builder(taskDefinition)
				.addValue(ATTR_IN_INT_1, 1)
				.addValue(ATTR_IN_INT_2, 8)
				.addValue(ATTR_IN_INT_3, 7)
				.build();

		final Integer result1 = taskManager
				.execute(task)
				.getResult();

		Assertions.assertEquals(Integer.valueOf(16), result1);

		final Integer result2 = taskManager
				.execute(task)
				.getResult();

		Assertions.assertEquals(Integer.valueOf(16), result2);
	}

	/**
	 * @param value1 value 1
	 * @param value2 value 2
	 * @param value3 value 3
	 * @return the addition of all these values.
	 */
	private Integer executeTask(final TaskDefinition taskDefinition, final Integer value1, final Integer value2, final Integer value3) {
		final Task task = Task.builder(taskDefinition)
				.addValue(ATTR_IN_INT_1, value1)
				.addValue(ATTR_IN_INT_2, value2)
				.addValue(ATTR_IN_INT_3, value3)
				.build();

		return taskManager
				.execute(task)
				.getResult();
	}

	/***
	 * Checks the use case of an addition  with one input and an output
	 * the input is composed with a List.
	 */
	@Test
	public void testExecuteAdd2() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_ADDITION_2);
		Assertions.assertEquals(Integer.valueOf(10), executeTask2(taskDefinition, 5, 2, 3));
	}

	/***
	 * Checks the use case of an multiplication with one input and an output
	 * the input is composed with a List.
	 */
	@Test
	public void testExecuteMulti2() {
		final TaskDefinition taskDefinition = getTaskDefinition(TaskDefinitionProvider.TK_MULTIPLICATION_2);
		Assertions.assertEquals(Integer.valueOf(30), executeTask2(taskDefinition, 5, 2, 3));
	}

	/***
	 * Checks the use case of an multiplication with one input and an output
	 * the input is composed with a List.
	 */
	@Test
	public void testExecuteMulti2Annotation() {
		Assertions.assertEquals(Integer.valueOf(30), testComponentTaskAnnotation.multiply(Arrays.asList(5, 2, 3)));
	}

	private Integer executeTask2(final TaskDefinition taskDefinition, final Integer... values) {
		final Task task = Task.builder(taskDefinition)
				.addValue(ATTR_IN_INTEGERS, Arrays.asList(values))
				.build();

		return taskManager
				.execute(task)
				.getResult();
	}

}
