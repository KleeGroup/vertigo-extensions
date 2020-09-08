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
package io.vertigo.datastore.impl.entitystore;

import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.Node;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.ListVAccessor;
import io.vertigo.datastore.entitystore.EntityStoreManager;

public class StoreListVAccessor<E extends Entity> extends ListVAccessor<E> {

	private static final long serialVersionUID = -4840484505809842010L;

	public StoreListVAccessor(final Entity entity, final String associationDefinitionName, final String roleName) {
		super(entity, associationDefinitionName, roleName);
	}

	/**
	 * Loads the value if needed.
	 */
	public final void load() {
		// we are not lazy the uid of the parent might have changed
		if (getSourceUID() != null) {
			final EntityStoreManager entityStoreManager = Node.getNode().getComponentSpace().resolve(EntityStoreManager.class);
			final DtList<E> dtList = entityStoreManager.findAll(getDtListURI());
			super.set(dtList);
		} else {
			// if the uid is null we return an empty dtList
			super.set(new DtList<>(getTargetDefinitionReference().get()));
		}
	}

	@Override
	public final void set(final DtList<E> dtList) {
		throw new VSystemException("StoreListVAccessor cannot be set, you can only load it");
	}

}
