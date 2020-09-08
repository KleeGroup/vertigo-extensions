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
package io.vertigo.social.notification;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.vertigo.account.account.Account;
import io.vertigo.core.node.component.Component;
import io.vertigo.datamodel.structure.model.UID;

/**
 * @author pchretien, npiedeloup, btounkara
 */
public interface NotificationManager extends Component {
	/**
	 * Sends a notification to a set of users
	 * @param notification Notification
	 * @param accountURIs Destination users
	 */
	void send(final Notification notification, final Set<UID<Account>> accountURIs);

	/**
	 * Retrieves all notifications for one account
	 * @param accountURI Account
	 * @return List notifications
	 */
	List<Notification> getCurrentNotifications(UID<Account> accountURI);

	/**
	 * Removes one notification.
	 * @param accountURI User account
	 * @param notificationUUID Notification uid
	 */
	void remove(UID<Account> accountURI, UUID notificationUUID);

	/**
	 * Removes all notifications by type and targetUrl.
	 * Could be use when a business module need to revoke its notifications
	 * @param type Notification type
	 * @param targetUrl Notification's target Url
	 */
	void removeAll(String type, String targetUrl);

	/**
	 * Updates the flag of the notification
	 * @param accountURI User account
	 * @param notificationUUID Notification uid
	 * @param userContent userContent
	 */
	void updateUserContent(UID<Account> accountURI, UUID notificationUUID, String userContent);
}
