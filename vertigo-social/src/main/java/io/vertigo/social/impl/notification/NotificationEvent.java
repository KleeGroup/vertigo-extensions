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

import java.util.Set;

import io.vertigo.account.account.Account;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.notification.Notification;

/**
 * @author pchretien
 */
public final class NotificationEvent {
	private final Notification notification;
	private final Set<UID<Account>> accountURIs;

	/**
	 * Constructor.
	 * @param notification Notification
	 * @param accountURIs To accounts uri
	 */
	NotificationEvent(final Notification notification, final Set<UID<Account>> accountURIs) {
		Assertion.check()
				.isNotNull(notification)
				.isNotNull(accountURIs);
		//-----
		this.notification = notification;
		this.accountURIs = accountURIs;
	}

	/**
	 * @return Notification
	 */
	public Notification getNotification() {
		return notification;
	}

	/**
	 * @return To accounts uri
	 */
	public Set<UID<Account>> getToAccountURIs() {
		return accountURIs;
	}
}
