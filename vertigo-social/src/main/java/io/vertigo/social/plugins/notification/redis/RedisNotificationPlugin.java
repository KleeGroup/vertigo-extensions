/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.social.plugins.notification.redis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.commons.daemon.DaemonScheduled;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.lang.Assertion;
import io.vertigo.social.impl.notification.NotificationEvent;
import io.vertigo.social.impl.notification.NotificationPlugin;
import io.vertigo.social.services.notification.Notification;
import io.vertigo.util.MapBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

/**
 * @author pchretien
 */
public final class RedisNotificationPlugin implements NotificationPlugin {
	private static final long REMOVE_PACKET_SIZE = 100L;
	private final RedisConnector redisConnector;

	/**
	 * Constructor.
	 * @param redisConnector the connector to REDIS database
	 * @param daemonManager Daemon Manager
	 */
	@Inject
	public RedisNotificationPlugin(final RedisConnector redisConnector, final DaemonManager daemonManager) {
		Assertion.checkNotNull(redisConnector);
		Assertion.checkNotNull(daemonManager);
		//-----
		this.redisConnector = redisConnector;
	}

	/** {@inheritDoc} */
	@Override
	public void send(final NotificationEvent notificationEvent) {
		Assertion.checkNotNull(notificationEvent);
		//-----
		//1 notif is store 5 times :
		// - data in map with key= notif:$uuid (with expiration)
		// - uuid in queue with key= notifs:all (for purge)
		// - uuid in queue with key= notifs:$accountId
		// - uuid in queue with key= type:$type;target:$target;uuid
		// - notifs:$accountId in queue with key= accounts:$uuid
		// - userContent value per accountId:$accountId in map with key= userContent:$uuid

		try (final Jedis jedis = redisConnector.getResource()) {
			final Notification notification = notificationEvent.getNotification();
			final String uuid = notification.getUuid().toString();
			final String typedTarget = "type:" + notification.getType() + ";target:" + notification.getTargetUrl() + ";uuid";
			try (final Transaction tx = jedis.multi()) {
				tx.hmset("notif:" + uuid, toMap(notification));

				//TODO add expire on data
				//retirer notifs:all qui ne sert plus à rien
				//loop on type:$type;target:$target;uuid et on déduit les autres queues de là
				if (notification.getTTLInSeconds() > 0) {
					tx.expire("notif:" + uuid, notification.getTTLInSeconds() + 24 * 60 * 60); //expire in Redis in a security way (TTL + 1 day) purge is done by daemon
				}
				tx.lrem("notifs:all", 0, uuid);
				tx.lpush("notifs:all", uuid);

				for (final UID<Account> accountURI : notificationEvent.getToAccountURIs()) {
					final String notifiedAccount = "notifs:" + accountURI.getId();
					//On publie la notif (the last wins)
					tx.lrem(notifiedAccount, 0, uuid);
					tx.lpush(notifiedAccount, uuid);
					tx.lrem("accounts:" + uuid, 0, notifiedAccount);
					tx.lpush("accounts:" + uuid, notifiedAccount);
					tx.lrem(typedTarget, 0, uuid);
					tx.lpush(typedTarget, uuid);
				}
				tx.exec();
			}

		}
	}

	private static Map<String, String> toMap(final Notification notification) {
		return new MapBuilder<String, String>()
				.put("uuid", notification.getUuid().toString())
				.put("sender", notification.getSender())
				.putNullable("type", notification.getType())
				.put("title", notification.getTitle())
				.put("content", notification.getContent())
				.put("creationDate", notification.getCreationDate().toString())
				.put("ttlInSeconds", String.valueOf(notification.getTTLInSeconds()))
				.put("targetUrl", notification.getTargetUrl())
				.put("userContent", notification.getUserContent().orElse("")) //only used for default value
				.build();
	}

	private static Notification fromMap(final Map<String, String> data, final String userContent) {
		return Notification.builder(UUID.fromString(data.get("uuid")))
				.withSender(data.get("sender"))
				.withType(data.get("type"))
				.withTitle(data.get("title"))
				.withContent(data.get("content"))
				.withCreationDate(Instant.parse(data.get("creationDate")))
				.withTTLInSeconds(Integer.parseInt(data.get("ttlInSeconds")))
				.withTargetUrl(data.get("targetUrl"))
				.withUserContent(userContent != null ? userContent : data.get("userContent")) //only used for default value
				.build();
	}

	/** {@inheritDoc} */
	@Override
	public List<Notification> getCurrentNotifications(final UID<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		final List<Response<Map<String, String>>> responses = new ArrayList<>();
		final List<Response<String>> responsesUserContent = new ArrayList<>();
		try (final Jedis jedis = redisConnector.getResource()) {
			final List<String> uuids = jedis.lrange("notifs:" + accountURI.getId(), 0, -1);
			final Transaction tx = jedis.multi();
			for (final String uuid : uuids) {
				responses.add(tx.hgetAll("notif:" + uuid));
				responsesUserContent.add(tx.hget("userContent:" + uuid, "accountURI:" + accountURI.getId()));
			}
			tx.exec();
		}
		//----- we are using tx to avoid roundtrips
		final List<Notification> notifications = new ArrayList<>();
		for (int i = 0; i < responses.size(); i++) {
			final Response<Map<String, String>> response = responses.get(i);
			final Map<String, String> data = response.get();
			if (!data.isEmpty()) {
				final String userContent = responsesUserContent.get(i).get();
				notifications.add(fromMap(data, userContent));
			}
		}
		cleanTooOldNotifications(notifications);
		return notifications;
	}

