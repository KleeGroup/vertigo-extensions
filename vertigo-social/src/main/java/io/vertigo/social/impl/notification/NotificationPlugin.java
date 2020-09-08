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
import java.util.UUID;

import io.vertigo.account.account.Account;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.notification.Notification;

/**
 * @author pchretien
 */
public interface NotificationPlugin extends Plugin {

	/**
	 * @param notificationEvent Notification to send
	 */
	void send(NotificationEvent notificationEvent);

	/**
	 * @param account Accout uri
	 * @return All notifications for this account
	 */
	List<Notification> getCurrentNotifications(UID<Account> account);

	/**
	 * @param accountURI Account uri
	 * @param notificationUUID Notification uuid
	 */
	void remove(UID<Account> accountURI, UUID notificationUUID);

	/**
	 * @param type Notification's type
	 * @param targetUrl Target URL, use to filter all notifications to remove
	 */
	void removeAll(String type, String targetUrl);

	/**
	 * @param accountURI Account uri
	 * @param notificationUUID Notification uuid
	 * @param userContent User Content
	 */
	void updateUserContent(UID<Account> accountURI, UUID notificationUUID, String userContent);
}
