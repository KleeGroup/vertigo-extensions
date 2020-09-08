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

import java.util.ArrayList;
import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * EMail Builder.
 * Multiple value params are in varargs (Type...) and can be call multiple times.
 * @author pchretien, npiedeloup
 */
public class MailBuilder implements Builder<Mail> {
	private String mySubject;
	private String myReplyTo;
	private String myFrom;
	private String myTextContent;
	private String myHtmlContent;

	private final List<String> myToAddresses = new ArrayList<>();
	private final List<String> myCcAddresses = new ArrayList<>();
	private final List<VFile> myAttachments = new ArrayList<>();

	/**
	 * Constructor.
	 */
	MailBuilder() {
		super();
	}

	/**
	 * Set subject.
	 * @param subject mail subject
	 * @return MailBuilder
	 */
	public MailBuilder withSubject(final String subject) {
		Assertion.check()
				.isNotBlank(subject)
				.isNull(mySubject, "subject is already completed");
		//-----
		mySubject = subject;
		return this;
	}

	/**
	 * Set sender.
	 * @param from Mail sender
	 * @return MailBuilder
	 */
	public MailBuilder from(final String from) {
		Assertion.check()
				.isNull(myFrom, "from is already completed")
				.isNotBlank(from);
		//-----
		myFrom = from;
		return this;
	}

	/**
	 * Set receiver of return mail (response or delivery fail)
	 * @param replyTo Receiver of return mail (response or delivery fail)
	 * @return MailBuilder
	 */
	public MailBuilder replyTo(final String replyTo) {
		Assertion.check()
				.isNull(myReplyTo, "replyTo is already completed")
				.isNotBlank(replyTo);
		//-----
		myReplyTo = replyTo;
		return this;
	}

	/**
	 * Add a receiver.
	 * @param addresses Mail addresses (one or more)
	 * @return MailBuilder
	 */
	public MailBuilder to(final String... addresses) {
		Assertion.check().isNotNull(addresses);
		//-----
		for (final String address : addresses) {
			Assertion.check().isNotBlank(address);
			myToAddresses.add(address);
		}
		return this;
	}

	/**
	 * Add a copy receiver.
	 * @param addresses Mail addresses (one or more)
	 * @return MailBuilder
	 */
	public MailBuilder cc(final String... addresses) {
		Assertion.check().isNotNull(addresses);
		//-----
		for (final String address : addresses) {
			Assertion.check().isNotBlank(address);
			myCcAddresses.add(address);
		}
		return this;
	}

	/**
	 * Set mail content at text format.
	 * @param textContent Text content
	 * @return MailBuilder
	 */
	public MailBuilder withTextContent(final String textContent) {
		Assertion.check()
				.isNull(myTextContent, "textContent is already completed")
				.isNotBlank(textContent);
		//-----
		myTextContent = textContent;
		return this;
	}

	/**
	 * Set mail content at html format.
	 * @param htmlContent Html content
	 * @return MailBuilder
	 */
	public MailBuilder withHtmlContent(final String htmlContent) {
		Assertion.check()
				.isNull(myHtmlContent, "htmlContent is already completed")
				.isNotBlank(htmlContent);
		//-----
		myHtmlContent = htmlContent;
		return this;
	}

	/**
	 * Add a attachment file.
	 * @param files Files to attach (one or more)
	 * @return MailBuilder
	 */
	public MailBuilder withAttachments(final VFile... files) {
		Assertion.check().isNotNull(files);
		//-----
		for (final VFile attachment : files) {
			Assertion.check().isNotNull(attachment);
			myAttachments.add(attachment);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Mail build() {
		return new Mail(
				mySubject,
				myReplyTo,
				myFrom,
				myToAddresses,
				myCcAddresses,
				myTextContent,
				myHtmlContent,
				myAttachments);
	}
}
