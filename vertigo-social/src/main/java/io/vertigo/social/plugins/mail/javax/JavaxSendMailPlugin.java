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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import io.vertigo.commons.analytics.health.HealthChecked;
import io.vertigo.commons.analytics.health.HealthMeasure;
import io.vertigo.core.locale.MessageKey;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VUserException;
import io.vertigo.lang.WrappedException;
import io.vertigo.social.impl.mail.Resources;
import io.vertigo.social.impl.mail.SendMailPlugin;
import io.vertigo.social.services.mail.Mail;

/**
 * Plugin de gestion des mails, pour l'implémentation du jdk.
 *
 * @author npiedeloup
 */
public final class JavaxSendMailPlugin implements SendMailPlugin {

	/** Nom du composant. */
	public static final String HEALTH_COMPONENT_NAME = "Mailer";

	private final String charset;
	private final FileManager fileManager;
	private final MailSessionConnector mailSessionConnector;
	private final boolean developmentMode;
	private final String developmentMailTo;

	/**
	 * Crée le plugin d'envoie de mail.
	 *
	 * @param fileManager Manager de gestion des fichiers
	 * @param mailSessionConnector Provider d'accès au MailSession
	 * @param developmentMode Indique s'il le mode developpement est activé (surcharge des emails destinataires)
	 * @param developmentMailTo Email destinataire forcé pour développement
	 * @param charsetOpt charset to use, default is ISO-8859-1
	 */
	@Inject
	public JavaxSendMailPlugin(
			final FileManager fileManager,
			final MailSessionConnector mailSessionConnector,
			@ParamValue("developmentMode") final boolean developmentMode,
			@ParamValue("developmentMailTo") final String developmentMailTo,
			@ParamValue("charset") final Optional<String> charsetOpt) {
		Assertion.checkNotNull(fileManager);
		Assertion.checkNotNull(mailSessionConnector);
		Assertion.checkArgNotEmpty(developmentMailTo);
		//-----
		this.fileManager = fileManager;
		this.mailSessionConnector = mailSessionConnector;
		this.developmentMailTo = developmentMailTo;
		this.developmentMode = developmentMode;
		charset = charsetOpt.orElseGet(StandardCharsets.ISO_8859_1::name);
	}

	/** {@inheritDoc} */
	@Override
	public void sendMail(final Mail mail) {
		Assertion.checkNotNull(mail);
		//-----
		final Session session = mailSessionConnector.createSession();
		try {
			final Message message = createMessage(mail, session);
			Transport.send(message);
		} catch (final MessagingException e) {
			throw createMailException(Resources.TEMPO_MAIL_SERVER_TIMEOUT, e, session.getProperty("mail.host"), session.getProperty("mail.port"));
		} catch (final UnsupportedEncodingException e) {
			throw WrappedException.wrap(e, "Probleme d'encodage lors de l'envoi du mail");
		}
	}

	private Message createMessage(final Mail mail, final Session session) throws MessagingException, UnsupportedEncodingException {
		final Message message = new MimeMessage(session);
		setFromAddress(mail.getFrom(), message);
		if (mail.getReplyTo() != null) {
			setReplyToAddress(mail.getReplyTo(), message);
		}
		setToAddress(mail.getToList(), message);
		setCcAddress(mail.getCcList(), message);
		if (mail.getSubject() != null) {
			message.setSubject(MimeUtility.encodeWord(mail.getSubject(), charset, "Q"));
		}
		message.setHeader("X-Mailer", "Java");
		message.setSentDate(new Date());
		final List<VFile> attachments = mail.getAttachments();
		if (attachments.isEmpty()) {
			setBodyContent(mail.getTextContent(), mail.getHtmlContent(), message);
		} else {
			final Multipart multiPart = new MimeMultipart();
			final BodyPart bodyPart = new MimeBodyPart();
			setBodyContent(mail.getTextContent(), mail.getHtmlContent(), bodyPart);
			multiPart.addBodyPart(bodyPart);
			for (final VFile vFile : attachments) {
				final BodyPart bodyFile = createBodyFile(vFile);
				multiPart.addBodyPart(bodyFile);
			}
			message.setContent(multiPart);
		}
		return message;
	}

	private static void setFromAddress(final String from, final Message message) throws MessagingException {
		Assertion.checkNotNull(from);
		Assertion.checkNotNull(message);
		//-----
		try {
			message.setFrom(createInternetAddress(from));
		} catch (final AddressException e) {
			// on catch ici, pour pouvoir indiquer l'adresse qui pose pb
			throw createMailException(Resources.TEMPO_MAIL_ADRESS_MAIL_INVALID, e, from);
		}
	}

