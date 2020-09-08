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
import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.stereotype.Field;

/**
 * This class defines the account of a user/organization/system.
 *
 * @author pchretien
 */
public final class Account implements Entity {
	private static final long serialVersionUID = 7509030642946579907L;

	@Field(type = "ID", smartType = "DoXAccountId", cardinality = Cardinality.ONE, label = "id")
	private final String id;

	@Field(smartType = "DoXAccountName", label = "displayName")
	private final String displayName;

	@Field(smartType = "DoXAccountEmail", label = "email")
	private final String email;

	@Field(smartType = "DoXAccountPhoto", label = "photo")
	private final String photo;

	@Field(smartType = "DoXAccountAuthToken", label = "authToken")
	private final String authToken;

	Account(final String id, final String displayName, final String email, final String photo, final String authToken) {
		Assertion.check().isNotBlank(id);
		//-----
		this.id = id;
		this.displayName = displayName;
		this.email = email;
		this.photo = photo;
		this.authToken = authToken;
	}

	/**
	 * Static method factory for AccountBuilder
	 * @param id the id of the account
	 * @return AccountBuilder
	 */
	public static AccountBuilder builder(final String id) {
		return new AccountBuilder(id);
	}

	/** {@inheritDoc} */
	@Override
	public UID<Account> getUID() {
		return UID.of(this);
	}

	/**
	 * @return the id of the account
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the diplayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the photo
	 */
	public String getPhoto() {
		return photo;
	}

	/**
	 * @return the authToken
	 */
	public String getAuthToken() {
		return authToken;
	}
}
