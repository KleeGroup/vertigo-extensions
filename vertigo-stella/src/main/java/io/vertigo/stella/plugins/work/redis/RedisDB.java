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
package io.vertigo.stella.plugins.work.redis;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.stella.impl.master.WorkResult;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.work.WorkEngine;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * @author pchretien
 */
public final class RedisDB {
	private final RedisConnector redisConnector;
	private final CodecManager codecManager;

	/**
	 * Constructor.
	 * @param codecManager the codecManager
	 * @param redisConnector the redis connector
	 */
	public RedisDB(final CodecManager codecManager, final RedisConnector redisConnector) {
		Assertion.check()
				.isNotNull(redisConnector)
				.isNotNull(codecManager);
		//-----
		this.redisConnector = redisConnector;
		this.codecManager = codecManager;
	}

	public void reset() {
		try (final Jedis jedis = redisConnector.getClient()) {
			jedis.flushAll();
		}
	}

	public void putStart(final String workId) {
		//Todo
	}

	/**
	 * Puts a workitem in the todo list.
	 * @param workItem the workItem
	 */
	public <R, W> void putWorkItem(final WorkItem<R, W> workItem) {
		Assertion.check().isNotNull(workItem);
		//-----
		try (Jedis jedis = redisConnector.getClient()) {
			//out.println("creating work [" + workId + "] : " + work.getClass().getSimpleName());

			final Map<String, String> datas = new MapBuilder<String, String>()
					.put("work64", encode(workItem.getWork()))
					.put("provider64", encode(workItem.getWorkEngineClass().getName()))
					.put("x-date", LocalDate.now().toString())
					.build();

			try (final Transaction tx = jedis.multi()) {
				tx.hmset("work:" + workItem.getId(), datas);
				//tx.expire("work:" + workId, 70);
				//On publie la demande de travaux
				tx.lpush("works:todo:" + workItem.getWorkEngineClass().getName(), workItem.getId());

				tx.exec();
			}
		}
	}

	/**
	 * Polls a workitem from the todo list.
	 * @param workType the type of workItem
	 * @return null or a workitem
	 */
	public <R, W> WorkItem<R, W> pollWorkItem(final String workType) {
		Assertion.check().isNotNull(workType);
		//-----
		final long start = System.currentTimeMillis();
		while ((System.currentTimeMillis() - start) < 1 * 1000) {
			final WorkItem<R, W> result = doPollWorkItem(workType);
			if (result != null) {
				return result;
			}
			//retry until timeout
			try {
				Thread.sleep(100L);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	private <W, R> WorkItem<W, R> doPollWorkItem(final String workType) {
		try (Jedis jedis = redisConnector.getClient()) {
			final String workId = jedis.rpoplpush("works:todo:" + workType, "works:in progress");
			if (workId == null) {
				return null;
			}
			final Map<String, String> hash = jedis.hgetAll("work:" + workId);
			final W work = (W) decode(hash.get("work64"));
			final String name = (String) decode(hash.get("provider64"));
			final Class<? extends WorkEngine> workEngineClass = ClassUtil.classForName(name, WorkEngine.class);
			return new WorkItem(workId, work, workEngineClass);
		}
	}

	/**
	 * Puts the result for a workitem identified by an id.
	 * @param workId the id of the workitem
	 * @param result the result
	 * @param error if an error occurred
	 */
	public <R> void putResult(final String workId, final R result, final Throwable error) {
		Assertion.check()
				.isNotBlank(workId)
				.isTrue(result == null ^ error == null, "result xor error is null");
		//-----
		final Map<String, String> datas = new HashMap<>();
		try (Jedis jedis = redisConnector.getClient()) {
			if (error == null) {
				datas.put("result", encode(result));
				datas.put("status", "ok");
			} else {
				datas.put("error", encode(error));
				datas.put("status", "ko");
			}
			try (final Transaction tx = jedis.multi()) {
				tx.hmset("work:" + workId, datas);
				tx.lrem("works:in progress", 0, workId);
				tx.lpush("works:done", workId);
				tx.exec();
			}
		}
	}

	public <R> WorkResult<R> pollResult(final int waitTimeSeconds) {
		final long start = System.currentTimeMillis();
		while ((System.currentTimeMillis() - start) < waitTimeSeconds * 1000) {
			final WorkResult<R> result = doPollResult();
			if (result != null) {
				return result;
			}
			//retry until timeout
			try {
				Thread.sleep(100L);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	private <R> WorkResult<R> doPollResult() {
		try (final Jedis jedis = redisConnector.getClient()) {
			final String workId = jedis.rpoplpush("works:done", "works:completed");
			if (workId == null) {
				return null;
			}
			final Map<String, String> hash = jedis.hgetAll("work:" + workId);
			//final boolean succeeded = "ok".equals(hash.get("status"));
			final R value = (R) decode(hash.get("result"));
			final Throwable error = (Throwable) decode(jedis.hget("work:" + workId, "error"));
			//et on détruit le work (ou bien on l'archive ???
			jedis.del("work:" + workId);
			return new WorkResult<>(workId, value, error);
		}
	}

	//	public void registerNode(final Node node) {
	//		Assertion.check().notNull(node);
	//		//-----
	//		try (Jedis jedis = jedisPool.getResource()) {
	//			jedis.lpush("nodes", node.getUID());
	//			final Map<String, String> hash = new MapBuilder<String, String>()
	//					.put("id", node.getUID())
	//					.put("active", node.isActive() ? "true" : "false")
	//					.build();
	//			jedis.hmset("node:" + node.getUID(), hash);
	//		}
	//	}

	//	public List<Node> getNodes() {
	//		try (Jedis jedis = jedisPool.getResource()) {
	//			final List<Node> nodes = new ArrayList<>();
	//
	//			final List<String> nodeIds = jedis.lrange("nodes", -1, -1);
	//			for (final String nodeId : nodeIds) {
	//				final Map<String, String> hash = jedis.hgetAll(nodeId);
	//				nodes.add(new Node(hash.get("id"), Boolean.valueOf(hash.get("active"))));
	//			}
	//			return nodes;
	//		}
	//	}

	private String encode(final Object toEncode) {
		return codecManager.getBase64Codec().encode(codecManager.getSerializationCodec().encode((Serializable) toEncode));
	}

	private Object decode(final String encoded) {
		return codecManager.getSerializationCodec().decode(codecManager.getBase64Codec().decode(encoded));
	}

}
