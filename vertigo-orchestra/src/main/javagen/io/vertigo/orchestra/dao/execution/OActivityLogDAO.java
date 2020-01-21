package io.vertigo.orchestra.dao.execution;

import javax.inject.Inject;

import java.util.Optional;
import io.vertigo.core.lang.Generated;
import io.vertigo.core.node.Home;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.impl.dao.DAO;
import io.vertigo.datastore.impl.dao.StoreServices;
import io.vertigo.dynamo.ngdomain.ModelManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.orchestra.domain.execution.OActivityLog;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class OActivityLogDAO extends DAO<OActivityLog, java.lang.Long> implements StoreServices {

	/**
	 * Contructeur.
	 * @param entityStoreManager Manager de persistance
	 * @param taskManager Manager de Task
	 */
	@Inject
	public OActivityLogDAO(final EntityStoreManager entityStoreManager, final TaskManager taskManager, final ModelManager modelManager) {
		super(OActivityLog.class, entityStoreManager, taskManager, modelManager);
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
	 * Execute la tache StTkGetActivityLogByAceId.
	 * @param aceId Long
	 * @return Option de OActivityLog dtcOActivityLog
	*/
	@io.vertigo.dynamo.task.proxy.TaskAnnotation(
			dataSpace = "orchestra",
			name = "TkGetActivityLogByAceId",
			request = "select acl.*" + 
 "        	from o_activity_log acl" + 
 "        	where acl.ACE_ID = #aceId#",
			taskEngineClass = io.vertigo.dynamox.task.TaskEngineSelect.class)
	@io.vertigo.dynamo.task.proxy.TaskOutput(domain = "STyDtOActivityLog")
	public Optional<io.vertigo.orchestra.domain.execution.OActivityLog> getActivityLogByAceId(@io.vertigo.dynamo.task.proxy.TaskInput(name = "aceId", domain = "STyOIdentifiant") final Long aceId) {
		final Task task = createTaskBuilder("TkGetActivityLogByAceId")
				.addValue("aceId", aceId)
				.build();
		return Optional.ofNullable((io.vertigo.orchestra.domain.execution.OActivityLog) getTaskManager()
				.execute(task)
				.getResult());
	}

	/**
	 * Execute la tache StTkGetLogByPreId.
	 * @param preId Long
	 * @return Option de OActivityLog dtActivityLog
	*/
	@io.vertigo.dynamo.task.proxy.TaskAnnotation(
			dataSpace = "orchestra",
			name = "TkGetLogByPreId",
			request = "select " + 
 "        	acl.*" + 
 "			from o_activity_execution ace" + 
 "			join o_activity_log acl on acl.ACE_ID = ace.ACE_ID" + 
 "			where ace.PRE_ID = #preId#" + 
 "			order by ace.end_time desc limit 1",
			taskEngineClass = io.vertigo.dynamox.task.TaskEngineSelect.class)
	@io.vertigo.dynamo.task.proxy.TaskOutput(domain = "STyDtOActivityLog")
	public Optional<io.vertigo.orchestra.domain.execution.OActivityLog> getLogByPreId(@io.vertigo.dynamo.task.proxy.TaskInput(name = "preId", domain = "STyOIdentifiant") final Long preId) {
		final Task task = createTaskBuilder("TkGetLogByPreId")
				.addValue("preId", preId)
				.build();
		return Optional.ofNullable((io.vertigo.orchestra.domain.execution.OActivityLog) getTaskManager()
				.execute(task)
				.getResult());
	}

}