	/** {@inheritDoc} */
	@Override
	public void updateUserContent(final UID<Account> accountURI, final UUID notificationUUID, final String userContent) {
		Assertion.checkNotNull(accountURI);
		Assertion.checkNotNull(notificationUUID);
		//-----
		try (final Jedis jedis = redisConnector.getResource()) {
			final String uuid = notificationUUID.toString();
			final String updatedAccount = "accountURI:" + accountURI.getId();

			try (final Transaction tx = jedis.multi()) {
				//we store the data of this notification
				tx.hset("userContent:" + uuid, updatedAccount, userContent != null ? userContent : "");
				tx.exec();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final UID<Account> accountURI, final UUID notificationUUID) {
		Assertion.checkNotNull(accountURI);
		Assertion.checkNotNull(notificationUUID);
		//-----
		try (final Jedis jedis = redisConnector.getResource()) {
			final String notifiedAccount = "notifs:" + accountURI.getId();
			final String uuid = notificationUUID.toString();
			//we remove notif from account stack and account from notif stack
			jedis.lrem(notifiedAccount, -1, uuid);
			jedis.lrem("accounts:" + uuid, -1, notifiedAccount);
			jedis.hdel("userContent:" + uuid, "accountURI:" + accountURI.getId());

			final List<String> notifiedAccounts = jedis.lrange("accounts:" + uuid, 0, -1);
			if (notifiedAccounts.isEmpty()) { //if no more account ref this notif we remove it
				//we remove list account for this notif
				jedis.del("accounts:" + uuid);

				//we remove uuid from queue by type and targetUrl
				final Map<String, String> notifMap = jedis.hgetAll("notif:" + uuid);
				if (!notifMap.isEmpty()) {
					final Notification notification = fromMap(notifMap, null);
					jedis.lrem("type:" + notification.getType() + ";target:" + notification.getTargetUrl() + ";uuid", -1, uuid);
				}

				//we remove userContent of this notif
				jedis.del("userContent:" + uuid);

				//we remove data of this notif
				jedis.del("notif:" + uuid);

				//we remove notif from global index (looking from tail)
				jedis.lrem("notifs:all", -1, uuid);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final String type, final String targetUrl) {
		try (final Jedis jedis = redisConnector.getResource()) {
			final List<String> uuids = jedis.lrange("type:" + type + ";target:" + targetUrl + ";uuid", 0, -1);
			for (final String uuid : uuids) {
				//we search accounts for this notif
				final List<String> notifiedAccounts = jedis.lrange("accounts:" + uuid, 0, -1);
				for (final String notifiedAccount : notifiedAccounts) {
					//we remove this notifs from accounts queue
					jedis.lrem(notifiedAccount, -1, uuid);
				}
				//we remove list account for this notif
				jedis.del("accounts:" + uuid);
				//we remove userContent of this notif
				jedis.del("userContent:" + uuid);
				//we remove data of this notif
				jedis.del("notif:" + uuid);

				//we remove notif from global index (looking from tail)
				jedis.lrem("notifs:all", -1, uuid);
			}
			//we remove list notifId for this type and targetUrl
			jedis.del("type:" + type + ";target:" + targetUrl + ";uuid");
		}
	}

	/**
	 * Scan all notifs every minutes to removed old ones.
	 */
	@DaemonScheduled(name = "DmnCleanTooOldRedisNotifications", periodInSeconds = 60)
	public void cleanTooOldNotifications() {
		long startIndex = -1L;
		final long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime < 10 * 1000) {
			try (final Jedis jedis = redisConnector.getResource()) {
				final List<String> uuids = jedis.lrange("notifs:all", startIndex - REMOVE_PACKET_SIZE, startIndex); //return last (older) 100 uuid (but not sorted)
				if (uuids.isEmpty()) {
					break;// no more notifs we do nothing and stop now
				}
				for (final String uuid : uuids) {
					final Map<String, String> notificationMap = jedis.hgetAll("notif:" + uuid);
					final Notification notification;
					if (!notificationMap.isEmpty()) {
						notification = fromMap(notificationMap, null);
						if (isTooOld(notification)) {
							removeNotification(uuid, notification, jedis);
							startIndex++;
						}
					} else { //case of Redis expiration
						removeNotification(uuid, null, jedis);
						startIndex++;
					}
				}
				startIndex = startIndex - REMOVE_PACKET_SIZE;
			}
		}
	}

	private static void removeNotification(final String uuid, final Notification notification, final Jedis jedis) {
		//we search accounts for this notif
		final List<String> notifiedAccounts = jedis.lrange("accounts:" + uuid, 0, -1);
		for (final String notifiedAccount : notifiedAccounts) {
			//we remove this notifs from accounts queue (looking from tail)
			jedis.lrem(notifiedAccount, -1, uuid);
		}
		//we remove list account for this notif
		jedis.del("accounts:" + uuid);
		//we remove userContent of this notif
		jedis.del("userContent:" + uuid);
		//we remove data of this notif
		jedis.del("notif:" + uuid);
		//we remove notif from global index (looking from tail)
		jedis.lrem("notifs:all", -1, uuid);
		//we remove uuid from queue by type and targetUrl (looking from tail)
		if (notification != null) {
			jedis.lrem("type:" + notification.getType() + ";target:" + notification.getTargetUrl() + ";uuid", -1, uuid);
		}
	}

	private static void cleanTooOldNotifications(final List<Notification> notifications) {
		notifications.removeIf(RedisNotificationPlugin::isTooOld);
	}

	private static boolean isTooOld(final Notification notification) {
		return notification.getTTLInSeconds() >= 0 && notification.getCreationDate().toEpochMilli() + notification.getTTLInSeconds() * 1000 < System.currentTimeMillis();
	}
}
