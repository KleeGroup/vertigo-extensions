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
package io.vertigo.datastore.plugins.cache.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.datastore.cache.definitions.CacheDefinition;
import io.vertigo.datastore.impl.cache.CachePlugin;

/**
 * Implémentation MapCache du plugins.
 *
 * @author npiedeloup
 */
public final class MemoryCachePlugin implements Activeable, CachePlugin {
	private final CodecManager codecManager;
	private final Map<String, MemoryCache> cachesPerContext = new HashMap<>();

	/**
	 * Constructor.
	 * @param codecManager Manager des mécanismes de codage/décodage.
	 */
	@Inject
	public MemoryCachePlugin(final CodecManager codecManager) {
		Assertion.check().isNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		registerCaches();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		cachesPerContext.clear();
	}

	private void registerCaches() {
		Node.getNode().getDefinitionSpace()
				.getAll(CacheDefinition.class)
				.forEach(this::registerCache);
	}

	private void registerCache(final CacheDefinition cacheDefinition) {
		if (!cachesPerContext.containsKey(cacheDefinition.getName())) {
			final MemoryCache cache = new MemoryCache(cacheDefinition.getName(), cacheDefinition.getTimeToLiveSeconds());
			cachesPerContext.put(cache.getName(), cache);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String context, final Serializable key, final Object value) {
		Assertion.check()
				.isNotNull(value, "CachePlugin can't cache null value. (context: {0}, key:{1})", context, key)
				.isFalse((value instanceof byte[]), "Ce CachePlugin ne permet pas de mettre en cache des byte[].");
		//-----
		//On regarde la conf du cache pour vérifier s'il on serialize/clone les éléments ou non.
		if (getCacheDefinition(context).shouldSerializeElements()) {
			Assertion.check().isTrue(value instanceof Serializable,
					"Object to cache isn't Serializable. Make it Serializable or change its CacheConfig 'serializeElement' parameter. (context: {0}, key:{1}, class:{2})",
					context, key, value.getClass().getSimpleName());
			// Sérialisation avec compression
			final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
			//La sérialisation est équivalente à un deep Clone.
			putElement(context, key, serializedObject);
		} else {
			//on fait un cache mémoire :
			// - adapté si l'élément est non modifiable
			// - bcp plus performant
			putElement(context, key, value);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		final Object cachedObject = getElement(context, key);
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
		return getMapCache(context).remove(key);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void clearAll() {
		for (final MemoryCache mapCache : cachesPerContext.values()) {
			mapCache.removeAll();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		//Dans le cas de clear
		final MemoryCache mapCache = cachesPerContext.get(context);
		if (mapCache != null) {
			mapCache.removeAll();
		}
	}

	private void putElement(final String context, final Serializable key, final Object value) {
		getMapCache(context).put(key, value);
	}

	private Object getElement(final String context, final Serializable key) {
		return getMapCache(context).get(key);
	}

	private synchronized MemoryCache getMapCache(final String context) {
		final MemoryCache mapCache = cachesPerContext.get(context);
		Assertion.check().isNotNull(mapCache, "Cache {0} are not yet registered.", context);
		return mapCache;
	}

	private static CacheDefinition getCacheDefinition(final String cacheName) {
		return Node.getNode().getDefinitionSpace().resolve(cacheName, CacheDefinition.class);
	}
}
