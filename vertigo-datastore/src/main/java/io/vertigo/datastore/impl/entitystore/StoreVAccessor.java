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

import io.vertigo.core.node.Node;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.VAccessor;
import io.vertigo.datastore.entitystore.EntityStoreManager;

/**
 * This class is a way to access an entity defined by a relationship.
 * It's a kind of box (aka optional) that offers a small list of methods.
 *
 * @author pchretien
 *
 * @param <E> the type of entity
 */
public class StoreVAccessor<E extends Entity> extends VAccessor<E> {

	private static final long serialVersionUID = -3620065166718209361L;

	/**
	 * Constructor.
	 * @param clazz the entity class
	 * @param role the role of the association (case of multiple associations with the same entity)
	 * @param loadFunction function that need to be applied to retrieve the targeted entity
	 */
	public StoreVAccessor(final Class<E> clazz, final String role) {
		super(clazz, role);
	}

	/**
	 * Loads the value if needed.
	 */
	public void load() {
		if (getUID() != null) {
			final EntityStoreManager entityStoreManager = Node.getNode().getComponentSpace().resolve(EntityStoreManager.class);
			final E value = entityStoreManager.readOne(getUID());
			set(value);
		}
	}

}
