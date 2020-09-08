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
package io.vertigo.basics.constraint;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.definitions.Constraint;
import io.vertigo.datamodel.structure.definitions.DtProperty;
import io.vertigo.datamodel.structure.definitions.Property;

/**
 * Implémentation de base des contraintes de longueur sur une donnée quelconque.
 * -numérique,
 * -textuelle.
 *
 * @author pchretien
 * @param <D> Type java de la valeur à contréler
 */
abstract class AbstractConstraintLength<D> implements Constraint<Integer, D> {
	/**
	 * Nombre maximum de caractères pour une chaine, de chiffres pour un entier...
	 */
	private final int maxLength;

	/**
	* @param max Nombre maximum de caractères, de chiffres...
	*/
	protected AbstractConstraintLength(final String max) {
		maxLength = Integer.parseInt(max);
		//-----
		Assertion.check().isTrue(maxLength > 0, "Longueur max doit être strictement positive");
	}

	/**
	 * @return int Nombre maximum de caractères, de chiffres...
	 */
	public final int getMaxLength() {
		return maxLength;
	}

	/** {@inheritDoc} */
	@Override
	public final Property getProperty() {
		return DtProperty.MAX_LENGTH;
	}

	/** {@inheritDoc} */
	@Override
	public final Integer getPropertyValue() {
		return getMaxLength();
	}
}
