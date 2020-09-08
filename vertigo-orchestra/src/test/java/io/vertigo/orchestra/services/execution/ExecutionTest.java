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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.orchestra.AbstractOrchestraTestCase;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.domain.execution.OActivityExecution;
import io.vertigo.orchestra.domain.execution.OActivityLog;
import io.vertigo.orchestra.domain.execution.OActivityWorkspace;
import io.vertigo.orchestra.domain.execution.OProcessExecution;
import io.vertigo.orchestra.domain.planification.OProcessPlanification;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.orchestra.services.schedule.SchedulerState;
import io.vertigo.orchestra.util.monitoring.MonitoringServices;

/**
 * TODO : Description de la classe.
 *
 * @author mlaroche.
 * @version $Id$
 */
public class ExecutionTest extends AbstractOrchestraTestCase {

	@Inject
	protected OrchestraDefinitionManager orchestraDefinitionManager;
	@Inject
	protected OrchestraServices orchestraServices;

	@Inject
	private MonitoringServices monitoringServices;

	@Test
	public void clean() {
		// nothing
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void singleExecution() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestManuel", "TestManuel")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();
		// We check the save is ok
		Assertions.assertNotNull(proId);

		// We plan right now
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// test planif
		testWithTimeout(() -> checkPlanifications(proId, 0, 1, 0, 0), 2);
		// We check executions
		testWithTimeout(() -> checkExecutions(proId, 0, 0, 1, 0), 15);

	}

	private static final void testWithTimeout(final Supplier<Boolean> booleanSupplier, final long timeoutSeconds) throws InterruptedException {
		testWithTimeoutAndDelay(booleanSupplier, timeoutSeconds, 0);
	}

	private static final void testWithTimeoutAndDelay(final Supplier<Boolean> booleanSupplier, final long timeoutSeconds, final long delayInSeconds) throws InterruptedException {
		Thread.sleep(delayInSeconds * 1000);
		final Instant beginning = Instant.now();
		boolean isSuccess = false;
		while (!isSuccess && Instant.now().isBefore(beginning.plusSeconds(timeoutSeconds))) {
			Thread.sleep(500L);
			isSuccess = booleanSupplier.get();
		}
		Assertions.assertTrue(isSuccess);
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void recurrentExecution() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestScheduled", "TestScheduled")
				.withCronExpression("*/15 * * * * ?")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();
		// We check the save is ok
		Assertions.assertNotNull(proId);

		// in 35 seconds there is at leats one done and one running
		// --- We check the counts
		testWithTimeout(() -> checkExecutions(proId, 0, 1, 1, 0), 35);

	}

	/**
	 * @throws InterruptedException
	 */
	//	@Test
	//	public void recurrentMultiNodeExecution() throws InterruptedException {
	//
	//		final ProcessDefinition processDefinition = ProcessDefinition.builder("TEST RECURRENT")
	//				.withRecurrence()
	//				.withMultiExecution()
	//				.withCron("*/4 * * * * ?")
	//				.addTask("dumb activity", "io.vertigo.orchestra.execution.engine.DumbActivityEngine", false)
	//				.build();
	//
	//		orchestraDefinitionManager.createDefinition(processDefinition);
	//
	//		Thread.sleep(1000 * 60 * 5);
	//
	//	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void executionError() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestError", "TestError")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbErrorActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();
		// We check the save is ok
		Assertions.assertNotNull(proId);

		// We plan right now
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// After 10 secondes at most there is 1 process in error
		testWithTimeout(() -> checkExecutions(proId, 0, 0, 0, 1), 10);
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void twoActivities() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTest2Activities", "Test2Activities")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// After 12 seconds the process is still running
		testWithTimeoutAndDelay(() -> checkExecutions(proId, 0, 1, 0, 0), 3, 15);
		// After 12 second the process is done
		testWithTimeout(() -> checkExecutions(proId, 0, 0, 1, 0), 15);
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void twoActivitiesWithError() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTest2ActivitiesError", "Test2ActivitiesError")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbErrorActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// After 5 seconds the process is still running
		testWithTimeout(() -> checkExecutions(proId, 0, 1, 0, 0), 5);
		// After 10 second the process is in error
		testWithTimeout(() -> checkExecutions(proId, 0, 0, 0, 1), 15);
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void testWithInitialParams() throws InterruptedException {
		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestInitialParams", "TestInitialParams")
				.addInitialParam("filePath", "toto/titi")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// We wait 5 secondes to be sure that execution is running
		testWithTimeout(() -> checkExecutions(proId, 0, 1, 0, 0), 5); // We are sure that the process is running so we can continue the test safely

		final OActivityWorkspace activityWorkspace = monitoringServices
				.getActivityWorkspaceByAceId(monitoringServices.getActivityExecutionsByPreId(monitoringServices.getExecutionsByProId(proId).get(0).getPreId()).get(0).getAceId(), true);
		Assertions.assertTrue(activityWorkspace.getWorkspace().contains("filePath"));

	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void testWithInitialParamsInPlanification() throws InterruptedException {
		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestInitialParamsPlanif", "TestInitialParamsPlanif")
				.addInitialParam("filePath", "toto/titi")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		final Map<String, String> planifParams = new HashMap<>();
		planifParams.put("filePath", "tata/tutu");
		planifParams.put("planifParam", "titi");

		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), planifParams);

