package io.vertigo.workflow.dao.model;

import javax.inject.Inject;

import io.vertigo.dynamo.impl.store.util.DAO;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.StoreServices;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.workflow.domain.model.WfMultiplicityDefinition;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class WfMultiplicityDefinitionDAO extends DAO<WfMultiplicityDefinition, java.lang.String> implements StoreServices {

	/**
	 * Contructeur.
	 * @param storeManager Manager de persistance
	 * @param taskManager Manager de Task
	 */
	@Inject
	public WfMultiplicityDefinitionDAO(final StoreManager storeManager, final TaskManager taskManager) {
		super(WfMultiplicityDefinition.class, storeManager, taskManager);
	}

}
