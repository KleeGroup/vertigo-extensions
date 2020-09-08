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
package io.vertigo.orchestra.definitions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.orchestra.AbstractOrchestraTestCase;

/**
 * TODO : Description de la classe.
 *
 * @author mlaroche.
 */
public class DbDefinitionsTest extends AbstractOrchestraTestCase {
	@Inject

	private OrchestraDefinitionManager orchestraDefinitionManager;

	@Test
	public void testRegister() {

		//Before : 0
		Assertions.assertEquals(0, orchestraDefinitionManager.getAllProcessDefinitionsByType(ProcessType.SUPERVISED).size());

		final Map<String, String> metadatas = new HashMap<>();
		metadatas.put("test", "toto");

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestBasic", "TestBasic")
				.withMetadatas(metadatas)
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbErrorActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);
		//After :1
		Assertions.assertEquals(1, orchestraDefinitionManager.getAllProcessDefinitionsByType(ProcessType.SUPERVISED).size());

		final ProcessDefinition processDefinition2 = orchestraDefinitionManager.getProcessDefinition("ProTestBasic");
		Assertions.assertEquals(processDefinition.getName(), processDefinition2.getName());
		Assertions.assertTrue(processDefinition2.getMetadatas().containsKey("test"));
	}
	//

	@Test
	public void testUpateInitialParams() {
		//Before : 0

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestBasic", "TestBasic")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbErrorActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);
		// no initialParams
		Assertions.assertTrue(orchestraDefinitionManager.getProcessDefinition("ProTestBasic").getTriggeringStrategy().getInitialParams().isEmpty());

		orchestraDefinitionManager.updateProcessDefinitionInitialParams("ProTestBasic", Map.of("filePath", "toto/titi"));
		// with initialParams
		Assertions.assertTrue(!orchestraDefinitionManager.getProcessDefinition("ProTestBasic").getTriggeringStrategy().getInitialParams().isEmpty());
	}

	@Test
	public void testUpateProperties() {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestUpdateCron", "TestUpdateCron")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbErrorActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);
		// no initialParams
		Assertions.assertTrue(orchestraDefinitionManager.getProcessDefinition("ProTestUpdateCron").getTriggeringStrategy().getCronExpression().isEmpty());

		orchestraDefinitionManager.updateProcessDefinitionProperties("ProTestUpdateCron", Optional.of("*/15 * * * * ?"), processDefinition.getTriggeringStrategy().isMultiExecution(),
				processDefinition.getTriggeringStrategy().getRescuePeriod(),
				processDefinition.isActive());
		// with initialParams
		Assertions.assertTrue(orchestraDefinitionManager.getProcessDefinition("ProTestUpdateCron").getTriggeringStrategy().getCronExpression().isPresent());
	}
}