		// We check 3 secondes to be sure that execution is running
		testWithTimeout(() -> checkExecutions(proId, 0, 1, 0, 0), 3); // We are sure that the process is running so we can continue the test safely

		final OActivityWorkspace activityWorkspace = monitoringServices
				.getActivityWorkspaceByAceId(monitoringServices.getActivityExecutionsByPreId(monitoringServices.getExecutionsByProId(proId).get(0).getPreId()).get(0).getAceId(), true);
		Assertions.assertTrue(activityWorkspace.getWorkspace().contains("tata/tutu"));
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void testException() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestException", "TestException")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbExceptionActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		//we schedule in 0 seconds
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// After 5 second at most the process is running
		testWithTimeout(() -> checkExecutions(proId, 0, 1, 0, 0), 5);
		// After 5 seconds at most the process is in error because there is an exception after 3 seconds
		testWithTimeout(() -> checkExecutions(proId, 0, 0, 0, 1), 5);
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void testMonoExecutionMisfire() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestMultiMisfire", "TestMultiMisfire")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// We should have 1 planification triggered and 1 misfired
		testWithTimeout(() -> checkPlanifications(proId, 0, 1, 1, 0), 3);
		// We should have one execution running
		checkExecutions(proId, 0, 1, 0, 0);
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void testLog() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestLog", "TestLog")
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbLoggedActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// We wait 8 seconds at most until it's finished
		testWithTimeout(() -> checkExecutions(proId, 0, 0, 1, 0), 8); // We are sure that the process is done so we can continue the test safely

		final Optional<OActivityLog> activityLog = monitoringServices
				.getActivityLogByAceId(monitoringServices.getActivityExecutionsByPreId(monitoringServices.getExecutionsByProId(proId).get(0).getPreId()).get(0).getAceId());
		Assertions.assertTrue(activityLog.isPresent());
		Assertions.assertEquals("/testPath", activityLog.get().getAttachment());

	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiExecution() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestMulti", "TEST MULTI")
				.withMultiExecution()
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// We should have 2 planifications triggered
		testWithTimeout(() -> checkPlanifications(proId, 0, 2, 0, 0), 5);
		// We should have two executions running
		checkExecutions(proId, 0, 2, 0, 0);
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void testFinishedExecution() throws InterruptedException {

		final ProcessDefinition processDefinition = ProcessDefinition.builder("ProTestMulti", "TestMulti")
				.withMultiExecution()
				.addActivity("dumb activity finished", "dumb activity finished", io.vertigo.orchestra.services.execution.engine.DumbFinishedActivityEngine.class)
				.addActivity("dumb activity", "dumb activity", io.vertigo.orchestra.services.execution.engine.DumbActivityEngine.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final Long proId = processDefinition.getId();

		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());

		// we have one process_execution done
		testWithTimeout(() -> checkExecutions(proId, 0, 0, 1, 0), 5);
		// we have one and only one activity_execution done
		checkActivityExecutions(proId, 0, 0, 1, 0);

	}

