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
package io.vertigo.vega.webservice.data.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.stereotype.Field;
import io.vertigo.datamodel.structure.stereotype.ForeignKey;
import io.vertigo.datastore.impl.entitystore.StoreVAccessor;

public final class Contact implements KeyConcept {
	private static final long serialVersionUID = 2074906343392206381L;

	@Field(smartType = "STyId", type = "ID", cardinality = Cardinality.ONE, label = "Contact Id")
	private Long conId;
	@Field(smartType = "STyCode", label = "Honorific title")
	private String honorificCode;
	//mandatory
	@Field(smartType = "STyTexte50", cardinality = Cardinality.ONE, label = "Name")
	private String name;
	@Field(smartType = "STyTexte50", label = "Firstname")
	private String firstName;
	@Field(smartType = "STyLocalDate", label = "Birthday")
	private LocalDate birthday;
	@Field(smartType = "STyEmail", label = "Email")
	private String email;

	private List<String> tels;

	@io.vertigo.datamodel.structure.stereotype.Association(
			name = "AConAdr",
			fkFieldName = "adrId",
			primaryDtDefinitionName = "DtAddress",
			primaryIsNavigable = true,
			primaryRole = "Address",
			primaryLabel = "Address",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DtContact",
			foreignIsNavigable = false,
			foreignRole = "Contact",
			foreignLabel = "Contact",
			foreignMultiplicity = "0..*")
	private final StoreVAccessor<Address> adrIdAccessor = new StoreVAccessor<>(Address.class, "address");

	/** {@inheritDoc} */
	@Override
	public UID<Contact> getUID() {
		return UID.of(this);
	}

	public Long getConId() {
		return conId;
	}

	public void setConId(final Long conId) {
		this.conId = conId;
	}

	public String getHonorificCode() {
		return honorificCode;
	}

	public void setHonorificCode(final String honorificCode) {
		this.honorificCode = honorificCode;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(final LocalDate birthday) {
		this.birthday = birthday;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public StoreVAccessor<Address> getAddressAccessor() {
		return adrIdAccessor;
	}

	@ForeignKey(smartType = "STyId", label = "AdrId", fkDefinition = "DtAddress")
	public Long getAdrId() {
		return (Long) adrIdAccessor.getId();
	}

	public void setAdrId(final Long adrId) {
		adrIdAccessor.setId(adrId);
	}

	public List<String> getTels() {
		return Collections.unmodifiableList(tels);
	}

	public void setTels(final List<String> tels) {
		this.tels = new ArrayList<>(tels);
	}

	@Field(smartType = "STyTexte50", type = "COMPUTED", label = "All Text", cardinality = Cardinality.ONE)
	public String getAllText() {
		return "";
	}

}
