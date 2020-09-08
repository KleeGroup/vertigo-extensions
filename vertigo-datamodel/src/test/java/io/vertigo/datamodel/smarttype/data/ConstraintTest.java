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
package io.vertigo.datamodel.smarttype.data;

import java.util.Optional;

import io.vertigo.core.locale.MessageText;
import io.vertigo.datamodel.structure.definitions.Constraint;
import io.vertigo.datamodel.structure.definitions.Property;

/**
 * Contrainte pour gérer la longueur des chaines de caractères.
 *
 * @author  plepaisant
 */
public final class ConstraintTest implements Constraint<Boolean, String> {

	/**
	 * @param args Liste des arguments réduite à un seul castable en integer.
	 * Cet argument correspond au nombre de caractères maximum authorisés sur la chaine de caractères.
	 */
	public ConstraintTest(final String args, final Optional<String> overrideMessageOpt) {
		//---
	}

	@Override
	public Property getProperty() {
		return new Property<>("test", Boolean.class);
	}

	@Override
	public Boolean getPropertyValue() {
		return true;
	}

	@Override
	public boolean checkConstraint(final String value) {
		return true;
	}

	@Override
	public MessageText getErrorMessage() {
		return MessageText.of("no message");
	}
}
