package io.vertigo.social.plugins.handle.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.lang.Assertion;
import io.vertigo.social.impl.handle.HandlePlugin;
import io.vertigo.social.services.handle.Handle;
import io.vertigo.util.MapBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class RedisHandlePlugin implements HandlePlugin {

	private final RedisConnector redisConnector;

	@Inject
	public RedisHandlePlugin(final RedisConnector redisConnector) {
		Assertion.checkNotNull(redisConnector);
		//---
		this.redisConnector = redisConnector;
	}

	@Override
	public void add(final List<Handle> handles) {
		try (final Jedis jedis = redisConnector.getResource()) {
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
		try (final Jedis jedis = redisConnector.getResource()) {
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
		try (final Jedis jedis = redisConnector.getResource()) {
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
		try (final Jedis jedis = redisConnector.getResource()) {
			return fromMap(jedis.hgetAll("handle:" + handleCode));
		}
	}

	@Override
	public Handle getByUid(final UID uid) {
		try (final Jedis jedis = redisConnector.getResource()) {
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
		try (final Jedis jedis = redisConnector.getResource()) {
			String cursor = "0";
			final ScanParams handleScanParams = new ScanParams();
			handleScanParams.match("handle:*");
			do {
				//
				final ScanResult<String> scanResult = jedis.scan(cursor, handleScanParams);
				cursor = scanResult.getCursor();
				scanResult.getResult()
						.forEach(key -> jedis.del(key));

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
						.forEach(key -> jedis.del(key));

			} while (!"0".equals(cursor));

		}

	}

}
