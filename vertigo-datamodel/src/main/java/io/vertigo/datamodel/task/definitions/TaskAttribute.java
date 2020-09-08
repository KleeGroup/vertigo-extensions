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
package io.vertigo.datamodel.task.definitions;

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.Node;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.ConstraintException;
import io.vertigo.datamodel.structure.model.DtList;

/**
 * Attribut d'une tache.
 * Il s'agit soit :
 *  - d'un type primitif
 *  - d'un type complexe : DTO ou DTC
 * Dans tous les cas il s'agit d'un {@link io.vertigo.dynamo.domain.metamodel.Domain}.
 *
 * Le paramètre peut être :
 *
 *  - obligatoire ou facultatif
 *
 * @author  fconstantin, pchretien
 */
public final class TaskAttribute {
	/** Name of the attribute. */
	private final String name;

	private final SmartTypeDefinition smartTypeDefinition;

	/** if the attribute cardinality. */
	private final Cardinality cardinality;

	/**
	 * Constructor.
	 *
	 * @param attributeName the name of the attribute
	 * @param smartType the smartType of the attribute
	 * @param cardinality cardinality of the attribute see {@code Cardinality}
	 * @param required if the attribute is required
	 */
	TaskAttribute(final String attributeName, final SmartTypeDefinition smartTypeDefinition, final Cardinality cardinality) {
		Assertion.check()
				.isNotNull(attributeName)
				.isNotNull(smartTypeDefinition)
				.isNotNull(cardinality)
				.isTrue(StringUtil.isLowerCamelCase(attributeName), "the name of the attribute {0} must be in lowerCamelCase", attributeName);
		//-----
		name = attributeName;
		this.smartTypeDefinition = smartTypeDefinition;
		this.cardinality = cardinality;
	}

	/**
	 * @return the name of the attribute.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Domain the domain
	 */
	public SmartTypeDefinition getSmartTypeDefinition() {
		return smartTypeDefinition;
	}

	/**
	 * @return if the attribute cardinality
	 */
	public Cardinality getCardinality() {
		return cardinality;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "{ name : " + name + ", smarttype :" + smartTypeDefinition + ", cardinality :" + cardinality + "]";
	}

	/**
	 * Vérifie la cohérence des arguments d'un Attribute
	 * Vérifie que l'objet est cohérent avec le type défini sur l'attribut.
	 * @param value Valeur (Object primitif ou DtObject ou bien DtList)
	 */
	public void checkAttribute(final Object value) {
		final SmartTypeManager smartTypeManager = Node.getNode().getComponentSpace().resolve(SmartTypeManager.class);
		if (cardinality.hasOne()) {
			Assertion.check().isNotNull(value, "Attribut task {0} ne doit pas etre null (cf. paramétrage task)", getName());
		}
		try {
			if (cardinality.hasMany()) {
				if (!(value instanceof List)) {
					throw new ClassCastException("Value " + value + " must be a list");
				}
				for (final Object element : List.class.cast(value)) {
					smartTypeManager.checkConstraints(getSmartTypeDefinition(), element);
				}
			} else {
				smartTypeManager.checkConstraints(getSmartTypeDefinition(), value);
			}
		} catch (final ConstraintException e) {
			//On retransforme en Runtime pour conserver une API sur les getters et setters.
			throw WrappedException.wrap(e);
		}
	}

	/**
	 * Returns the class that holds the value of the field.
	 * If cardinality is many it's either a list or a dtList, if not then it's the base type of the domain.
	 * @return the data accessor.
	 */
	public Class getTargetJavaClass() {
		if (cardinality.hasMany()) {
			switch (smartTypeDefinition.getScope()) {
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
		return smartTypeDefinition.getJavaClass();
	}
}
