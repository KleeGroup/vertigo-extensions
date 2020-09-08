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
package io.vertigo.account.account;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;

/**
 * @author pchretien
 */
public final class AccountBuilder implements Builder<Account> {
	private final String myId;
	private String myDisplayName;
	private String myEmail;
	private String myPhoto;
	private String myAuthToken;

	/**
	 * constructor
	 * @param id the id of the account
	 */
	AccountBuilder(final String id) {
		Assertion.check().isNotBlank(id);
		//-----
		myId = id;
	}

	/**
	 * Adds a displayName
	 * @param displayName the display name
	 * @return this builder
	 */
	public AccountBuilder withDisplayName(final String displayName) {
		Assertion.check()
				.isNull(myDisplayName, "displayName already set")
				.isNotBlank(displayName);
		//-----
		myDisplayName = displayName;
		return this;
	}

	/**
	 * Adds an email
	 * @param email the email
	 * @return this builder
	 */
	public AccountBuilder withEmail(final String email) {
		Assertion.check().isNull(myEmail, "email already set");
		//email is nullable, we accept null value in case this builder is use by deserializer
		//-----
		myEmail = email;
		return this;
	}

	/**
	 * Adds an photo
	 * @param photo the photo
	 * @return this builder
	 */
	public AccountBuilder withPhoto(final String photo) {
		Assertion.check().isNull(myPhoto, "photo already set");
		//photo is nullable, we accept null value in case this builder is use by deserializer
		//-----
		myPhoto = photo;
		return this;
	}

	/**
	 * Adds an authToken
	 * @param authToken the authToken
	 * @return this builder
	 */
	public AccountBuilder withAuthToken(final String authToken) {
		Assertion.check()
				.isNull(myAuthToken, "authToken already set")
				.isNotBlank(authToken);
		//-----
		myAuthToken = authToken;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Account build() {
		return new Account(myId, myDisplayName, myEmail, myPhoto, myAuthToken != null ? myAuthToken : myEmail);
	}
}
