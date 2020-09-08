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
package io.vertigo.orchestra;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import io.vertigo.basics.task.TaskEngineProc;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.model.Task;
import io.vertigo.datastore.impl.dao.StoreUtil;

/**
 * Test Junit de Vertigo Orchestra.
 *
 * @author mlaroche.
 * @version $Id$
 */
public abstract class AbstractOrchestraTestCase {
	private static AutoCloseableNode node;

	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private TaskManager taskManager;

	@BeforeAll
	public static final void setUp() {
		node = new AutoCloseableNode(MyNodeConfig.config());
	}

	@AfterAll
	public static final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	public final void setUpInjection() {
		if (node != null) {
			InjectorUtil.injectMembers(this);
		}
	}

	@BeforeEach
	public void doSetUp() {
		setUpInjection();
		//A chaque test on supprime tout
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final List<String> requests = List.of(
					" delete from o_activity_log;",
					" delete from o_activity_workspace;",
					" delete from o_process_planification;",
					" delete from o_activity_execution;",
					" delete from o_process_execution;",
					" delete from o_activity;",
					" delete from o_process;");

			for (final String request : requests) {
				final TaskDefinition taskDefinition = TaskDefinition.builder("TkClean")
						.withDataSpace("orchestra")
						.withEngine(TaskEngineProc.class)
						.withRequest(request)
						.build();
				final Task task = Task.builder(taskDefinition)
						.addContextProperty("connectionName", StoreUtil.getConnectionName("orchestra")).build();
				taskManager.execute(task);
			}
			transaction.commit();
		}

	}

}
