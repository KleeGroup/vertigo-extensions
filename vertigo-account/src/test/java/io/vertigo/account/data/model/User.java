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
package io.vertigo.account.data.model;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.stereotype.Field;
import io.vertigo.datamodel.structure.stereotype.ForeignKey;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datastore.impl.entitystore.StoreVAccessor;

/**
 * User.
 */
public final class User implements KeyConcept {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String usrId;
	private String fullName;
	private String email;

	@io.vertigo.datamodel.structure.stereotype.Association(name = "AGrpUsr", fkFieldName = "grpId", primaryDtDefinitionName = "DtUserGroup", primaryIsNavigable = true, primaryRole = "Group", primaryLabel = "Group", primaryMultiplicity = "0..1", foreignDtDefinitionName = "DtUser", foreignIsNavigable = false, foreignRole = "User", foreignLabel = "User", foreignMultiplicity = "0..*")
	private final StoreVAccessor<UserGroup> grpIdAccessor = new StoreVAccessor<>(UserGroup.class, "Group");

	/** {@inheritDoc} */
	@Override
	public UID<User> getUID() {
		return UID.of(this);
	}

	@Field(smartType = "STyCode", type = "ID", cardinality = Cardinality.ONE, label = "Id")
	public String getUsrId() {
		return usrId;
	}

	public void setUsrId(final String usrId) {
		this.usrId = usrId;
	}

	@Field(smartType = "STyLabel", cardinality = Cardinality.ONE, label = "FullName")
	public String getFullName() {
		return fullName;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	@Field(smartType = "STyLabel", cardinality = Cardinality.ONE, label = "Email")
	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Group'.
	 * @return String grpId
	 */
	@ForeignKey(smartType = "STyCode", label = "Group", fkDefinition = "DtUserGroup")
	public String getGrpId() {
		return (String) grpIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Motor type'.
	 * @param grpId Long
	 */
	public void setGrpId(final String grpId) {
		grpIdAccessor.setId(grpId);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
