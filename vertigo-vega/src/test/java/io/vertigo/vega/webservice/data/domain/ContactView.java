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
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.stereotype.Field;

public final class ContactView implements DtObject {
	private static final long serialVersionUID = 2074906343392206381L;

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
	@Field(smartType = "STyDtAddress", label = "Addresses connues", cardinality = Cardinality.MANY, persistent = false)
	private DtList<Address> addresses;

	private List<String> tels;

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

	public DtList<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(final DtList<Address> addresses) {
		this.addresses = addresses;
	}

	public List<String> getTels() {
		return Collections.unmodifiableList(tels);
	}

	public void setTels(final List<String> tels) {
		this.tels = new ArrayList<>(tels);
	}

}
