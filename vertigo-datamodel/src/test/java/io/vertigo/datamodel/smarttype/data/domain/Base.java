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
package io.vertigo.datamodel.smarttype.data.domain;

import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.model.VAccessor;
import io.vertigo.datamodel.structure.stereotype.Field;
import io.vertigo.datamodel.structure.stereotype.ForeignKey;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

public final class Base implements KeyConcept {
	private static final long serialVersionUID = 1L;

	private Long baseId;
	private String code;
	private String name;
	private Integer healthLevel;
	private java.time.LocalDate creationDate;
	private String description;
	private String geoLocation;
	private java.math.BigDecimal assetsValue;
	private java.math.BigDecimal rentingFee;
	private String tags;

	@io.vertigo.datamodel.structure.stereotype.Association(
			name = "ABaseGeosector",
			fkFieldName = "geosectorId",
			primaryDtDefinitionName = "DtGeosector",
			primaryIsNavigable = true,
			primaryRole = "Geosector",
			primaryLabel = "Base Geosector",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DtBase",
			foreignIsNavigable = false,
			foreignRole = "Base",
			foreignLabel = "Base",
			foreignMultiplicity = "0..*")
	private final VAccessor<Geosector> geosectorIdAccessor = new VAccessor<>(Geosector.class, "Geosector");

	/** {@inheritDoc} */
	@Override
	public UID<Base> getUID() {
		return UID.of(this);
	}

	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id'.
	 * @return Long baseId <b>Obligatoire</b>
	 */
	@Field(smartType = "STyId", type = "ID", cardinality = io.vertigo.core.lang.Cardinality.ONE, label = "Id")
	public Long getBaseId() {
		return baseId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id'.
	 * @param baseId Long <b>Obligatoire</b>
	 */
	public void setBaseId(final Long baseId) {
		this.baseId = baseId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Code'.
	 * @return String code <b>Obligatoire</b>
	 */
	@Field(smartType = "STyCode", cardinality = io.vertigo.core.lang.Cardinality.ONE, label = "Code")
	public String getCode() {
		return code;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Code'.
	 * @param code String <b>Obligatoire</b>
	 */
	public void setCode(final String code) {
		this.code = code;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Name'.
	 * @return String name
	 */
	@Field(smartType = "STyLabel", label = "Name")
	public String getName() {
		return name;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Name'.
	 * @param name String
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Health Level'.
	 * @return Integer healthLevel
	 */
	@Field(smartType = "STyHealth", label = "Health Level")
	public Integer getHealthLevel() {
		return healthLevel;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Health Level'.
	 * @param healthLevel Integer
	 */
	public void setHealthLevel(final Integer healthLevel) {
		this.healthLevel = healthLevel;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Creation Date'.
	 * @return LocalDate creationDate
	 */
	@Field(smartType = "STyLocaldate", label = "Creation Date")
	public java.time.LocalDate getCreationDate() {
		return creationDate;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Creation Date'.
	 * @param creationDate LocalDate
	 */
	public void setCreationDate(final java.time.LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Description'.
	 * @return String description
	 */
	@Field(smartType = "STyDescription", label = "Description")
	public String getDescription() {
		return description;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Description'.
	 * @param description String
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Geographic Location'.
	 * @return String geoLocation
	 */
	@Field(smartType = "STyLabel", label = "Geographic Location")
	public String getGeoLocation() {
		return geoLocation;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Geographic Location'.
	 * @param geoLocation String
	 */
	public void setGeoLocation(final String geoLocation) {
		this.geoLocation = geoLocation;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Current base assets value'.
	 * @return BigDecimal assetsValue
	 */
	@Field(smartType = "STyCurrency", label = "Current base assets value")
	public java.math.BigDecimal getAssetsValue() {
		return assetsValue;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Current base assets value'.
	 * @param assetsValue BigDecimal
	 */
	public void setAssetsValue(final java.math.BigDecimal assetsValue) {
		this.assetsValue = assetsValue;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Renting Fee'.
	 * @return BigDecimal rentingFee
	 */
	@Field(smartType = "STyCurrency", label = "Renting Fee")
	public java.math.BigDecimal getRentingFee() {
		return rentingFee;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Renting Fee'.
	 * @param rentingFee BigDecimal
	 */
	public void setRentingFee(final java.math.BigDecimal rentingFee) {
		this.rentingFee = rentingFee;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Tags'.
	 * @return String tags
	 */
	@Field(smartType = "STyMultipleIds", label = "Tags")
	public String getTags() {
		return tags;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Tags'.
	 * @param tags String
	 */
	public void setTags(final String tags) {
		this.tags = tags;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Base Geosector'.
	 * @return Long geosectorId
	 */
	@ForeignKey(smartType = "STyId", label = "Base Geosector", fkDefinition = "DtGeosector")
	public Long getGeosectorId() {
		return (Long) geosectorIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Base Geosector'.
	 * @param geosectorId Long
	 */
	public void setGeosectorId(final Long geosectorId) {
		geosectorIdAccessor.setId(geosectorId);
	}

	/**
	 * Association : Base Geosector.
	 * @return l'accesseur vers la propriété 'Base Geosector'
	 */
	public VAccessor<Geosector> geosector() {
		return geosectorIdAccessor;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
