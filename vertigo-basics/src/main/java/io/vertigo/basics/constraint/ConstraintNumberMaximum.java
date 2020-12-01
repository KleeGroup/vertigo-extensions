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

import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.locale.MessageText;
import io.vertigo.datamodel.structure.definitions.Constraint;
import io.vertigo.datamodel.structure.definitions.DtProperty;
import io.vertigo.datamodel.structure.definitions.Property;

/**
 * Contrainte sur la valeur maximale d'un nombre.
 * arguments = valeur maximale.
 * @author npiedeloup
 */
public final class ConstraintNumberMaximum implements Constraint<Number, Number> {
	private final double maxValue;
	private final MessageText errorMessage;

	/**
	 * Constructor.
	 * @param args the maximum length
	 */
	public ConstraintNumberMaximum(final String args, final Optional<String> overrideMessageOpt) {
		Assertion.check()
				.isNotBlank(args, "Vous devez préciser la valeur maximum comme argument de ConstraintNumberMaximum")
				.isNotNull(overrideMessageOpt);
		//-----
		maxValue = Double.parseDouble(args);
		errorMessage = overrideMessageOpt.isPresent() ? MessageText.of(overrideMessageOpt.get()) : MessageText.of(Resources.DYNAMO_CONSTRAINT_NUMBER_MAXIMUM, maxValue);
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final Number value) {
		return value == null
				|| value.doubleValue() <= maxValue;
	}

	/** {@inheritDoc} */
	@Override
	public MessageText getErrorMessage() {
		return errorMessage;
	}

	/** {@inheritDoc} */
	@Override
	public Property getProperty() {
		return DtProperty.MAX_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	public Number getPropertyValue() {
		return maxValue;
	}
}
