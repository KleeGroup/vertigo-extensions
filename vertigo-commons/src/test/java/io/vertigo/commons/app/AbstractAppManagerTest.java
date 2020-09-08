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
package io.vertigo.commons.app;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.Node;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.NodeConfigBuilder;

public abstract class AbstractAppManagerTest {
	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	protected abstract NodeConfig buildNodeConfig();

	protected NodeConfigBuilder buildRootNodeConfig() {
		return NodeConfig.builder()
				.withAppName("nodeTestApp")
				.withNodeId("nodeTest1")
				.addModule(ModuleConfig.builder("db")
						.build());
	}

	@Test
	void testRegisterNode() {
		final AppManager nodeManager = Node.getNode().getComponentSpace().resolve(AppManager.class);

		final List<AppNode> nodesWithDbSkill = nodeManager.locateSkills("db");
		final List<AppNode> nodesWithOtherSkill = nodeManager.locateSkills("other");

		// ---
		Assertions.assertEquals(1, nodesWithDbSkill.size());
		Assertions.assertEquals(0, nodesWithOtherSkill.size());

	}

	@Disabled // ignored for now we need heartbeat of node update to be parametized for shorter tests
	void testUpdate() throws InterruptedException {
		final AppManager nodeManager = Node.getNode().getComponentSpace().resolve(AppManager.class);
		// ---
		final Instant firstTouch = nodeManager.find("nodeTest1").get().getLastTouch();
		Thread.sleep(7 * 1000L);
		final Instant secondTouch = nodeManager.find("nodeTest1").get().getLastTouch();
		// ---
		Assertions.assertTrue(secondTouch.isAfter(firstTouch));

	}

}
