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
package io.vertigo.quarto.publisher.definitions;

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.quarto.publisher.model.PublisherNode;

/**
 * Type des champs.
 *
 * @author npiedeloup, pchretien
 */
public enum PublisherFieldType {
	/**
	 * Champ de type chaine.
	 * Primitif.
	 */
	String,

	/**
	 * Champ de type boolean.
	 * Primitif.
	 */
	Boolean,

	/**
	 * Champ de type List.
	 */
	List,

	/**
	 * Champ de type Objet.
	 */
	Node,

	/**
	 * Champ de type Image.
	 */
	Image;

	/**
	 * Validation du type d'une valeur.
	 * @param value Valeur
	 * @return Si la valeur correspond au type du champ.
	 */
	public boolean checkValue(final Object value) {
		Assertion.check().isNotNull(value, "La valeur du champ est obligatoire.");
		//-----
		switch (this) {
			case Boolean:
				return value instanceof Boolean;
			case Node:
				return value instanceof PublisherNode;
			case List:
				if (!(value instanceof List<?>)) {
					return false;
				}
				//on teste le contenu de la liste, pas la liste elle même
				for (final Object object : (List<?>) value) {
					if (!PublisherFieldType.Node.checkValue(object)) {
						return false;
					}
				}
				return true;
			case Image:
				return value instanceof VFile;
			case String:
				return value instanceof String;
			default:
				throw new UnsupportedOperationException();
		}
	}
}
