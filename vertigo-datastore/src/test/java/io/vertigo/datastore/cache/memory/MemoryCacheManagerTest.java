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
package io.vertigo.datastore.cache.memory;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.cache.AbstractCacheManagerTest;
import io.vertigo.datastore.cache.TestCacheDefinitionProvider;

/**
 * MemoryCache Manager test class
 *
 * @author pchretien, dszniten
 */
public class MemoryCacheManagerTest extends AbstractCacheManagerTest {
	// Unit tests use abstract class methods

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withMemoryCache()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(TestCacheDefinitionProvider.class)
						.build())
				.build();
	}
}
