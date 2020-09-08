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
package io.vertigo.datastore.impl.entitystore.logical;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datastore.impl.entitystore.EntityStorePlugin;

/**
 * This class defines how the dataSpaces are mapped to the physical stores.
 * A dataSpace is a set of collections.
 * A dataSpace has a name.
 * A dataSpace can have one dataStore.
 *
 * @author pchretien, npiedeloup
 */
public final class LogicalEntityStoreConfig {
	/**
	 * Map (collection-dataStorePlugin).
	 * This map defines the dataStore for each collection */
	private final Map<String, EntityStorePlugin> dataStorePluginsMap;

	/**
	 * Constructor.
	 * @param dataStorePlugins DataStore plugins
	 */
	public LogicalEntityStoreConfig(final List<EntityStorePlugin> dataStorePlugins) {
		Assertion.check().isNotNull(dataStorePlugins);
		//-----
		final Map<String, EntityStorePlugin> pluginsMap = new HashMap<>();
		for (final EntityStorePlugin dataStorePlugin : dataStorePlugins) {
			final String dataSpace = dataStorePlugin.getDataSpace();
			final EntityStorePlugin previous = pluginsMap.put(dataSpace, dataStorePlugin);
			Assertion.check().isNull(previous, "this dataSpace {0} is already registered", dataSpace);
		}
		dataStorePluginsMap = Collections.unmodifiableMap(pluginsMap);
	}

	/**
	 * Provides a 'DataStorePlugin' for the specified 'DtDefinition'.
	 * Each DtDefinition is mapped to a collection.
	 * @param dtDefinition the DtDefinition
	 * @return the dataStore used for the specified 'DtDefinition'
	 */
	public EntityStorePlugin getPhysicalDataStore(final DtDefinition dtDefinition) {
		Assertion.check().isNotNull(dtDefinition);
		//-----
		return getDataStorePlugin(dtDefinition.getDataSpace());
	}

	/**
	 * Provides the name of the connection.
	 * @param dataSpace the dataSpace
	 * @return the name of the connection
	 */
	public String getConnectionName(final String dataSpace) {
		Assertion.check().isNotBlank(dataSpace);
		//-----
		return getDataStorePlugin(dataSpace).getConnectionName();
	}

	private EntityStorePlugin getDataStorePlugin(final String dataSpace) {
		Assertion.check().isNotBlank(dataSpace);
		//-----
		final EntityStorePlugin dataStore = dataStorePluginsMap.get(dataSpace);
		Assertion.check().isNotNull(dataStore, "No store found mapped to collection '{0}'", dataSpace);
		return dataStore;
	}
}
