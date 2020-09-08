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
package io.vertigo.orchestra.webservices;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.parsing.Parser;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.Node;
import io.vertigo.orchestra.MyNodeConfig;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.orchestra.services.execution.engine.EmptyActivityEngine;

/**
 * Test des WS Orchestra
 * @author mlaroche
 *
 */
public class OrchestraWsTest {

	private static AutoCloseableNode node;

	private final static SessionFilter loggedSessionFilter = new SessionFilter();

	static {
		//RestAsssured init
		RestAssured.port = MyNodeConfig.WS_PORT;
	}

	@BeforeAll
	public static void setUp() {
		node = new AutoCloseableNode(MyNodeConfig.configWithVega());

		final OrchestraDefinitionManager orchestraDefinitionManager = Node.getNode().getComponentSpace().resolve(OrchestraDefinitionManager.class);
		final OrchestraServices orchestraServices = Node.getNode().getComponentSpace().resolve(OrchestraServices.class);

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestBasic", "TestBasic")
				.addActivity("dumb activity", "dumb activity", EmptyActivityEngine.class)
				.build();

		final ProcessDefinition processDefinition2 = ProcessDefinition.builder("ProTestBasic2", "TestBasic2")
				.addActivity("dumb activity 2", "dumb activity", io.vertigo.orchestra.services.execution.engine.EmptyActivityEngine.class)
				.addActivity("dumb activity 3", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbErrorActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);
		orchestraServices.getScheduler().scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());
		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition2);
		orchestraServices.getScheduler().scheduleAt(processDefinition2, Instant.now(), Collections.emptyMap());

		RestAssured.registerParser("plain/text", Parser.TEXT);
		RestAssured.given()
				.filter(loggedSessionFilter)
				.get("/test/login");

	}

	@AfterAll
	public static void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	@Test
	public void testDefinitions() {
		RestAssured.given()
				.filter(loggedSessionFilter)
				.expect()
				.log()
				.all()
				.body("name", Matchers.equalTo("ProTestBasic"))
				.body("activities", Matchers.hasSize(1))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/orchestra/definitions/ProTestBasic");
	}

	@Test
	public void testSearchDefinition() {
		final Map<String, Object> body = new HashMap<>();
		body.put("criteria", "Test");
		//---
		RestAssured.given()
				.filter(loggedSessionFilter)
				.body(body)
				.expect()
				.log()
				.all()
				.body("size()", Matchers.is(2))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/orchestra/definitions/_search");
	}

}
