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
import java.math.BigInteger;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.locale.MessageText;
import io.vertigo.datamodel.structure.definitions.Constraint;
import io.vertigo.datamodel.structure.definitions.Property;

/**
 * Manage BigDecimal's constraints.
 * The configuration is like the configuration of Database's decimal (DECIMAL(M,D)).
 * Where M is the maximum of digits (the precision) and D is the number of digits to the right of the decimal point (the scale).
 * The maximum number of digits to the left of the decimal point is check too and must be less than M-D.
 * @author mlaroche
 */
public final class ConstraintBigDecimal implements Constraint<String, BigDecimal> {

	private static final String SEPARATOR_ARGS = ",";
	private final Integer maxPrecision;
	private final Integer maxScale;
	private final MessageText errorMessage;

	/**
	 * Initialise les paramètres.
	 * @param args args but no args
	 */
	public ConstraintBigDecimal(final String args, final Optional<String> overrideMessageOpt) {
		final String[] beforeAfter = args.split(SEPARATOR_ARGS);
		Assertion.check().isTrue(beforeAfter.length == 2, "L'argument doit être au format M,D. M le nombre de chiffre au total (precision) et D le nombre de chiffre à droite de la virgule (scale).");
		try {
			maxPrecision = Integer.valueOf(beforeAfter[0]);
		} catch (final NumberFormatException e) {
			throw WrappedException.wrap(e, "{0} : first part is a not an integer", args);
		}
		try {
			maxScale = Integer.valueOf(beforeAfter[1]);
		} catch (final NumberFormatException e) {
			throw WrappedException.wrap(e, "{0} : second part is a not an integer", args);
		}
		// ---
		Assertion.check()
				.isNotNull(maxPrecision, "Le nombre de chiffres ne peut pas être null")
				.isNotNull(maxScale, "Le nombre de chiffres après la virgule ne peut pas être null")
				.isTrue(maxScale <= maxPrecision, "Le nombre de chiffres après la virgule doit être inférieur au nombre total de chiffres");
		errorMessage = overrideMessageOpt.isPresent() ? MessageText.of(overrideMessageOpt.get())
				: MessageText.of(Resources.DYNAMO_CONSTRAINT_DECIMAL_EXCEEDED,
						new BigDecimal(new BigInteger("1"), 0 - maxPrecision - maxScale),
						maxScale,
						maxPrecision - maxScale);
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final BigDecimal value) {
		if (value == null) {
			return true;
		}
		final BigDecimal noZero = value.stripTrailingZeros();
		final int scale = noZero.scale();
		final int precision = noZero.precision();
		return !(scale > maxScale || precision > maxPrecision || (precision - scale) > (maxPrecision - maxScale));
	}

	/** {@inheritDoc} */
	@Override
	public MessageText getErrorMessage() {
		return errorMessage;
	}

	/** {@inheritDoc} */
	@Override
	public Property<String> getProperty() {
		return new Property<>("numberFormat", String.class);
	}

	/** {@inheritDoc} */
	@Override
	public String getPropertyValue() {
		return new StringBuilder()
				.append(maxPrecision)
				.append(SEPARATOR_ARGS)
				.append(maxScale)
				.toString();
	}

}
