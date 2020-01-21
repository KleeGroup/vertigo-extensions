package io.vertigo.orchestra.dao.execution;

import javax.inject.Inject;

import io.vertigo.core.node.Home;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Generated;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.datastore.impl.dao.StoreServices;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
 @Generated
public final class ExecutionPAO implements StoreServices {
	private final TaskManager taskManager;

	/**
	 * Constructeur.
	 * @param taskManager Manager des Task
	 */
	@Inject
	public ExecutionPAO(final TaskManager taskManager) {
		Assertion.checkNotNull(taskManager);
		//-----
		this.taskManager = taskManager;
	}

	/**
	 * Creates a taskBuilder.
	 * @param name  the name of the task
	 * @return the builder 
	 */
	private static TaskBuilder createTaskBuilder(final String name) {
		final TaskDefinition taskDefinition = Home.getApp().getDefinitionSpace().resolve(name, TaskDefinition.class);
		return Task.builder(taskDefinition);
	}

	/**
	 * Execute la tache StTkHandleDeadProcessesOfNode.
	 * @param nodId Long
	*/
	@io.vertigo.dynamo.task.proxy.TaskAnnotation(
			dataSpace = "orchestra",
			name = "TkHandleDeadProcessesOfNode",
			request = "update o_activity_execution " + 
 "        	set EST_CD = 'ABORTED'" + 
 "        	where ACE_ID in (" + 
 "				select ace.ACE_ID" + 
 "				from o_activity_execution ace" + 
 "				where ace.EST_CD in ('RUNNING','WAITING','SUBMITTED') and ace.NOD_ID = #nodId#);" + 
 "			" + 
 " 			update o_process_execution" + 
 "        	set EST_CD = 'ABORTED'" + 
 "        	where PRE_ID in (" + 
 "				select pre.PRE_ID" + 
 "				from o_process_execution pre" + 
 "				join o_activity_execution ace on ace.PRE_ID = pre.PRE_ID" + 
 "				where ace.EST_CD = 'ABORTED');",
			taskEngineClass = io.vertigo.dynamox.task.TaskEngineProc.class)
	public void handleDeadProcessesOfNode(@io.vertigo.dynamo.task.proxy.TaskInput(name = "nodId", domain = "STyOIdentifiant") final Long nodId) {
		final Task task = createTaskBuilder("TkHandleDeadProcessesOfNode")
				.addValue("nodId", nodId)
				.build();
		getTaskManager().execute(task);
	}

	/**
	 * Execute la tache StTkHandleProcessesOfDeadNodes.
	 * @param maxDate Instant
	*/
	@io.vertigo.dynamo.task.proxy.TaskAnnotation(
			dataSpace = "orchestra",
			name = "TkHandleProcessesOfDeadNodes",
			request = "update o_activity_execution " + 
 "        	set EST_CD = 'ABORTED'" + 
 "        	where ACE_ID in (" + 
 "				select ace.ACE_ID" + 
 "				from o_activity_execution ace" + 
 "				join o_node nod on nod.NOD_ID = ace.NOD_ID" + 
 "				where ace.EST_CD in ('RUNNING','WAITING','SUBMITTED') and nod.HEARTBEAT < #maxDate#);" + 
 "			" + 
 " 			update o_process_execution" + 
 "        	set EST_CD = 'ABORTED'" + 
 "        	where PRE_ID in (" + 
 "				select pre.PRE_ID" + 
 "				from o_process_execution pre" + 
 "				join o_activity_execution ace on ace.PRE_ID = pre.PRE_ID" + 
 "				where ace.EST_CD = 'ABORTED');",
			taskEngineClass = io.vertigo.dynamox.task.TaskEngineProc.class)
	public void handleProcessesOfDeadNodes(@io.vertigo.dynamo.task.proxy.TaskInput(name = "maxDate", domain = "STyOTimestamp") final java.time.Instant maxDate) {
		final Task task = createTaskBuilder("TkHandleProcessesOfDeadNodes")
				.addValue("maxDate", maxDate)
				.build();
		getTaskManager().execute(task);
	}

	/**
	 * Execute la tache StTkReserveActivitiesToLaunch.
	 * @param nodId Long
	 * @param maxNumber Integer
	*/
	@io.vertigo.dynamo.task.proxy.TaskAnnotation(
			dataSpace = "orchestra",
			name = "TkReserveActivitiesToLaunch",
			request = "update  o_activity_execution " + 
 "        	set EST_CD = 'RESERVED'," + 
 "        		NOD_ID = #nodId#" + 
 "        		" + 
 "        	where ace_id in (" + 
 "        			select ace_id " + 
 "        			from o_activity_execution" + 
 "        			where EST_CD = 'WAITING' " + 
 "        			order by creation_time asc" + 
 "        			limit #maxNumber#" + 
 "        	)",
			taskEngineClass = io.vertigo.dynamox.task.TaskEngineProc.class)
	public void reserveActivitiesToLaunch(@io.vertigo.dynamo.task.proxy.TaskInput(name = "nodId", domain = "STyOIdentifiant") final Long nodId, @io.vertigo.dynamo.task.proxy.TaskInput(name = "maxNumber", domain = "STyONombre") final Integer maxNumber) {
		final Task task = createTaskBuilder("TkReserveActivitiesToLaunch")
				.addValue("nodId", nodId)
				.addValue("maxNumber", maxNumber)
				.build();
		getTaskManager().execute(task);
	}

	/**
	 * Execute la tache StTkUpdateProcessExecutionTreatment.
	 * @param preId Long
	 * @param checked Boolean
	 * @param checkingDate Instant
	 * @param checkingComment String
	*/
	@io.vertigo.dynamo.task.proxy.TaskAnnotation(
			dataSpace = "orchestra",
			name = "TkUpdateProcessExecutionTreatment",
			request = "update o_process_execution" + 
 "        	set CHECKED = #checked# ," + 
 "        		CHECKING_DATE = #checkingDate#," + 
 "        		CHECKING_COMMENT = #checkingComment#" + 
 "        		where PRE_ID = #preId#",
			taskEngineClass = io.vertigo.dynamox.task.TaskEngineProc.class)
	public void updateProcessExecutionTreatment(@io.vertigo.dynamo.task.proxy.TaskInput(name = "preId", domain = "STyOIdentifiant") final Long preId, @io.vertigo.dynamo.task.proxy.TaskInput(name = "checked", domain = "STyOBooleen") final Boolean checked, @io.vertigo.dynamo.task.proxy.TaskInput(name = "checkingDate", domain = "STyOTimestamp") final java.time.Instant checkingDate, @io.vertigo.dynamo.task.proxy.TaskInput(name = "checkingComment", domain = "STyOText") final String checkingComment) {
		final Task task = createTaskBuilder("TkUpdateProcessExecutionTreatment")
				.addValue("preId", preId)
				.addValue("checked", checked)
				.addValue("checkingDate", checkingDate)
				.addValue("checkingComment", checkingComment)
				.build();
		getTaskManager().execute(task);
	}

	private TaskManager getTaskManager() {
		return taskManager;
	}
}
