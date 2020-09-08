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
package io.vertigo.datastore.impl.dao;

import io.vertigo.core.node.Node;
import io.vertigo.datastore.entitystore.EntityStoreManager;

public final class StoreUtil {

	protected StoreUtil() {
		// util
	}

	public static String getConnectionName(final String dataSpace) {
		return Node.getNode().getComponentSpace().resolve(EntityStoreManager.class).getDataStoreConfig().getConnectionName(dataSpace);
	}
}
