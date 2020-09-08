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
package io.vertigo.account.identityprovider.model;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.stereotype.Field;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * User.
 */
public final class User implements KeyConcept {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String usrId;
	private String fullName;
	private String email;

	/** {@inheritDoc} */
	@Override
	public UID<User> getUID() {
		return UID.of(this);
	}

	@Field(smartType = "STyCode", type = "ID", cardinality = Cardinality.ONE, label = "Id")
	public final String getUsrId() {
		return usrId;
	}

	public final void setUsrId(final String usrId) {
		this.usrId = usrId;
	}

	@Field(smartType = "STyLabel", cardinality = Cardinality.ONE, label = "FullName")
	public final String getFullName() {
		return fullName;
	}

	public final void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	@Field(smartType = "STyLabel", cardinality = Cardinality.ONE, label = "Email")
	public final String getEmail() {
		return email;
	}

	public final void setEmail(final String email) {
		this.email = email;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
