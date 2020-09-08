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
package io.vertigo.ui.data.controllers;

import java.time.ZoneId;
import java.util.Locale;

import io.vertigo.account.security.UserSession;
import io.vertigo.ui.data.domain.users.ApplicationUser;

/**
 * Session d'un utilisateur<br>
 * Un utilisateur possède une liste de profils correspondant à des rêgles au sein d'une ou plusieurs entités.<br>
 * On considère que toute session utilisateur créée implique que l'utilisateur est authentifié.
 *
 * @author cgodard
 */
public final class TestUserSession extends UserSession {

	/**
	 * Serial Version.
	 */
	private static final long serialVersionUID = 2497388902473962429L;

	private ApplicationUser applicationUser;
	private ZoneId zoneId;

	/**
	 * @return Default Locale.
	 */
	@Override
	public Locale getLocale() {
		return Locale.FRANCE;
	}

	/**
	 * @return Current ZoneId.
	 */
	@Override
	public ZoneId getZoneId() {
		return zoneId;
	}

	/**
	 * @param zoneId New ZoneId.
	 */
	public void setZoneId(final ZoneId zoneId) {
		this.zoneId = zoneId;
	}

	public void setApplicationUser(final ApplicationUser applicationUser) {
		this.applicationUser = applicationUser;
	}

	public ApplicationUser getApplicationUser() {
		return applicationUser;
	}

}
