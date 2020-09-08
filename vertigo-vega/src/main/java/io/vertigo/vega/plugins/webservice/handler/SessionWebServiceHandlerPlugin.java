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
package io.vertigo.vega.plugins.webservice.handler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;
import io.vertigo.vega.webservice.exception.SessionException;

/**
 * Session handler.
 * Create and bind UserSession object with client.
 * @author npiedeloup
 */
public final class SessionWebServiceHandlerPlugin implements WebServiceHandlerPlugin {

	/** Stack index of the handler for sorting at startup**/
	public static final int STACK_INDEX = 60;

	/**
	 * UserSession attributeName in HttpSession.
	 */
	private static final String USER_SESSION = "io.vertigo.Session";

	private final VSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param securityManager Security Manager
	 */
	@Inject
	public SessionWebServiceHandlerPlugin(final VSecurityManager securityManager) {
		Assertion.check().isNotNull(securityManager);
		//-----
		this.securityManager = securityManager;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return webServiceDefinition.isNeedSession();
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final HttpServletRequest request, final HttpServletResponse response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {
		final HttpSession session = request.getSession(true); //obtain session (create if needed)
		final UserSession user = obtainUserSession(session);
		try {
			// Bind userSession to SecurityManager
			securityManager.startCurrentUserSession(user);

			return chain.handle(request, response, routeContext);
		} catch (final VSecurityException e) {
			if (session.isNew()) {
				//If a new session is badly use, we invalid it (light protection against DDOS)
				session.invalidate();
				//If session was just created, we translate securityException as a Session expiration.
				throw (SessionException) new SessionException("Session Expired").initCause(e);
			} else if (!user.isAuthenticated()) {
				//If user isn't authenticated, it need a authentication (http 401) like a session expiration
				throw (SessionException) new SessionException("Authentication mandatory").initCause(e);
			}
			throw e;
		} finally {
			// Unbind userSession to SecurityManager
			securityManager.stopCurrentUserSession();
		}
	}

	// ==========================================================================
	// =================GESTION DE LA SESSION UTILISATEUR========================
	// ==========================================================================

	/**
	 * Retourne la session utilisateur.
	 *
	 * @return Session utilisateur
	 */
	private UserSession obtainUserSession(final HttpSession session) {
		UserSession user = (UserSession) session.getAttribute(USER_SESSION);
		// Si la session user n'est pas créée on la crée
		if (user == null) {
			user = securityManager.createUserSession();
			session.setAttribute(USER_SESSION, user);
		}
		return user;
	}

	@Override
	public int getStackIndex() {
		return STACK_INDEX;
	}
}
