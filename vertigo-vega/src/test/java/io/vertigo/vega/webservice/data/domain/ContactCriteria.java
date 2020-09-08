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

import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.stereotype.Field;

public final class ContactCriteria implements DtObject {

	private static final long serialVersionUID = 6839427455017031471L;

	//mandatory
	@Field(smartType = "STyTexte50", label = "Name")
	private String name;
	@Field(smartType = "STyTexte50", label = "Firstname")
	private String firstName;
	@Field(smartType = "STyLocalDate", label = "Birthday min")
	private LocalDate birthdayMin;
	@Field(smartType = "STyLocalDate", label = "Birthday max")
	private LocalDate birthdayMax;

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

	public LocalDate getBirthdayMin() {
		return birthdayMin;
	}

	public void setBirthdayMin(final LocalDate birthdayMin) {
		this.birthdayMin = birthdayMin;
	}

	public LocalDate getBirthdayMax() {
		return birthdayMax;
	}

	public void setBirthdayMax(final LocalDate birthdayMax) {
		this.birthdayMax = birthdayMax;
	}

}
