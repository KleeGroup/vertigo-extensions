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
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;

import io.vertigo.orchestra.AbstractOrchestraTestCase;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.orchestra.services.report.ActivityExecution;
import io.vertigo.orchestra.services.report.ExecutionSummary;

/**
 * TODO : Description de la classe.
 *
 * @author mlaroche.
 * @version $Id$
 */
public class ReportingTest extends AbstractOrchestraTestCase {

	@Inject
	private OrchestraServices orchestraServices;
	@Inject
	private OrchestraDefinitionManager orchestraDefinitionManager;
	@Inject
	private OrchestraServices processExecutionManager;

	private ProcessDefinition executeProcess() throws InterruptedException {
		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestSingle", "TEST_SINGLE")
				.addActivity("DUMB ACTIVITY", "DUMB ACTIVITY", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();
		// We check the save is ok
		Assertions.assertNotNull(proId);

		// We plan right now
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// The task takes 10 secondes to run we wait 12 secondes to check the final states
		Thread.sleep(1000 * 12);
		return processDefinition;
	}

	/**
	 * @throws InterruptedException
	 */
	//@Test
	public void testReport() throws InterruptedException {

		final ProcessDefinition processDefinition = executeProcess();
		// -1h +1h
		final ExecutionSummary executionSummary = processExecutionManager.getReport().getSummaryByDate(processDefinition,
				Instant.now().minus(1, ChronoUnit.HOURS),
				Instant.now().plus(1, ChronoUnit.HOURS));
		Assertions.assertTrue(1 == executionSummary.getSuccessfulCount());
	}

	/**
	 * @throws InterruptedException
	 */
	//@Test
	public void testLog() throws InterruptedException {
		final ProcessDefinition processDefinition = executeProcess();
		// ---
		final ActivityExecution activityExecution = processExecutionManager.getReport()
				.getActivityExecutionsByProcessExecution(processExecutionManager.getReport().getProcessExecutions(processDefinition, "", 10, 0).get(0).getPreId()).get(0);
		Assertions.assertTrue(processExecutionManager.getLogger().getActivityLogFile(activityExecution.getAceId()).isPresent());
	}

}
