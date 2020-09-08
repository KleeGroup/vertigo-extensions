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

import io.vertigo.core.locale.MessageText;

/**
 * Exception lancée en cas d'échec de vérification des contraintes.
 *
 * @author  pchretien
 */
public final class ConstraintException extends Exception {
	private static final long serialVersionUID = -7317938262923785124L;
	private final MessageText messageText;

	/**
	 * Constructor.
	 * @param messageText MessageText de l'erreur.
	 */
	public ConstraintException(final MessageText messageText) {
		super(messageText.getDisplay());
		this.messageText = messageText;
	}

	/**
	 * Gestion des messages d'erreur externalisés.
	 * @return messageText.
	 */
	public MessageText getMessageText() {
		return messageText;
	}
}
