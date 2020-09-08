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
package io.vertigo.datastore.cache.redis;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.connectors.redis.RedisFeatures;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.cache.AbstractCacheManagerTest;
import io.vertigo.datastore.cache.TestCacheDefinitionProvider;

/**
 * RedisCache Manager test class.
 * Uses RedisConnector from {@link io.vertigo.commons.impl.connectors.redis.RedisConnector}
 *
 * @author pchretien, dszniten
 */
public class RedisCacheManagerTest extends AbstractCacheManagerTest {
	// Unit tests use abstract class methods

	/**
	 * Max nbRows to 500.
	 */
	public RedisCacheManagerTest() {
		super(500);
	}

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new RedisFeatures()
						.withJedis(
								Param.of("host", "redis-pic.part.klee.lan.net"),
								Param.of("port", "6379"),
								Param.of("database", "0"))
						.build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withRedisCache()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(TestCacheDefinitionProvider.class)
						.build())
				.build();
	}
}
