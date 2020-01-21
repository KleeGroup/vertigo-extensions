package io.vertigo.orchestra.dao.referential;

import javax.inject.Inject;

import io.vertigo.core.lang.Generated;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.impl.dao.DAO;
import io.vertigo.datastore.impl.dao.StoreServices;
import io.vertigo.dynamo.ngdomain.ModelManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.orchestra.domain.referential.OProcessType;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class OProcessTypeDAO extends DAO<OProcessType, java.lang.String> implements StoreServices {

	/**
	 * Contructeur.
	 * @param entityStoreManager Manager de persistance
	 * @param taskManager Manager de Task
	 */
	@Inject
	public OProcessTypeDAO(final EntityStoreManager entityStoreManager, final TaskManager taskManager, final ModelManager modelManager) {
		super(OProcessType.class, entityStoreManager, taskManager, modelManager);
	}

}
