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
package io.vertigo.account.impl.authentication;

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.core.lang.Assertion;

/**
 * @author npiedeloup
 */
public final class UsernamePasswordAuthenticationToken implements AuthenticationToken {
	private final PasswordHelper passwordHelper;

	/**
	 * The username
	 */
	private final String username;

	/**
	 * The encoded password
	 */
	private final String password;

	/**
	 * @param username the principal submitted for authentication
	 * @param password the password string submitted for authentication
	 */
	public UsernamePasswordAuthenticationToken(final String username, final String password) {
		Assertion.check()
				.isNotBlank(username)
				.isNotBlank(password);
		//----
		this.username = username;
		this.password = password;
		passwordHelper = new PasswordHelper();
	}

	/** {@inheritDoc} */
	@Override
	public String getPrincipal() {
		return username;
	}

	/**
	 * Returns the password submitted during an authentication attempt
	 *
	 * @return the password submitted during an authentication attempt.
	 */
	public String getPassword() {
		return password;
	}

	/** {@inheritDoc} */
	@Override
	public boolean match(final AuthenticationToken trustedAuthenticationToken) {
		if (trustedAuthenticationToken instanceof UsernamePasswordAuthenticationToken) {
			return trustedAuthenticationToken.getPrincipal().equals(username)
					&& passwordHelper.checkPassword(((UsernamePasswordAuthenticationToken) trustedAuthenticationToken).getPassword(), password);
		}
		return false;
	}
}
