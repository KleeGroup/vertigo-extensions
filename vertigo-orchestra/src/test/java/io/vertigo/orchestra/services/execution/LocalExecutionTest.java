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
package io.vertigo.orchestra.services.execution;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.orchestra.AbstractOrchestraTestCase;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.orchestra.services.execution.engine.TestJob;
import io.vertigo.orchestra.services.execution.engine.TestJob2;
import io.vertigo.orchestra.services.execution.engine.TestJob3;
import io.vertigo.orchestra.services.execution.engine.TestJobScheduled;

/**
 * TODO : Description de la classe.
 *
 * @author mlaroche.
 * @version $Id$
 */
public class LocalExecutionTest extends AbstractOrchestraTestCase {

	@Inject
	private OrchestraServices orchestraServices;
	@Inject
	private OrchestraDefinitionManager orchestraDefinitionManager;

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void singleExecution() throws InterruptedException {
		TestJob.reset();

		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition("ProTestUnsupervisedManual");

		// We plan right now
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// The task takes 10 secondes to run we wait 12 secondes to check the final states
		Thread.sleep(1000 * 1);
		Assertions.assertEquals(1, TestJob.getCount());

	}

	@Test
	public void twoActivities() throws InterruptedException {
		TestJob.reset();
		TestJob2.reset();

		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition("ProTestUnsupervisedManual2");

		// We plan right now
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// The task takes 10 secondes to run we wait 12 secondes to check the final states
		Thread.sleep(1000 * 1);
		Assertions.assertEquals(1, TestJob.getCount());
		Assertions.assertEquals(1, TestJob2.getCount());

	}

	@Test
	public void testParams() throws InterruptedException {
		TestJob3.reset();

		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition("ProTestUnsupervisedManual3");

		// We plan right now
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// The task takes 1 secondes to run we wait 12 secondes to check the final states
		Thread.sleep(1000 * 1);
		Assertions.assertEquals("value1", TestJob3.getParam1Value());

	}

	@Test
	public void testOverrideParamsInPlanif() throws InterruptedException {
		TestJob3.reset();

		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition("ProTestUnsupervisedManual3");

		final Map<String, String> planifParams = Map.of(
				TestJob3.PARAM_KEY_1, "overide",
				TestJob3.PARAM_KEY_2, "value2");

		// We plan right now
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), planifParams);

		// The task takes 1 secondes to run we wait 12 secondes to check the final states
		Thread.sleep(1000 * 1);
		Assertions.assertEquals("overide", TestJob3.getParam1Value());
		Assertions.assertEquals("value2", TestJob3.getParam2Value());

	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void recurrentExecution() throws InterruptedException {
		TestJobScheduled.reset();

		// process "PRO_TEST_UNSUPERVISED_RECURRENT" is scheduled with cron expression

		// The task takes 10 secondes to run we wait 10 secondes to check the final states
		Thread.sleep(1000 * 10);
		Assertions.assertEquals(2, TestJobScheduled.getCount());
	}

}
