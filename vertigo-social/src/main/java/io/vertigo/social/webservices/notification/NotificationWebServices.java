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
package io.vertigo.social.webservices.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.authentication.AuthenticationManager;
import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.core.locale.MessageText;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.notification.Notification;
import io.vertigo.social.notification.NotificationManager;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.DELETE;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

/**
 * Webservice for Notification extension.
 *
 * @author npiedeloup
 */
@PathPrefix("/x/notifications")
public final class NotificationWebServices implements WebServices {

	private static final String API_VERSION = "0.1.0";
	private static final String IMPL_VERSION = "0.9.4";

	@Inject
	private NotificationManager NotificationManager;
	@Inject
	private AuthenticationManager authenticationManager;

	/**
	 * Get messages for logged user.
	 * @return messages for logged user
	 */
	@GET("/api/messages")
	public List<Notification> getMessages() {
		final UID<Account> loggedAccountURI = getLoggedAccountURI();
		return NotificationManager.getCurrentNotifications(loggedAccountURI);
	}

	/**
	 * Remove a message.
	 * @param messageUuid message id.
	 */
	@DELETE("/api/messages/{uuid}")
	public void removeMessage(@PathParam("uuid") final String messageUuid) {
		final UID<Account> loggedAccountURI = getLoggedAccountURI();
		NotificationManager.remove(loggedAccountURI, UUID.fromString(messageUuid));
	}

	/**
	 * Remove a message.
	 * @param messageUuids messages id.
	 */
	@DELETE("/api/messages")
	public void removeMessage(final List<String> messageUuids) {
		final UID<Account> loggedAccountURI = getLoggedAccountURI();
		for (final String messageUuid : messageUuids) {
			NotificationManager.remove(loggedAccountURI, UUID.fromString(messageUuid));
		}
	}

	//-----
	/**
	 * Extension status (code 200 or 500)
	 * @return "OK" or error message
	 */
	@GET("/infos/status")
	@AnonymousAccessAllowed
	public String getStatus() {
		return "OK";
	}

	/**
	 * Extension stats.
	 * @return "OK" or error message
	 */
	@GET("/infos/stats")
	@AnonymousAccessAllowed
	public Map<String, Object> getStats() {
		final Map<String, Object> stats = new HashMap<>();
		final Map<String, Object> sizeStats = new HashMap<>();
		sizeStats.put("notifications", "not yet");
		stats.put("size", sizeStats);
		return stats;
	}

	/**
	 * Extension config.
	 * @return Config object
	 */
	@GET("/infos/config")
	@AnonymousAccessAllowed
	public Map<String, Object> getConfig() {
		return new MapBuilder<String, Object>()
				.put("api-version", API_VERSION)
				.put("impl-version", IMPL_VERSION)
				.build();
	}

	/**
	 * Extension help.
	 * @return Help object
	 */
	@GET("/infos/help")
	@AnonymousAccessAllowed
	public String getHelp() {
		return "##Notification extension"
				+ "\n This extension manage the notification center.";
	}

	private UID<Account> getLoggedAccountURI() {
		return authenticationManager.getLoggedAccount()
				.orElseThrow(() -> new VSecurityException(MessageText.of("No account logged in")))
				.getUID();
	}

}