	protected boolean checkExecutions(final Long proId, final int waitingCount, final int runningCount, final int doneCount, final int errorCount) {
		int waitingExecutionCount = 0;
		int runningExecutionCount = 0;
		int doneExecutionCount = 0;
		int errorExecutionCount = 0;

		for (final OProcessExecution processExecution : monitoringServices.getExecutionsByProId(proId)) {
			// --- We check the execution state of the process
			final DtList<OActivityExecution> activityExecutions = monitoringServices.getActivityExecutionsByPreId(processExecution.getPreId());
			int countActivitiesError = 0;
			for (final OActivityExecution activityExecution : activityExecutions) {
				switch (ExecutionState.valueOf(activityExecution.getEstCd())) {
					case WAITING:
						break;
					case RUNNING:
						break;
					case DONE:
						break;
					case ERROR:
						countActivitiesError++;
						break;
					case SUBMITTED:
					case PENDING:
					case ABORTED:
					case RESERVED:
						break;
					default:
						throw new UnsupportedOperationException("Unsupported state :" + activityExecution.getEstCd());
				}
			}
			switch (ExecutionState.valueOf(processExecution.getEstCd())) {
				case WAITING:
					waitingExecutionCount++;
					break;
				case RUNNING:
					runningExecutionCount++;
					break;
				case DONE:
					doneExecutionCount++;
					// --- We check that all activities are done if a process is done
					for (final OActivityExecution activityExecution : activityExecutions) {
						Assertions.assertEquals(ExecutionState.DONE.name(), activityExecution.getEstCd());
					}
					break;
				case ERROR:
					errorExecutionCount++;
					// --- We check that there is one and only one activity is ERROR
					Assertions.assertEquals(1, countActivitiesError);
					break;
				case SUBMITTED:
				case PENDING:
				case RESERVED:
					break;
				default:
					throw new UnsupportedOperationException("Unsupported state :" + processExecution.getEstCd());
			}
		}
		// --- We check the counts
		return waitingCount == waitingExecutionCount &&
				runningCount == runningExecutionCount &&
				doneCount == doneExecutionCount &&
				errorCount == errorExecutionCount;
	}

	protected boolean checkPlanifications(final Long proId, final int waitingCount, final int triggeredCount, final int misfiredCount, final int rescuedCount) {
		int waitingPlanificationCount = 0;
		int triggeredPlanificationCount = 0;
		int misfiredPlanificationCount = 0;
		int rescuedPlanificationCount = 0;

		for (final OProcessPlanification processPlanification : monitoringServices.getPlanificationsByProId(proId)) {
			switch (SchedulerState.valueOf(processPlanification.getSstCd())) {
				case WAITING:
					waitingPlanificationCount++;
					break;
				case TRIGGERED:
					triggeredPlanificationCount++;
					break;
				case MISFIRED:
					misfiredPlanificationCount++;
					break;
				case RESCUED:
					rescuedPlanificationCount++;
					break;
				default:
					throw new UnsupportedOperationException("Unsupported state :" + processPlanification.getSstCd());
			}
		}
		// --- We check the counts
		return waitingCount == waitingPlanificationCount &&
				triggeredCount == triggeredPlanificationCount &&
				misfiredCount == misfiredPlanificationCount &&
				rescuedCount == rescuedPlanificationCount;
	}

	protected void checkActivityExecutions(final Long proId, final int waitingCount, final int runningCount, final int doneCount, final int errorCount) {

		for (final OProcessExecution processExecution : monitoringServices.getExecutionsByProId(proId)) {
			// --- We check the execution state of the process
			final DtList<OActivityExecution> activityExecutions = monitoringServices.getActivityExecutionsByPreId(processExecution.getPreId());
			int waitingExecutionCount = 0;
			int runningExecutionCount = 0;
			int doneExecutionCount = 0;
			int errorExecutionCount = 0;
			for (final OActivityExecution activityExecution : activityExecutions) {
				switch (ExecutionState.valueOf(activityExecution.getEstCd())) {
					case WAITING:
						waitingExecutionCount++;
						break;
					case RUNNING:
						runningExecutionCount++;
						break;
					case DONE:
						doneExecutionCount++;
						break;
					case ERROR:
						errorExecutionCount++;
						break;
					case SUBMITTED:
					case PENDING:
					case ABORTED:
					default:
						throw new UnsupportedOperationException("Unsupported state :" + activityExecution.getEstCd());
				}
			}
			// --- We check the counts
			Assertions.assertEquals(waitingCount, waitingExecutionCount, "waiting ");
			Assertions.assertEquals(runningCount, runningExecutionCount, "running");
			Assertions.assertEquals(doneCount, doneExecutionCount, "done");
			Assertions.assertEquals(errorCount, errorExecutionCount, "error");

		}

	}

}
