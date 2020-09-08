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
package io.vertigo.account.plugins.authentication.ldap;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.account.impl.authentication.AuthenticationPlugin;
import io.vertigo.account.impl.authentication.UsernamePasswordAuthenticationToken;
import io.vertigo.connectors.ldap.EsapiLdapEncoder;
import io.vertigo.connectors.ldap.LdapConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.param.ParamValue;

/**
 * LDAP impl of Authentification.
 * @author npiedeloup
 */
public final class LdapAuthenticationPlugin implements AuthenticationPlugin {
	private static final Logger LOGGER = LogManager.getLogger(LdapAuthenticationPlugin.class);

	private static final String USERDN_SUBSTITUTION_TOKEN = "{0}";
	private String userLoginPrefix;
	private String userLoginSuffix;
	private final LdapConnector ldapConnector;

	/**
	 * Constructor.
	 * @param userLoginTemplate userLoginTemplate
	 * @param ldapServerHost Ldap Server host
	 * @param ldapServerPort Ldap server port (default : 389)
	 */
	@Inject
	public LdapAuthenticationPlugin(
			@ParamValue("userLoginTemplate") final String userLoginTemplate,
			@ParamValue("connectorName") final Optional<String> connectorNameOpt,
			final List<LdapConnector> ldapConnectors) {
		Assertion.check()
				.isNotNull(ldapConnectors)
				.isFalse(ldapConnectors.isEmpty(), "At least one LdapConnector espected");
		//----
		parseUserLoginTemplate(userLoginTemplate);
		final String connectorName = connectorNameOpt.orElse("main");
		ldapConnector = ldapConnectors.stream()
				.filter(connector -> connectorName.equals(connector.getName()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Can't found LdapConnector named '" + connectorName + "' in " + ldapConnectors));
	}

	/** {@inheritDoc} */
	@Override
	public boolean supports(final AuthenticationToken token) {
		return token instanceof UsernamePasswordAuthenticationToken;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<String> authenticateAccount(final AuthenticationToken token) {
		Assertion.check().isNotNull(token);
		//---
		final UsernamePasswordAuthenticationToken usernamePasswordToken = (UsernamePasswordAuthenticationToken) token;
		LdapContext ldapContext = null;
		try {
			final String userProtectedDn = userLoginPrefix + protectLdap(usernamePasswordToken.getPrincipal()) + userLoginSuffix;
			ldapContext = ldapConnector.createLdapContext(userProtectedDn, usernamePasswordToken.getPassword());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Ouverture de connexion LDAP  '{}'", ldapContext);
			}
			return Optional.of(token.getPrincipal());
		} catch (final NamingException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.info("Can't authenticate user '{}'", token.getPrincipal(), e);
			} else {
				LOGGER.info("Can't authenticate user '{}'", token.getPrincipal());
			}
			return Optional.empty(); //can't connect user
		} finally {
			if (ldapContext != null) {
				closeLdapContext(ldapContext);
			}
		}
	}

	private void parseUserLoginTemplate(final String template) {
		Assertion.check().isNotBlank(template, "User DN template cannot be null or empty.");
		//----
		final int index = template.indexOf(USERDN_SUBSTITUTION_TOKEN);
		if (index < 0) {
			final String msg = "User Login template must contain the '" +
					USERDN_SUBSTITUTION_TOKEN + "' replacement token to understand where to " +
					"insert the runtime authentication principal.";
			throw new IllegalArgumentException(msg);
		}
		final String prefix = template.substring(0, index);
		final String suffix = template.substring(prefix.length() + USERDN_SUBSTITUTION_TOKEN.length());

		userLoginPrefix = prefix;
		userLoginSuffix = suffix;
	}

	private static String protectLdap(final String principal) {
		return EsapiLdapEncoder.encodeForDN(principal);
	}

	private static void closeLdapContext(final LdapContext ldapContext) {
		try {
			ldapContext.close();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Fermeture connexion Ldap  \" {} \"", ldapContext);
			}
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Erreur lors de la fermeture de l'objet LdapContext");
		}
	}
}
