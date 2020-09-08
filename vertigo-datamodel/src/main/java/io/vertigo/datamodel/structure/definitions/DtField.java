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
package io.vertigo.datamodel.structure.definitions;

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.lang.JsonExclude;
import io.vertigo.core.locale.MessageText;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.definition.DefinitionReference;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.model.DtList;

/**
 * This class defines the structure of a field.
 *
 * A field represents a named and typed data
 *
 * A field
 *   - has a name
 *   - has a domain
 *   - has a fieldType
 *   - has a label
 *   - can be required
 *   - can be persistent
 *   - can be dynamic
 *
 * @author  fconstantin, pchretien , npiedeloup
 */
public final class DtField {

	private static final int FIELD_NAME_MAX_LENGTH = 30;
	/** Field definition Prefix. */
	public static final String PREFIX = "fld";

	/**
	 * This enum lists all types that can be used by a field.
	 * The most common types are ID and DATA
	 */
	public enum FieldType {
		/**
		 * identity
		 */
		ID,

		/**
		 * a simple data field
		 */
		DATA,

		/**
		 * a link towards an other entity
		 */
		FOREIGN_KEY,

		/**
		 * a computed field
		 */
		COMPUTED;

		/**
		 * @return if the field is the 'id'
		 */
		public boolean isId() {
			return this == ID;
		}
	}

	private final String name;
	private final FieldType type;
	private final Cardinality cardinality;
	private final DefinitionReference<SmartTypeDefinition> smartTypeRef;
	private final MessageText label;
	private final boolean persistent;

	/** Cas des FK ; référence à une FK. */
	private final String fkDtDefinitionName;

	private final String id;

	@JsonExclude
	private final DataAccessor dataAccessor;

	/**
	 * Constructor.
	 *
	 * @param id the ID of the field
	 * @param fieldName the name of the field
	 * @param type the type of the field
	 * @param smartType the smartType of the field
	 * @param cardinality cardinality of the field see {@code Cardinality}
	 * @param label the label of the field
	 * @param required if the field is required
	 * @param persistent if the field is persistent
	 * @param fkDtDefinitionName Nom de la DtDefinition de la FK (noNull si type=FK)
	 * @param computedExpression Expression du computed (noNull si type=Computed)
	 * @param dynamic if the field is dynamic
	 */
	DtField(
			final String id,
			final String fieldName,
			final FieldType type,
			final SmartTypeDefinition smartType,
			final MessageText label,
			final Cardinality cardinality,
			final boolean persistent,
			final String fkDtDefinitionName) {
		Assertion.check()
				.isNotBlank(id)
				.isNotNull(type)
				.isNotNull(smartType)
				.isNotNull(type)
				.isNotNull(cardinality);
		//-----
		this.id = id;
		smartTypeRef = new DefinitionReference<>(smartType);
		this.type = type;
		this.cardinality = cardinality;
		//-----
		Assertion.check()
				.isNotNull(fieldName)
				.isTrue(fieldName.length() <= FIELD_NAME_MAX_LENGTH, "the name of the field {0} has a limit size of {1}", fieldName, FIELD_NAME_MAX_LENGTH)
				.isTrue(StringUtil.isLowerCamelCase(fieldName), "the name of the field {0} must be in lowerCamelCase", fieldName);
		name = fieldName;
		//-----
		Assertion.check().isNotNull(label);
		this.label = label;
		//-----
		Assertion.check().isFalse(getType() == FieldType.COMPUTED && persistent, "a computed field can't be persistent");
		this.persistent = persistent;
		//-----
		if (getType() == FieldType.FOREIGN_KEY) {
			Assertion.check().isNotNull(fkDtDefinitionName, "Le champ {0} de type clé étrangère doit référencer une définition ", fieldName);
		} else {
			Assertion.check().isNull(fkDtDefinitionName, "Le champ {0} n''est pas une clé étrangère", fieldName);
		}
		this.fkDtDefinitionName = fkDtDefinitionName;
		//-----
		dataAccessor = new DataAccessor(this);
	}

	/**
	 * @return the key of the resource (i18n)
	 */
	public String getResourceKey() {
		return id;
	}

	/**
	 * @return the name of the field
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the cardinality of the field (one, optional, many)
	 */
	public Cardinality getCardinality() {
		return cardinality;
	}

	/**
	 * @return the type of the field
	 */
	public FieldType getType() {
		return type;
	}

	/**
	 * @return the smarttype of the field
	 */
	public SmartTypeDefinition getSmartTypeDefinition() {
		return smartTypeRef.get();
	}

	/**
	 * @return the label of the field
	 */
	public MessageText getLabel() {
		return label;
	}

	/**
	 * Gestion de la persistance.
	 * @return Si le champ est persisté.
	 */
	public boolean isPersistent() {
		return persistent;
	}

	public boolean isDtList() {
		return getSmartTypeDefinition().getScope().isDataObject() && cardinality.hasMany();
	}

	/**
	 *  @return DtDefinition de la ForeignKey (caractère obligatoire lié au type)
	 */
	//Todo changer le nom
	public DtDefinition getFkDtDefinition() {
		Assertion.check().isNotNull(fkDtDefinitionName);
		//-----
		return Node.getNode().getDefinitionSpace().resolve(fkDtDefinitionName, DtDefinition.class);
	}

	/**
	 * Returns the way to access the data.
	 * @return the data accessor.
	 */
	public DataAccessor getDataAccessor() {
		return dataAccessor;
	}

	/**
	 * Returns the class that holds the value of the field.
	 * If cardinality is many it's either a list or a dtList, if not then it's the base type of the domain.
	 * @return the data accessor.
	 */
	public Class getTargetJavaClass() {
		final SmartTypeDefinition domain = getSmartTypeDefinition();
		if (cardinality.hasMany()) {
			switch (domain.getScope()) {
				case PRIMITIVE:
					return List.class;
				case DATA_OBJECT:
					return DtList.class;
				case VALUE_OBJECT:
					return List.class;
				default:
					throw new IllegalStateException();
			}
		}
		return domain.getJavaClass();
	}
}
