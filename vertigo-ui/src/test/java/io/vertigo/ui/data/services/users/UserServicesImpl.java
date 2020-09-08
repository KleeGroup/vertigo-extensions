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
package io.vertigo.ui.data.services.users;

import javax.inject.Inject;

import io.vertigo.account.security.VSecurityManager;
import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.lang.VUserException;
import io.vertigo.ui.data.controllers.TestUserSession;
import io.vertigo.ui.data.domain.users.ApplicationUser;

@Transactional
public class UserServicesImpl implements UserServices {
	private long usrIdSeq = 1;

	@Inject
	private VSecurityManager securityManager;

	@Override
	public ApplicationUser loginUser(final String login, final String password) {
		//mock login User
		if (login == null || password == null
				|| login.isEmpty() || password.isEmpty()
				|| login.startsWith(password) || password.startsWith(login)) {
			throw new VUserException(UserResources.INVALID_CREDENTIALS);
		}

		final ApplicationUser applicationUser = new ApplicationUser();
		applicationUser.setFirstName(password);
		applicationUser.setLastName(login);
		applicationUser.setEmail(password + "." + login + "@kleegroup.com");
		applicationUser.setUsrId(usrIdSeq++);

		final TestUserSession userSession = securityManager.<TestUserSession> getCurrentUserSession().get();
		userSession.setApplicationUser(applicationUser);
		userSession.authenticate();

		return applicationUser;
	}

}
