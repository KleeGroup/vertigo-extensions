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

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.cache.CacheManager;
import io.vertigo.datastore.entitystore.EntityStoreConfig;
import io.vertigo.datastore.impl.entitystore.cache.CacheDataStoreConfig;
import io.vertigo.datastore.impl.entitystore.logical.LogicalEntityStoreConfig;

/**
 * Implémentation Standard du StoreProvider.
 *
 * @author pchretien
 */
public final class EntityStoreConfigImpl implements EntityStoreConfig {
	private final CacheDataStoreConfig cacheStoreConfig;
	private final LogicalEntityStoreConfig logicalDataStoreConfig;

	/**
	 * Constructor.
	 * @param dataStorePlugins DataStorePlugins list
	 * @param cacheManager Manager de gestion du cache
	 */
	public EntityStoreConfigImpl(final List<EntityStorePlugin> dataStorePlugins, final CacheManager cacheManager) {
		Assertion.check()
				.isNotNull(dataStorePlugins)
				.isNotNull(cacheManager);
		//-----
		cacheStoreConfig = new CacheDataStoreConfig(cacheManager);
		logicalDataStoreConfig = new LogicalEntityStoreConfig(dataStorePlugins);
	}

	/**
	 * @return Data store config
	 */
	public CacheDataStoreConfig getCacheStoreConfig() {
		return cacheStoreConfig;
	}

	/**
	 * @return logical data store config
	 */
	public LogicalEntityStoreConfig getLogicalStoreConfig() {
		return logicalDataStoreConfig;
	}

	/** {@inheritDoc} */
	@Override
	public String getConnectionName(final String dataSpace) {
		return logicalDataStoreConfig.getConnectionName(dataSpace);
	}
}
