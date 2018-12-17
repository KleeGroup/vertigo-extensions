package io.vertigo.workflow.dao.model;

import javax.inject.Inject;

import java.util.Optional;
import io.vertigo.app.Home;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.impl.store.util.DAO;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.StoreServices;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.workflow.domain.model.WfTransitionDefinition;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class WfTransitionDefinitionDAO extends DAO<WfTransitionDefinition, java.lang.Long> implements StoreServices {

	/**
	 * Contructeur.
	 * @param storeManager Manager de persistance
	 * @param taskManager Manager de Task
	 */
	@Inject
	public WfTransitionDefinitionDAO(final StoreManager storeManager, final TaskManager taskManager) {
		super(WfTransitionDefinition.class, storeManager, taskManager);
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
	 * Execute la tache TK_FIND_NEXT_ACTIVITY.
	 * @param wfadId Long 
	 * @param name String 
	 * @return io.vertigo.workflow.domain.model.WfTransitionDefinition wfTransitionDefinition
	*/
	public io.vertigo.workflow.domain.model.WfTransitionDefinition findNextActivity(final Long wfadId, final String name) {
		final Task task = createTaskBuilder("TK_FIND_NEXT_ACTIVITY")
				.addValue("WFAD_ID", wfadId)
				.addValue("NAME", name)
				.build();
		return getTaskManager()
				.execute(task)
				.getResult();
	}

	/**
	 * Execute la tache TK_FIND_TRANSITION.
	 * @param name String 
	 * @param wfadIdTo Long (peut être null)
	 * @param wfadIdFrom Long (peut être null)
	 * @return Option de io.vertigo.workflow.domain.model.WfTransitionDefinition workflowTransitionDefinitinList
	*/
	public Optional<io.vertigo.workflow.domain.model.WfTransitionDefinition> findTransition(final String name, final Optional<Long> wfadIdTo, final Optional<Long> wfadIdFrom) {
		final Task task = createTaskBuilder("TK_FIND_TRANSITION")
				.addValue("NAME", name)
				.addValue("WFAD_ID_TO", wfadIdTo.orElse(null))
				.addValue("WFAD_ID_FROM", wfadIdFrom.orElse(null))
				.build();
		return Optional.ofNullable((io.vertigo.workflow.domain.model.WfTransitionDefinition) getTaskManager()
				.execute(task)
				.getResult());
	}

}
