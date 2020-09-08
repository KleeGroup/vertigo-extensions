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
package io.vertigo.datastore.plugins.cache.ehcache;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.ehcache.Cache;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.datastore.cache.definitions.CacheDefinition;
import io.vertigo.datastore.impl.cache.CachePlugin;

/**
 * Implémentation EHCache du CacheManager.
 *
 * @author pchretien, npiedeloup
 */
public final class EhCachePlugin implements Activeable, CachePlugin {
	private final org.ehcache.CacheManager manager;
	private final CodecManager codecManager;

	/**
	 * Constructor.
	 * @param codecManager CodecManager
	 */
	@Inject
	public EhCachePlugin(final CodecManager codecManager) {
		Assertion.check().isNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
		manager = CacheManagerBuilder.newCacheManagerBuilder().build();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		manager.init();
		registerCaches();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		manager.close();
	}

	private void registerCaches() {
		Node.getNode().getDefinitionSpace()
				.getAll(CacheDefinition.class)
				.forEach(this::registerCache);
	}

	private void registerCache(final CacheDefinition cacheDefinition) {
		final boolean overflowToDisk = cacheDefinition.shouldSerializeElements(); //don't overflow
		final ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
				.heap(cacheDefinition.getMaxElementsInMemory(), EntryUnit.ENTRIES);
		if (overflowToDisk) {
			resourcePoolsBuilder.disk(300, MemoryUnit.MB, true);
		}

		final CacheConfiguration<Serializable, Object> cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(Serializable.class, Object.class,
				resourcePoolsBuilder.build())
				.withExpiry(new ExpiryPolicy<Serializable, Object>() {

					@Override
					public java.time.Duration getExpiryForCreation(final Serializable key, final Object value) {
						return Duration.of(cacheDefinition.getTimeToLiveSeconds(), ChronoUnit.SECONDS); //time out after creation
					}

					@Override
					public java.time.Duration getExpiryForAccess(final Serializable key, final Supplier<?> value) {
						return Duration.of(cacheDefinition.getTimeToIdleSeconds(), ChronoUnit.SECONDS); //time out after an access
					}

					@Override
					public java.time.Duration getExpiryForUpdate(final Serializable key, final Supplier<?> oldValue, final Object newValue) {
						return null; // Keeping the existing expiry
					}
				})
				.build();
		manager.createCache(cacheDefinition.getName(), cacheConfiguration);
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String context, final Serializable key, final Object value) {
		Assertion.check()
				.isNotNull(value, "CachePlugin can't cache null value. (context: {0}, key:{1})", context, key)
				.isFalse((value instanceof byte[]), "CachePlugin can't cache byte[] values");
		//-----
		//On regarde la conf du cache pour vérifier s'il on serialize/clone les éléments ou non.
		if (getCacheDefinition(context).shouldSerializeElements()) {
			Assertion.check().isTrue(value instanceof Serializable,
					"Object to cache isn't Serializable. Make it unmodifiable or add it in noSerialization's plugin parameter. (context: {0}, key:{1}, class:{2})",
					context, key, value.getClass().getSimpleName());
			// Sérialisation avec compression
			final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
			//La sérialisation est équivalente à un deep Clone.
			putEH(context, key, serializedObject);
		} else {
			putEH(context, key, value);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		final Object cachedObject = getEH(context, key);
		//on ne connait pas l'état Modifiable ou non de l'objet, on se base sur son type.
		if (cachedObject instanceof byte[]) {
			final byte[] serializedObject = (byte[]) cachedObject;
			return codecManager.getCompressedSerializationCodec().decode(serializedObject);
		}
		return cachedObject;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final String context, final Serializable key) {
		getEHCache(context).remove(key);
		return getEHCache(context).get(key) == null;
	}

	/** {@inheritDoc} */
	@Override
	public void clearAll() {
		Node.getNode().getDefinitionSpace()
				.getAll(CacheDefinition.class)
				.forEach(cacheDefinition -> {
					final Cache<?, ?> cache = manager.getCache(cacheDefinition.getName(), Serializable.class, Object.class);
					if (cache != null) { //we maximized clear command, cache must exists
						cache.clear();
					}
				});
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		getEHCache(context).clear();
	}

	private void putEH(final String context, final Serializable key, final Object value) {
		getEHCache(context).put(key, value);
	}

	private Object getEH(final String context, final Serializable key) {
		return getEHCache(context).get(key);
	}

	private Cache<Serializable, Object> getEHCache(final String context) {
		final Cache<Serializable, Object> ehCache = manager.getCache(context, Serializable.class, Object.class);
		Assertion.check().isNotNull(ehCache, "Cache {0} are not yet registered. Add it into a file ehcache.xml and put it into the WEB-INF directory of your webnode.", context);
		return ehCache;
	}

	private static CacheDefinition getCacheDefinition(final String cacheName) {
		return Node.getNode().getDefinitionSpace().resolve(cacheName, CacheDefinition.class);
	}
}
