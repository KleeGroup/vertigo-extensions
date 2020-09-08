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
package io.vertigo.datastore;

import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.datamodel.impl.task.proxy.TaskProxyMethod;
import io.vertigo.datastore.cache.CacheManager;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.entitystore.metrics.EntityMetricsProvider;
import io.vertigo.datastore.filestore.FileStoreManager;
import io.vertigo.datastore.impl.cache.CacheManagerImpl;
import io.vertigo.datastore.impl.entitystore.EntityStoreManagerImpl;
import io.vertigo.datastore.impl.filestore.FileStoreManagerImpl;
import io.vertigo.datastore.impl.kvstore.KVStoreManagerImpl;
import io.vertigo.datastore.kvstore.KVStoreManager;
import io.vertigo.datastore.plugins.cache.ehcache.EhCachePlugin;
import io.vertigo.datastore.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.datastore.plugins.cache.redis.RedisCachePlugin;
import io.vertigo.datastore.plugins.entitystore.sql.SqlEntityStorePlugin;
import io.vertigo.datastore.plugins.filestore.db.DbFileStorePlugin;
import io.vertigo.datastore.plugins.filestore.fs.FsFileStorePlugin;
import io.vertigo.datastore.plugins.filestore.fs.FsFullFileStorePlugin;
import io.vertigo.datastore.plugins.kvstore.berkeley.BerkeleyKVStorePlugin;
import io.vertigo.datastore.plugins.kvstore.delayedmemory.DelayedMemoryKVStorePlugin;

/**
 * Defines dynamo features.
 *
 * @author pchretien
 */
public final class DataStoreFeatures extends Features<DataStoreFeatures> {

	/**
	 * Constructor.
	 */
	public DataStoreFeatures() {
		super("vertigo-datastore");
	}

	/**
	 * Add store to dynamo
	 * @return  the feature
	 */
	@Feature("entitystore")
	public DataStoreFeatures withEntityStore() {
		getModuleConfigBuilder()
				.addComponent(EntityStoreManager.class, EntityStoreManagerImpl.class);
		return this;
	}

	@Feature("entitystore.sql")
	public DataStoreFeatures withSqlEntityStore(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(SqlEntityStorePlugin.class, params);
		return this;
	}

	/**
	 * Add store to dynamo
	 * @return  the feature
	 */
	@Feature("filestore")
	public DataStoreFeatures withFileStore(final Param... params) {
		getModuleConfigBuilder()
				.addComponent(FileStoreManager.class, FileStoreManagerImpl.class, params);
		return this;
	}

	@Feature("filestore.filesystem")
	public DataStoreFeatures withFsFileStore(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(FsFileStorePlugin.class, params);
		return this;
	}

	@Feature("filestore.db")
	public DataStoreFeatures withDbFileStore(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(DbFileStorePlugin.class, params);
		return this;
	}

	@Feature("filestore.fullFilesystem")
	public DataStoreFeatures withFsFullFileStore(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(FsFullFileStorePlugin.class, params);
		return this;
	}

	/**
	 * Add key/value store to dynamo
	 * @return  the feature
	 */
	@Feature("kvStore")
	public DataStoreFeatures withKVStore() {
		getModuleConfigBuilder()
				.addComponent(KVStoreManager.class, KVStoreManagerImpl.class);
		return this;
	}

	@Feature("kvStore.berkeley")
	public DataStoreFeatures withBerkleyKV(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(BerkeleyKVStorePlugin.class, params);
		return this;
	}

	@Feature("kvStore.delayedMemory")
	public DataStoreFeatures withDelayedMemoryKV(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(DelayedMemoryKVStorePlugin.class, params);
		return this;
	}

	@Feature("taskProxyMethod")
	public DataStoreFeatures withTaskProxyMethod() {
		getModuleConfigBuilder()
				.addProxyMethod(TaskProxyMethod.class);
		return this;
	}

	@Feature("entityMetrics")
	public DataStoreFeatures withEntityMetrics() {
		getModuleConfigBuilder()
				.addComponent(EntityMetricsProvider.class);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache")
	public DataStoreFeatures withCache() {
		getModuleConfigBuilder()
				.addComponent(CacheManager.class, CacheManagerImpl.class);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache.redis")
	public DataStoreFeatures withRedisCache(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(RedisCachePlugin.class, params);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache.memory")
	public DataStoreFeatures withMemoryCache() {
		getModuleConfigBuilder()
				.addPlugin(MemoryCachePlugin.class);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache.eh")
	public DataStoreFeatures withEhCache() {
		getModuleConfigBuilder()
				.addPlugin(EhCachePlugin.class);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		//nothing by default
	}
}
