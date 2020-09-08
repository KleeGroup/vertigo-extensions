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

import java.math.BigDecimal;
import java.util.Optional;

import io.vertigo.core.locale.MessageText;

/**
 * Contrainte vérifiant que l'objet est : <ul>
 * <li>soit un BigDecimal comprit dans le segment ]-10^n, 10^n[</li>
 * <li>soit null </li>
 * </ul><br>.
 *
 * @author pchretien
 */
public final class ConstraintBigDecimalLength extends AbstractConstraintLength<BigDecimal> {
	/**
	 * Borne maximale au sens strict de BigDecimal (= 10 puissance maxLength)
	 */
	private final BigDecimal maxValue;

	/**
	 * Borne minimale au sens strict de BigDecimal (= - maxValue)
	 */
	private final BigDecimal minValue;

	private final MessageText errorMessage;

	/**
	 * Constructeur nécessaire pour le ksp.
	 * @param args Liste des arguments réduite à un seul castable en integer.
	 * Cet argument correspond au nombre de chifres maximum authorisé sur le BigDecimal.
	 * maxLength Valeur n du segment ]-10^n, 10^n[ dans lequel est comprise la valeur.
	 */
	public ConstraintBigDecimalLength(final String args, final Optional<String> overrideMessageOpt) {
		super(args);
		//-----
		maxValue = BigDecimal.valueOf(1L).movePointRight(getMaxLength());
		minValue = maxValue.negate();
		errorMessage = overrideMessageOpt.isPresent() ? MessageText.of(overrideMessageOpt.get()) : MessageText.of(Resources.DYNAMO_CONSTRAINT_DECIMALLENGTH_EXCEEDED, minValue, maxValue);
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final BigDecimal value) {
		return value == null
				|| value.compareTo(maxValue) < 0 && value.compareTo(minValue) > 0;
	}

	/** {@inheritDoc} */
	@Override
	public MessageText getErrorMessage() {
		return errorMessage;
	}
}