	private static void setReplyToAddress(final String replyTo, final Message message) throws MessagingException {
		Assertion.checkNotNull(message);
		Assertion.checkNotNull(replyTo);
		//-----
		try {
			final InternetAddress[] replyToArray = { createInternetAddress(replyTo) };
			message.setReplyTo(replyToArray);
		} catch (final AddressException e) {
			// on catch ici, pour pouvoir indiquer l'adresse qui pose pb
			throw createMailException(Resources.TEMPO_MAIL_ADRESS_MAIL_INVALID, e, replyTo);
		}
	}

	private static InternetAddress createInternetAddress(final String address) throws AddressException {
		final InternetAddress internetAddress = new InternetAddress(address);
		internetAddress.validate();
		return internetAddress;
	}

	private void setToAddress(final List<String> addressList, final Message message) throws MessagingException {
		setDestAddress(addressList, message, Message.RecipientType.TO);
	}

	private void setCcAddress(final List<String> addressList, final Message message) throws MessagingException {
		if (!addressList.isEmpty()) {
			setDestAddress(addressList, message, Message.RecipientType.CC);
		}
	}

	private void setDestAddress(final List<String> addressList, final Message message, final Message.RecipientType type) throws MessagingException {
		Assertion.checkNotNull(addressList);
		Assertion.checkArgument(!addressList.isEmpty(), "La liste des destinataires ne doit pas être vide");
		Assertion.checkNotNull(message);
		//-----
		final InternetAddress[] addresses = new InternetAddress[addressList.size()];
		for (int i = 0; i < addressList.size(); i++) {
			try {
				addresses[i] = createInternetAddress(addressList.get(i));
				if (developmentMode) {
					// en mode debug on change l'adresse, mais on laisse le nom.
					addresses[i].setAddress(developmentMailTo);
				}
			} catch (final AddressException e) {
				// on catch ici, pour pouvoir indiquer l'adresse qui pose pb
				throw createMailException(Resources.TEMPO_MAIL_ADRESS_MAIL_INVALID, e, addressList.get(i));
			}
		}
		message.setRecipients(type, addresses);
	}

	private void setBodyContent(final String textContent, final String htmlContent, final Part bodyPart) throws MessagingException {
		Assertion.checkArgument(textContent != null || htmlContent != null, "Le mail n'a pas de contenu, ni en text, ni en html");
		Assertion.checkNotNull(bodyPart);
		//-----
		if (textContent != null && htmlContent != null) {
			final Multipart multipart = new MimeMultipart("alternative");
			final BodyPart plainMessageBodyPart = new MimeBodyPart();
			plainMessageBodyPart.setContent(textContent, "text/plain; charset=" + charset);
			multipart.addBodyPart(plainMessageBodyPart);
			final BodyPart htmlMessageBodyPart = new MimeBodyPart();
			htmlMessageBodyPart.setContent(htmlContent, "text/html; charset=" + charset);
			multipart.addBodyPart(htmlMessageBodyPart);
			bodyPart.setContent(multipart);
		} else if (textContent != null) {
			bodyPart.setText(textContent);
		} else if (htmlContent != null) {
			bodyPart.setContent(htmlContent, "text/html; charset=" + charset);
		}
	}

	private BodyPart createBodyFile(final VFile vFile) throws MessagingException {
		try {
			final File file = fileManager.obtainReadOnlyFile(vFile);
			final MimeBodyPart bodyFile = new MimeBodyPart();
			bodyFile.attachFile(file);
			bodyFile.setFileName(vFile.getFileName());
			return bodyFile;
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't read attached file");
		}
	}

	private static VUserException createMailException(final MessageKey messageKey, final MessagingException messagingException, final Serializable... params) {
		final VUserException mailException = new VUserException(messageKey, params);
		mailException.initCause(messagingException);
		return mailException;
	}

	/**
	 * Test de la connexion SMTP.
	 *
	 * @return HealthMeasure
	 */
	@HealthChecked(feature = HEALTH_COMPONENT_NAME, name = "connection")
	public HealthMeasure checkConnexion() {
		// -----
		try {
			final Session session = mailSessionConnector.createSession();
			try (final Transport transport = session.getTransport()) {
				// On tente la connexion
				transport.connect();
				transport.close();
			} // La connexion s'est passée correctement. On peut la fermer
				// si on est ici, c'est que tout va bien
			return HealthMeasure.builder().withGreenStatus("Connection OK to Mail Server " + session.getProperty("mail.host")).build();
		} catch (final Exception e) {
			return HealthMeasure.builder().withRedStatus("Can't connect Mail Server", e).build();
		}
	}
}
