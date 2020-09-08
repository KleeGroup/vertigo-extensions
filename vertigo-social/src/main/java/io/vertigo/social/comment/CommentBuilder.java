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
package io.vertigo.social.comment;

import java.time.Instant;
import java.util.UUID;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.node.Node;
import io.vertigo.datamodel.structure.model.UID;

/**
 * @author pchretien
 */
public final class CommentBuilder implements Builder<Comment> {
	private UUID myUuid;
	private String myMsg;
	private UID<Account> myAuthor;
	private String myAuthorDisplayName;
	private Instant myCreationInstant;
	private Instant myLastModified;

	/**
	 * Constructor for new comment.
	 */
	CommentBuilder() {
		//RAF
	}

	private static AccountManager getIdentityManager() {
		return Node.getNode().getComponentSpace().resolve(AccountManager.class);
	}

	/**
	 * @param author Author account
	 * @return this builder
	 */
	public CommentBuilder withAuthor(final UID<Account> author) {
		Assertion.check()
				.isNull(myAuthor, "author already set")
				.isNotNull(author);
		//-----
		myAuthor = author;
		myAuthorDisplayName = getIdentityManager().getAccount(author).getDisplayName();
		return this;
	}

	/**
	 * @param uuid UUID identifier
	 * @return this builder
	 */
	public CommentBuilder withUuid(final UUID uuid) {
		Assertion.check()
				.isNull(myUuid, "UUID already set")
				.isNotNull(uuid);
		//-----
		myUuid = uuid;
		return this;
	}

	/**
	 * @param msg Comment
	 * @return this builder
	 */
	public CommentBuilder withMsg(final String msg) {
		Assertion.check()
				.isNull(myMsg, "msg already set")
				.isNotBlank(msg);
		//-----
		myMsg = msg;
		return this;
	}

	/**
	 * @param creationDate create date time
	 * @return this builder
	 */
	public CommentBuilder withCreationDate(final Instant creationInstant) {
		Assertion.check()
				.isNull(myCreationInstant, "creationDate already set")
				.isNotNull(creationInstant);
		//-----
		myCreationInstant = creationInstant;
		return this;
	}

	/**
	 * @param lastModified Last modify date time
	 * @return this builder
	 */
	public CommentBuilder withLastModified(final Instant lastModified) {
		Assertion.check().isNull(myLastModified, "lastModified already set");
		//lastModified is optional
		//-----
		myLastModified = lastModified;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Comment build() {
		if (myCreationInstant == null) {
			myCreationInstant = Instant.now();
		}
		if (myUuid == null) {
			myUuid = UUID.randomUUID();
		}
		return new Comment(myUuid, myAuthor, myAuthorDisplayName, myMsg, myCreationInstant, myLastModified);
	}
}
