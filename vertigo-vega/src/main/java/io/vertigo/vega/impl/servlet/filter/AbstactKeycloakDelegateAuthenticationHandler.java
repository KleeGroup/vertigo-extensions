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
package io.vertigo.vega.impl.servlet.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.AuthenticatedActionsHandler;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.NodesRegistrationManagement;
import org.keycloak.adapters.PreAuthActionsHandler;
import org.keycloak.adapters.servlet.FilterRequestAuthenticator;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore;
import org.keycloak.adapters.servlet.OIDCServletHttpFacade;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.InMemorySessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.UserSessionManagement;

import io.vertigo.connectors.keycloak.KeycloakDeploymentConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Tuple;
import io.vertigo.core.lang.WrappedException;

/**
 * This class provides predinied workflow for authenticating Vertigo users with a keycloak server using OpenIdConnect protocol.
 * It is mainly insipired by the {@link org.keycloak.adapters.servlet.KeycloakOIDCFilter} provided by Keycloak
 * @author mlaroche
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 *
 */
public abstract class AbstactKeycloakDelegateAuthenticationHandler implements DelegateAuthenticationFilterHandler {

	protected final static Logger log = LogManager.getLogger("" + KeycloakOIDCFilter.class);
	protected AdapterDeploymentContext deploymentContext;
	protected final SessionIdMapper idMapper = new InMemorySessionIdMapper();
	protected NodesRegistrationManagement nodesRegistrationManagement;

	public void init(final KeycloakDeploymentConnector keycloakDeploymentConnector) {
		Assertion.check().isNotNull(keycloakDeploymentConnector);
		//---
		deploymentContext = keycloakDeploymentConnector.getClient();
		nodesRegistrationManagement = new NodesRegistrationManagement();
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<Boolean, HttpServletRequest> doBeforeChain(final HttpServletRequest request, final HttpServletResponse response) {
		final OIDCServletHttpFacade facade = new OIDCServletHttpFacade(request, response);
		final KeycloakDeployment deployment = deploymentContext.resolveDeployment(facade);
		if (deployment == null || !deployment.isConfigured()) {
			try {
				response.sendError(403);
			} catch (final IOException e) {
				throw WrappedException.wrap(e);
			}
			log.debug("deployment not configured");
			return Tuple.of(true, request);
		}

		final PreAuthActionsHandler preActions = new PreAuthActionsHandler(new UserSessionManagement() {
			@Override
			public void logoutAll() {
				idMapper.clear();
			}

			@Override
			public void logoutHttpSessions(final List<String> ids) {
				log.debug("**************** logoutHttpSessions");
				//System.err.println("**************** logoutHttpSessions");
				for (final String id : ids) {
					log.trace("removed idMapper: " + id);
					idMapper.removeSession(id);
				}

			}
		}, deploymentContext, facade);

		if (preActions.handleRequest()) {
			//System.err.println("**************** preActions.handleRequest happened!");
			return Tuple.of(true, request);
		}

		nodesRegistrationManagement.tryRegister(deployment);
		final OIDCFilterSessionStore tokenStore = new OIDCFilterSessionStore(request, facade, 100000, deployment, idMapper);
		tokenStore.checkCurrentToken();

		final FilterRequestAuthenticator authenticator = new FilterRequestAuthenticator(deployment, tokenStore, facade, request, 8443);
		final AuthOutcome outcome = authenticator.authenticate();
		if (outcome == AuthOutcome.AUTHENTICATED) {
			log.debug("AUTHENTICATED");
			if (facade.isEnded()) {
				return Tuple.of(true, request);
			}
			final AuthenticatedActionsHandler actions = new AuthenticatedActionsHandler(deployment, facade);
			if (actions.handledRequest()) {
				return Tuple.of(true, request);
			}
			final HttpServletRequestWrapper wrapper = tokenStore.buildWrapper();
			final boolean isHandledInLogin = doLogin(wrapper, response);
			if (isHandledInLogin) {
				return Tuple.of(true, wrapper);
			}
			return Tuple.of(false, wrapper);
		}

		final AuthChallenge challenge = authenticator.getChallenge();
		if (challenge != null) {
			log.debug("challenge");
			challenge.challenge(facade);
			return Tuple.of(true, request);
		}
		try {
			response.sendError(403);
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
		return Tuple.of(false, request);
	}

	public abstract boolean doLogin(final HttpServletRequest request, final HttpServletResponse response);

	/** {@inheritDoc} */
	@Override
	public void doAfterChain(final HttpServletRequest request, final HttpServletResponse response) {
		// nothing
	}

	/** {@inheritDoc} */
	@Override
	public void doFinally(final HttpServletRequest request, final HttpServletResponse response) {
		// nothing
	}
}
