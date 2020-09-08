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
package io.vertigo.account.plugins.authentication.text;

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.core.lang.Assertion;

final class AuthenticationAccountInfo {
	private final String accountKey;
	private final AuthenticationToken authenticationToken;

	AuthenticationAccountInfo(final String accountKey, final AuthenticationToken authenticationToken) {
		Assertion.check()
				.isNotNull(accountKey)
				.isNotNull(authenticationToken);
		//-----
		this.accountKey = accountKey;
		this.authenticationToken = authenticationToken;
	}

	String getAccountKey() {
		return accountKey;
	}

	AuthenticationToken getAuthenticationToken() {
		return authenticationToken;
	}
}
