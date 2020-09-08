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
package io.vertigo.datastore.plugins.cache.redis;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.param.ParamValue;
import io.vertigo.datastore.cache.definitions.CacheDefinition;
import io.vertigo.datastore.impl.cache.CachePlugin;
import redis.clients.jedis.Jedis;

/**
 * RedisCache plugin
 *
 * @author pchretien, dszniten
 */
public class RedisCachePlugin implements CachePlugin {

	private final CodecManager codecManager;
	private final RedisConnector redisConnector;

	private static final String VERTIGO_CACHE = "vertigo:cache";
	private static final String DELETE_KEYS_ON_PATTERN_SCRIPT = "local keys = redis.call('keys', '%s') for i,k in ipairs(keys) do local res = redis.call('del', k) end";

	/**
	 * Constructor.
	 * @param codecManager  the codecManager
	 * @param redisConnector the redis connector
	 */
	@Inject
	public RedisCachePlugin(
			@ParamValue("connectorName") final Optional<String> connectorNameOpt,
			final List<RedisConnector> redisConnectors,
			final CodecManager codecManager) {
		Assertion.check()
				.isNotNull(connectorNameOpt)
				.isNotNull(redisConnectors)
				.isNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
		final String connectorName = connectorNameOpt.orElse("main");
		redisConnector = redisConnectors.stream()
				.filter(connector -> connectorName.equals(connector.getName()))
				.findFirst().get();
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String context, final Serializable key, final Object value) {
		Assertion.check()
				.isNotNull(value, "CachePlugin can't cache null value. (context: {0}, key:{1})", context, key)
				.isFalse((value instanceof byte[]), "CachePlugin can't cache byte[] values")
				.isTrue(value instanceof Serializable,
						"Object to cache isn't Serializable. Make it unmodifiable or add it in noSerialization's plugin parameter. (context: {0}, key:{1}, class:{2})",
						context, key, value.getClass().getSimpleName());
		//---
		final String redisKey = buildRedisKey(context, key);

		// redisValue = Base64 encoding of the serialized value
		final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
		final String redisValue = codecManager.getBase64Codec().encode(serializedObject);

		try (final Jedis jedis = redisConnector.getClient()) {
			jedis.setex(redisKey, getCacheDefinition(context).getTimeToLiveSeconds(), redisValue);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		final String redisKey = buildRedisKey(context, key); //Assertions on context and key done inside this private method
		final String redisValue;

		try (final Jedis jedis = redisConnector.getClient()) {
			redisValue = jedis.get(redisKey);
		}

		if (redisValue == null) {
			return null;
		}
		final byte[] serializedObject = codecManager.getBase64Codec().decode(redisValue);
		return codecManager.getCompressedSerializationCodec().decode(serializedObject);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final String context, final Serializable key) {
		final String redisKey = buildRedisKey(context, key); //Assertions on context and key done inside this private method
		try (final Jedis jedis = redisConnector.getClient()) {
			return jedis.del(redisKey) > 0;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		final String pattern = buildPatternFromContext(context);
		try (final Jedis jedis = redisConnector.getClient()) {
			jedis.eval(String.format(DELETE_KEYS_ON_PATTERN_SCRIPT, pattern));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clearAll() {
		try (final Jedis jedis = redisConnector.getClient()) {
			jedis.eval(String.format(DELETE_KEYS_ON_PATTERN_SCRIPT, VERTIGO_CACHE + ":*"));
		}

	}

	private static CacheDefinition getCacheDefinition(final String cacheName) {
		return Node.getNode().getDefinitionSpace().resolve(cacheName, CacheDefinition.class);
	}

	/*
	 * Builds a string to represent the key to be set in Redis
	 * redisKey = "vertigo:cache:" + context + key
	 */
	private static String buildRedisKey(final String context, final Serializable key) {
		Assertion.check()
				.isNotBlank(context)
				.isNotNull(key);
		//---
		return VERTIGO_CACHE + ":" + context + ":" + keyToString(key);
	}

	/*
	 * Builds a string to represent the key to be set in Redis
	 * redisKey = "vertigo:cache:" + context + key
	 */
	private static String buildPatternFromContext(final String context) {
		Assertion.check().isNotBlank(context);
		//---
		return VERTIGO_CACHE + ":" + context + ":*";
	}

	/*
	 * Converts a key into a String.
	 * An empty key is considered as null.
	 */
	private static String keyToString(final Serializable key) {
		Assertion.check().isNotNull(key);
		//---
		if (key instanceof String) {
			Assertion.check().isNotBlank((String) key, "a key cannot be an empty string");
			//--
			return ((String) key).trim();
		} else if (key instanceof Integer) {
			return key.toString();
		} else if (key instanceof Long) {
			return key.toString();
		}
		throw new IllegalArgumentException(key.toString() + " is not supported as a key type");
	}
}
