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
package io.vertigo.social.plugins.handle.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.handle.Handle;
import io.vertigo.social.impl.handle.HandlePlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class RedisHandlePlugin implements HandlePlugin {

	private final RedisConnector redisConnector;

	@Inject
	public RedisHandlePlugin(
			@ParamValue("connectorName") final Optional<String> connectorNameOpt,
			final List<RedisConnector> redisConnectors) {
		Assertion.check()
				.isNotNull(connectorNameOpt)
				.isNotNull(redisConnectors);
		//---
		final String connectorName = connectorNameOpt.orElse("main");
		redisConnector = redisConnectors.stream()
				.filter(connector -> connectorName.equals(connector.getName()))
				.findFirst().get();
	}

	@Override
	public void add(final List<Handle> handles) {
		try (final Jedis jedis = redisConnector.getClient()) {
			for (final Handle handle : handles) {
				if (jedis.exists("urn_handle:" + handle.getUid().urn())) {
					// if exist we need to clean the index and the reverse index
					final String code = jedis.hget("urn_handle:" + handle.getUid().urn(), "code");
					jedis.del("urn_handle:" + handle.getUid().urn());
					jedis.del("handle:" + code, "urn_handle:" + handle.getUid().urn());
				}
				jedis.hmset("handle:" + handle.getCode(), toMap(handle));
				jedis.hmset("urn_handle:" + handle.getUid().urn(), toMap(handle));
			}
		}

	}

	@Override
	public void remove(final List<UID> uids) {
		try (final Jedis jedis = redisConnector.getClient()) {
			for (final UID uid : uids) {
				if (jedis.exists("urn_handle:" + uid.urn())) {
					// if exist we need to clean the index and the reverse index
					final String code = jedis.hget("urn_handle:" + uid.urn(), "code");
					jedis.del("handle:" + code, "urn_handle:" + uid.urn());
				}
				jedis.del("urn_handle:" + uid.urn());
			}
		}

	}

	@Override
	public List<Handle> search(final String prefix) {
		try (final Jedis jedis = redisConnector.getClient()) {
			final List<Handle> handleResults = new ArrayList<>();
			String cursor = "0";
			final ScanParams scanParams = new ScanParams();
			scanParams.count(1000);
			scanParams.match("handle:" + prefix + "*");
			do {
				//
				final ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
				cursor = scanResult.getCursor();
				scanResult.getResult()
						.stream()
						.limit(10)
						.forEach(key -> handleResults.add(fromMap(jedis.hgetAll(key))));

			} while (handleResults.size() < 10 && !"0".equals(cursor));
			return handleResults;
		}

	}

	@Override
	public Handle getByCode(final String handleCode) {
		try (final Jedis jedis = redisConnector.getClient()) {
			return fromMap(jedis.hgetAll("handle:" + handleCode));
		}
	}

	@Override
	public Handle getByUid(final UID uid) {
		try (final Jedis jedis = redisConnector.getClient()) {
			return fromMap(jedis.hgetAll("urn_handle:" + uid.urn()));
		}
	}

	private static Map<String, String> toMap(final Handle handle) {
		return new MapBuilder<String, String>()
				.put("urn", handle.getUid().urn())
				.put("code", handle.getCode())
				.build();
	}

	private static Handle fromMap(final Map<String, String> data) {
		return new Handle(UID.of(data.get("urn")), data.get("code"));

	}

	@Override
	public void removeAll() {
		try (final Jedis jedis = redisConnector.getClient()) {
			String cursor = "0";
			final ScanParams handleScanParams = new ScanParams();
			handleScanParams.match("handle:*");
			do {
				//
				final ScanResult<String> scanResult = jedis.scan(cursor, handleScanParams);
				cursor = scanResult.getCursor();
				scanResult.getResult()
						.forEach(jedis::del);

			} while (!"0".equals(cursor));
			// cursor is 0... but still
			cursor = "0";
			final ScanParams urnScanParams = new ScanParams();
			urnScanParams.match("urn_handle:*");
			do {
				//
				final ScanResult<String> scanResult = jedis.scan(cursor, urnScanParams);
				cursor = scanResult.getCursor();
				scanResult.getResult()
						.forEach(jedis::del);

			} while (!"0".equals(cursor));

		}

	}

}
