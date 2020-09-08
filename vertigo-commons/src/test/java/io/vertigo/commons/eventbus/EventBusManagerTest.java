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
package io.vertigo.commons.eventbus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.eventbus.data.BlueColorEvent;
import io.vertigo.commons.eventbus.data.DummyEvent;
import io.vertigo.commons.eventbus.data.MySubscriber;
import io.vertigo.commons.eventbus.data.RedColorEvent;
import io.vertigo.commons.eventbus.data.WhiteColorEvent;
import io.vertigo.commons.eventbus.data.aspects.FlipAspect;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

/**
 * @author pchretien
 */
public final class EventBusManagerTest {

	@Inject
	private EventBusManager eventBusManager;

	@Inject
	private MySubscriber mySubscriber;
	private int deadEvents = 0;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		eventBusManager.registerDead(event -> deadEvents++);
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(ModuleConfig.builder("myAspects")
						.addAspect(FlipAspect.class)
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(MySubscriber.class)
						.build())
				.build();
	}

	@Test
	public void testSimple() {
		assertEquals(0, mySubscriber.getBlueCount());
		assertEquals(0, mySubscriber.getRedCount());
		assertEquals(0, mySubscriber.getCount());

		eventBusManager.post(new BlueColorEvent());
		eventBusManager.post(new WhiteColorEvent());
		eventBusManager.post(new RedColorEvent());
		eventBusManager.post(new RedColorEvent());

		assertEquals(1, mySubscriber.getBlueCount());
		assertEquals(2, mySubscriber.getRedCount());
		assertEquals(4, mySubscriber.getCount());

		assertEquals(0, deadEvents);
	}

	@Test
	public void testWithAspects() {
		/*
		 * We want to check that aspects are used.
		 */
		Assertions.assertTrue(FlipAspect.isOff());

		eventBusManager.post(new BlueColorEvent()); //<< Flip here
		Assertions.assertTrue(FlipAspect.isOn());

		eventBusManager.post(new RedColorEvent()); //there is no aspect
		Assertions.assertTrue(FlipAspect.isOn());

		eventBusManager.post(new BlueColorEvent()); //<< Flip here
		Assertions.assertTrue(FlipAspect.isOff());

		assertEquals(0, deadEvents);
	}

	@Test
	public void testDeadEvent() {
		assertEquals(0, deadEvents);
		eventBusManager.post(new DummyEvent());
		assertEquals(1, deadEvents);
	}
}
