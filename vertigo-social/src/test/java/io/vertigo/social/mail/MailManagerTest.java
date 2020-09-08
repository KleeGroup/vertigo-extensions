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
package io.vertigo.social.mail;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.connectors.mail.MailFeatures;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VUserException;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.social.SocialFeatures;

/**
 * Test de l'implémentation standard.
 *
 * @author npiedeloup
 */
public final class MailManagerTest {
	//private static final String DT_MAIL = "Direction Technique<direction.technique@kleegroup.com>";
	//private static final String NPI_MAIL = "Nicolas Piedeloup<npiedeloup@kleegroup.com>";
	private static final String DT_MAIL = "Direction Technique<direction.technique@yopmail.com>";
	private static final String NPI_MAIL = "Nicolas Piedeloup<npiedeloup@yopmail.com>";

	@Inject
	private MailManager mailManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.build())
				.addModule(new MailFeatures()
						.withNativeMailConnector(
								Param.of("storeProtocol", "smtp"),
								Param.of("host", "localdelivery.klee.lan.net"))
						.build())
				.addModule(new CommonsFeatures().build())
				.addModule(new DataStoreFeatures().build())
				.addModule(new SocialFeatures()
						.withMails()
						.withJavaxMail(
								Param.of("developmentMode", "true"),
								Param.of("developmentMailTo", "klee-DevTest@yopmail.com"))
						.build())
				.build();
	}

	private void nop(final Object o) {
		//nada
	}

	/**
	 */
	@Test
	public void testNotNull() {
		Assertion.check().isNotNull(mailManager);
	}

	/**
	 * Crée un mail simple.
	 */
	@Test
	public void testSimpliestCreateMail() {
		final Mail mail = Mail.builder()
				.from(DT_MAIL)
				.to(NPI_MAIL)
				.withSubject("-1-testSimpliestCreateMail")
				.withTextContent("test")
				.build();
		Assertions.assertNotNull(mail);
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullAddress() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final String to = null;
			final Mail mail = Mail.builder()
					.withSubject("-3-testWritableMailErrors")
					.to(to)
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithEmptyAddress() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-4-testWritableMailErrors")
					.to("")
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullSubject() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject(null).to(NPI_MAIL)
					.build();
			nop(mail);
		});

	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithEmptySubject() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("")//
					.to(NPI_MAIL)
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullFrom() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()//
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.from(null)
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithEmptyFrom() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.from("")
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullHtmlContent() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.withHtmlContent(null)
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithEmptyHtmlContent() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.withHtmlContent("")
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullContent() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.withTextContent(null)
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithEmptyContent() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.withTextContent("")
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullReply() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.replyTo(null)
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithEmptyReply() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.to("")
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullTo() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final String to = null;
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.to(to)
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithEmptyTo() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.to("")
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullCc() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final String cc = null;

			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.cc(cc)
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithEmptyCc() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.cc("")
					.build();
			nop(mail);
		});
	}

	/**
	 * Test les erreurs de creation de mail.
	 */
	@Test
	public void testWritableMailErrorsWithNullAttachment() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final VFile files = null;

			final Mail mail = Mail.builder()
					.withSubject("-5-testWritableMailErrors")
					.to(DT_MAIL)
					.withAttachments(files)
					.build();
			nop(mail);
		});
	}

	//	/**
	//	 * Test l'emetteur par défaut.
	//	 */
	//	@Test
	//	public void testDefaultFrom() {
	//		final Mail mail = Mail.builder()
	//				.to(NPI_MAIL)
	//				.withSubject("-6-testWritableMail")
	//				.build();
	//		Assertions.assertEquals("npiedeloup@kleegroup.com", mail.getFrom());
	//	}

	/**
	 * Test l'envoi d'un mail vide.
	 */
	@Test
	public void testSendEmptyMail() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()
					.to(NPI_MAIL)
					.withSubject("-7-testSendEmptyMail")
					.build();
			mailManager.sendMail(mail);
		});
	}

	/**
	 * Test l'envoi d'un mail simple.
	 */
	@Test
	public void testSendSimpliestMail() {
		final Mail mail = Mail.builder()
				.from(DT_MAIL)
				.to(NPI_MAIL)
				.withSubject("1-testSendSimpliestMail")
				.withTextContent("Mon test en <b>TEXT</b>")
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un From.
	 */
	@Test
	public void testSendMailWithFrom() {
		final Mail mail = Mail.builder()
				.to(NPI_MAIL)
				.withSubject("2-testSendMailWithFrom")
				.from(DT_MAIL)
				.withTextContent("Mon test en <b>TEXT</b>")
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un To erroné.
	 */
	@Test
	public void testSendMailWithBadTo() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()
					.withSubject("-8-testWritableMailWithBadTo")
					.to("NOT-A-EMAIL")
					.withTextContent("Mon test en <b>TEXT</b>")
					.build();

			mailManager.sendMail(mail);
		});
	}

	/**
	 * Test l'envoi d'un mail avec un From erroné.
	 */
	@Test
	public void testSendMailWithBadFrom() {
		Assertions.assertThrows(VUserException.class, () -> {
			final Mail mail = Mail.builder()
					.to(NPI_MAIL)
					.withSubject("-9-testWritableMailWithBadFrom")
					.from("NOT-A-EMAIL")
					.withTextContent("Mon test en <b>TEXT</b>")
					.build();

			mailManager.sendMail(mail);
		});
	}

	/**
	 * Test l'envoi d'un mail avec un ReplyTo erroné.
	 */
	@Test
	public void testSendMailWithBadReplyTo() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()
					.to(NPI_MAIL)
					.withSubject("-10-testWritableMailWithBadReplyTo")
					.replyTo("NOT-A-EMAIL")
					.withTextContent("Mon test en <b>TEXT</b>")
					.build();

			mailManager.sendMail(mail);
		});
	}

	/**
	 * Test l'envoi d'un mail avec un To erroné.
	 */
	@Test
	public void testSendMailWithBadAddTo() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Mail mail = Mail.builder()
					.to(NPI_MAIL)
					.withSubject("-11-testWritableMailWithBadAddTo")
					.to("NOT-A-EMAIL")
					.withTextContent("Mon test en <b>TEXT</b>")
					.build();

			mailManager.sendMail(mail);
		});
	}

	/**
	 * Test l'envoi d'un mail avec un Cc erroné.
	 */
	@Test
	public void testSendMailWithBadCc() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			final Mail mail = Mail.builder()
					.from(DT_MAIL)
					.withSubject("-12-testWritableMailWithBadCc")
					.cc("NOT-A-EMAIL")
					.withTextContent("Mon test en <b>TEXT</b>")
					.build();

			mailManager.sendMail(mail);
		});
	}

	/**
	 * Test l'envoi d'un mail avec des destinataires multiples.
	 */
	@Test
	public void testSendMailMultipleTo() {
		final Mail mail = Mail.builder()
				.from(DT_MAIL)
				.withSubject("3-testSendMailMultipleTo")
				.withTextContent("Mon test en <b>TEXT</b>")
				.to("Denis Challas (to) <dchallas@kleegroup.com>")
				.to("Philippe Chretien (to)<pchretien@kleegroup.com>")
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec des destinataires en copie.
	 */
	@Test
	public void testSendMailMultipleCc() {
		final Mail mail = Mail.builder()
				.from(NPI_MAIL)
				.to(NPI_MAIL)
				.withSubject("4-testSendMailMultipleCc")
				.withTextContent("Mon test en <b>TEXT</b>")
				.cc("Philippe Chretien (cc)<pchretien@kleegroup.com>")
				.cc("Denis Challas (cc)<dchallas@kleegroup.com>")
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un replyTo différent du From.
	 */
	@Test
	public void testSendMailDifferentReplyTo() {
		final Mail mail = Mail.builder()
				.from(NPI_MAIL)
				.to(NPI_MAIL)
				.withSubject("5-testSendMailDifferentReplyTo")
				.replyTo(DT_MAIL)
				.withTextContent("Mon test en <b>TEXT</b>")
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html.
	 */
	@Test
	public void testSendMailHtmlOnly() {
		final Mail mail = Mail.builder()
				.from(DT_MAIL)
				.to(NPI_MAIL)
				.withSubject("6-testSendMailHtmlOnly")
				.withHtmlContent("Mon test en <b>HTML</b>")
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html et text alternatif.
	 */
	@Test
	public void testSendMailTextAndHtml() {
		final Mail mail = Mail.builder()
				.from(DT_MAIL)
				.to(NPI_MAIL)
				.withSubject("7-testSendMailTextAndHtml")
				.withTextContent("Mon test en <b>TEXT</b>")
				.withHtmlContent("Mon test en <b>HTML</b>")
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html et text alternatif.
	 */
	@Test
	public void testSendMailWithPJ() {
		final VFile image = TestUtil.createVFile("data/logo.jpg", getClass());

		final Mail mail = Mail.builder()
				.from(DT_MAIL)
				.to(NPI_MAIL)
				.withSubject("8-testSendMailWithPJ")
				.withTextContent("Mon test en <b>TEXT</b>")
				.withAttachments(image)
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html et text alternatif.
	 */
	@Test
	public void testSendMailWithOneContentTwoPJ() {
		final Mail mail = Mail.builder()
				.from(DT_MAIL)
				.to(NPI_MAIL)
				.withSubject("9-testSendMailWithOneContentTwoPJ")
				.withTextContent("Mon test en <b>TEXT</b>")
				.withAttachments(TestUtil.createVFile("data/logo.jpg", getClass()))
				.withAttachments(TestUtil.createVFile("data/test.txt", getClass()))
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html et text alternatif.
	 */
	@Test
	public void testSendMailFull() {
		final Mail mail = Mail.builder()
				.to(NPI_MAIL)
				.withSubject("-2-testWritableMail")
				.from(DT_MAIL)
				.withHtmlContent("Mon test en <b>HTML</b>")
				.withTextContent("Mon test en <b>TEXT</b>")
				.to("npiedeloup@kleegroup.com")
				.to("Denis Challas (to) <dchallas@kleegroup.com>")
				.to("Philippe Chretien (to)<pchretien@kleegroup.com>")
				.to("Philippe Chretien (cc)<pchretien@kleegroup.com>")
				.to("Denis Challas (cc)<dchallas@kleegroup.com>")

				.withAttachments(TestUtil.createVFile("data/logo.jpg", getClass()))
				.withAttachments(TestUtil.createVFile("data/test.txt", getClass()))
				.build();
		mailManager.sendMail(mail);
	}
}
