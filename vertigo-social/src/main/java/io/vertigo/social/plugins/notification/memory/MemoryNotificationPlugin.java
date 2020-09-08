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
package io.vertigo.social.plugins.notification.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.vertigo.account.account.Account;
import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.impl.notification.NotificationEvent;
import io.vertigo.social.impl.notification.NotificationPlugin;
import io.vertigo.social.notification.Notification;

/**
 * @author pchretien
 */
public final class MemoryNotificationPlugin implements NotificationPlugin {
	private final Map<UID<Account>, List<Notification>> notificationsByAccountURI = new ConcurrentHashMap<>();

	/** {@inheritDoc} */
	@Override
	public void send(final NotificationEvent notificationEvent) {
		Assertion.check().isNotNull(notificationEvent);
		//-----
		//0 - Remplir la pile des événements

		//1 - Dépiler les événemnts en asynchrone FIFO
		for (final UID<Account> accountURI : notificationEvent.getToAccountURIs()) {
			obtainNotifications(accountURI).add(0, notificationEvent.getNotification());
		}

		//2 - gestion globale async des erreurs
	}

	/** {@inheritDoc} */
	@Override
	public void updateUserContent(final UID<Account> accountURI, final UUID notificationUUID, final String userContent) {
		Assertion.check()
				.isNotNull(accountURI)
				.isNotNull(notificationUUID);
		//-----
		//on recopie la notification et on ajoute la modif
		final List<Notification> newNotifications = getCurrentNotifications(accountURI)
				.stream()
				.map(notification -> (notification.getUuid().equals(notificationUUID)) ?
				//on remplace la notif
						updateNotification(notification, userContent)
						: notification)
				.collect(Collectors.toList());
		//on remplace la liste
		notificationsByAccountURI.put(accountURI, newNotifications);
	}

	private static Notification updateNotification(final Notification notification, final String userContent) {
		return Notification.builder(notification.getUuid())
				.withSender(notification.getSender())
				.withType(notification.getType())
				.withTitle(notification.getTitle())
				.withContent(notification.getContent())
				.withCreationDate(notification.getCreationDate())
				.withTTLInSeconds(notification.getTTLInSeconds())
				.withTargetUrl(notification.getTargetUrl())
				.withUserContent(userContent != null ? userContent : notification.getUserContent().orElse("")) //only used for default value
				.build();
	}

	/** {@inheritDoc} */
	@Override
	public List<Notification> getCurrentNotifications(final UID<Account> userProfileURI) {
		Assertion.check().isNotNull(userProfileURI);
		//-----
		final List<Notification> notifications = notificationsByAccountURI.get(userProfileURI);
		if (notifications == null) {
			return Collections.emptyList();
		}
		cleanTooOldNotifications(notifications);
		return notifications;
	}

	private List<Notification> obtainNotifications(final UID<Account> accountURI) {
		Assertion.check().isNotNull(accountURI);
		//-----
		final List<Notification> notifications = notificationsByAccountURI.computeIfAbsent(accountURI, uri -> new ArrayList<>());
		cleanTooOldNotifications(notifications);
		return notifications;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final UID<Account> accountURI, final UUID notificationUUID) {
		final List<Notification> notifications = notificationsByAccountURI.get(accountURI);
		if (notifications != null) {
			notifications.removeIf(notification -> notification.getUuid().equals(notificationUUID));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final String type, final String targetUrl) {
		for (final List<Notification> notifications : notificationsByAccountURI.values()) {
			notifications.removeIf(notification -> notification.getType().equals(type) && notification.getTargetUrl().equals(targetUrl));
		}
	}

	/**
	 * Clean notifications every minutes.
	 */
	@DaemonScheduled(name = "DmnCleanTooOldMemoryNotifications", periodInSeconds = 60)
	public void cleanTooOldNotifications() {
		for (final List<Notification> notifications : notificationsByAccountURI.values()) {
			cleanTooOldNotifications(notifications);
		}
	}

	private static void cleanTooOldNotifications(final List<Notification> notifications) {
		notifications.removeIf(MemoryNotificationPlugin::isTooOld);
	}

	private static boolean isTooOld(final Notification notification) {
		return notification.getTTLInSeconds() >= 0 && notification.getCreationDate().toEpochMilli() + notification.getTTLInSeconds() * 1000 < System.currentTimeMillis();
	}

}
