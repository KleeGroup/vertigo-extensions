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
 * @author pchretien
 */
public final class AccountGroup implements Entity {
	private static final long serialVersionUID = -4463291583101516140L;

	@Field(type = "ID", smartType = "DoXAccountId", cardinality = Cardinality.ONE, label = "id")
	private final String id;

	@Field(smartType = "DoXAccountName", label = "displayName")
	private final String displayName;

	/**
	 * @param id Id
	 * @param displayName Display name
	 */
	public AccountGroup(final String id, final String displayName) {
		Assertion.check()
				.isNotBlank(id)
				.isNotBlank(displayName);
		//-----
		this.displayName = displayName;
		this.id = id;
	}

	/** {@inheritDoc} */
	@Override
	public UID<AccountGroup> getUID() {
		return UID.of(this);
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Display name
	 */
	public String getDisplayName() {
		return displayName;
	}
}
