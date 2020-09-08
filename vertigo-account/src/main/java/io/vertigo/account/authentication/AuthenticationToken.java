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
package io.vertigo.account.authentication;

/**
 * Authentification token.
 * @author npiedeloup
 */
public interface AuthenticationToken {

	/**
	 * @return the username submitted during an authentication attempt.
	 */
	String getPrincipal();

	/**
	 * Checks if this User-submitted AuthenticationToken matches the realm trustedAuthenticationToken.
	 * @param trustedAuthenticationToken the realm trustedAuthenticationToken
	 * @return if this token matches
	 */
	boolean match(AuthenticationToken trustedAuthenticationToken);
}
