/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.social.plugins.mail.javax;

import java.util.Optional;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Plugin de gestion des mails, pour l'implémentation du jdk.
 *
 * @author npiedeloup
 */
public final class NativeMailSessionConnector implements MailSessionConnector {

	private final String mailStoreProtocol;
	private final String mailHost;
	private final Optional<Integer> mailPort;
	private final Optional<String> mailLogin;
	private final Optional<String> mailPassword;

	/**
	 * Crée le plugin d'accès au serveur mail.
	 *
	 * @param mailStoreProtocol Protocole utilisé
	 * @param mailHost Serveur de mail
	 * @param mailPort port à utiliser (facultatif)
	 * @param mailLogin Login à utiliser lors de la connexion au serveur mail (facultatif)
	 * @param mailPassword mot de passe à utiliser lors de la connexion au serveur mail (facultatif)
	 */
	@Inject
	public NativeMailSessionConnector(
			@ParamValue("storeProtocol") final String mailStoreProtocol,
			@ParamValue("host") final String mailHost,
			@ParamValue("port") final Optional<Integer> mailPort,
			@ParamValue("login") final Optional<String> mailLogin,
			@ParamValue("pwd") final Optional<String> mailPassword) {
		Assertion.checkArgNotEmpty(mailStoreProtocol);
		Assertion.checkArgNotEmpty(mailHost);
		Assertion.checkNotNull(mailPort);
		Assertion.checkNotNull(mailLogin);
		Assertion.checkNotNull(mailPassword);
		Assertion.when(mailLogin.isPresent())
				.check(() -> !StringUtil.isEmpty(mailLogin.get()), // if set, login can't be empty
						"When defined Login can't be empty");
		Assertion.checkArgument(!mailLogin.isPresent() ^ mailPassword.isPresent(), // login and password must be null or not null both
				"Password is required when login is defined");
		//-----
		this.mailStoreProtocol = mailStoreProtocol;
		this.mailHost = mailHost;
		this.mailPort = mailPort;
		this.mailLogin = mailLogin;
		this.mailPassword = mailPassword;
	}

	@Override
	public Session createSession() {
		final Properties properties = new Properties();
		properties.setProperty("mail.store.protocol", mailStoreProtocol);
		properties.setProperty("mail.host", mailHost);
		if (mailPort.isPresent()) {
			properties.setProperty("mail.port", mailPort.get().toString());
		}
		properties.setProperty("mail.debug", "false");
		final Session session;
		if (mailLogin.isPresent()) {
			properties.setProperty("mail.smtp.ssl.trust", mailHost);
			properties.setProperty("mail.smtp.starttls.enable", "true");
			properties.setProperty("mail.smtp.auth", "true");

			final String username = mailLogin.get();
			final String password = mailPassword.get();
			session = Session.getInstance(properties, new javax.mail.Authenticator() {

				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		} else {
			session = Session.getDefaultInstance(properties);
		}
		session.setDebug(false);
		return session;
	}
}
