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

/**
 * Contrainte vérifiant que l'objet est : <ul>
 * <li>soit un Long comprenant au maximum le nombre de chiffres précisé à la construction (nombres de digits)</li>
 * <li>soit null</li>
 * </ul><br>
 * On rappelle que le maximum d'un type Long est compris entre 1O^18 et 10^19 <br>
 * On conseille donc d'utiliser 10^18 comme structure de stockage max en BDD : donc number(18) <br>
 * Si vous souhaitez flirter avec les 10^19 alors n'utilisez pas de contraintes.
 *
 * @author pchretien
 */
public final class ConstraintLongLength extends AbstractConstraintLength<Long> {
	private static final int MAX_LENGHT = 19;

	/**
	 * Borne maximale au sens strict de Long (= 10 puissance maxLength)
	 */
	private final long maxValue;

	/**
	 * Borne minimale au sens strict de Long (= - maxValue)
	 */
	private final long minValue;

	private final MessageText errorMessage;

	/**
	 * @param args Liste des arguments réduite à un seul castable en long.
	 * Cet argument correspond au nombre de chifres maximum authorisé sur le Long.
	 */
	public ConstraintLongLength(final String args, final Optional<String> overrideMessageOpt) {
		super(args);
		//-----
		Assertion.check().isTrue(getMaxLength() < MAX_LENGHT, "Longueur max doit être strictement inférieure à " + MAX_LENGHT);
		//-----
		long tmpMaxValue = 1;
		for (int i = 0; i < getMaxLength(); i++) {
			tmpMaxValue *= 10;
		}
		maxValue = tmpMaxValue;
		minValue = -tmpMaxValue;
		//---
		errorMessage = overrideMessageOpt.map(MessageText::of).orElseGet(() -> MessageText.of(Resources.DYNAMO_CONSTRAINT_LONGLENGTH_EXCEEDED, minValue, maxValue));
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final Long value) {
		return value == null
				|| value > minValue && value < maxValue;
	}

	/** {@inheritDoc} */
	@Override
	public MessageText getErrorMessage() {
		return errorMessage;
	}
}
