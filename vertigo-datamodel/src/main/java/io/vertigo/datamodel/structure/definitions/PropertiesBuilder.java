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

import java.util.HashMap;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;

/**
 * Gestion de la flexibilité structurelle du modèle.
 * Permet d'ajouter des propriétés sur les concepts structurels.
 *
 * @author pchretien
 */
public final class PropertiesBuilder implements Builder<Properties> {
	private final Map<Property<?>, Object> properties = new HashMap<>();

	/**
	 * Constructeur.
	 */
	PropertiesBuilder() {
		super();
	}

	/**
	 * Ajout d'une propriété typée.
	 * @param <T> Property type
	 * @param property propriété
	 * @param value Valeur de la propriété
	 * @return builder
	 */
	public <T> PropertiesBuilder addValue(final Property<T> property, final T value) {
		Assertion.check()
				.isNotNull(property)
				.isFalse(properties.containsKey(property), "Propriété {0} déjà déclarée : ", property.getName());
		//On vérifie que la valeur est du bon type
		property.getType().cast(value);
		//-----
		properties.put(property, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Properties build() {
		return new Properties(properties);
	}
}
