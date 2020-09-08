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
package io.vertigo.social.impl.notification;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.notification.Notification;
import io.vertigo.social.notification.NotificationManager;

/**
 * @author pchretien, npiedeloup, btounkara
 */
public final class NotificationManagerImpl implements NotificationManager {

	private final NotificationPlugin notificationsPlugin;

	/**
	 * @param notificationsPlugin Notifications plugin
	 */
	@Inject
	public NotificationManagerImpl(final NotificationPlugin notificationsPlugin) {
		Assertion.check().isNotNull(notificationsPlugin);
		// -----
		this.notificationsPlugin = notificationsPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public void send(final Notification notification, final Set<UID<Account>> accountURIs) {
		final NotificationEvent notificationEvent = new NotificationEvent(notification, accountURIs);
		notificationsPlugin.send(notificationEvent);
	}

	/** {@inheritDoc} */
	@Override
	public List<Notification> getCurrentNotifications(final UID<Account> userProfileURI) {
		Assertion.check().isNotNull(userProfileURI);
		// -----
		return notificationsPlugin.getCurrentNotifications(userProfileURI);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final UID<Account> accountURI, final UUID notificationUUID) {
		notificationsPlugin.remove(accountURI, notificationUUID);
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final String type, final String targetUrl) {
		notificationsPlugin.removeAll(type, targetUrl);
	}

	@Override
	public void updateUserContent(final UID<Account> accountURI, final UUID notificationUUID, final String userContent) {
		notificationsPlugin.updateUserContent(accountURI, notificationUUID, userContent);
	}
}
