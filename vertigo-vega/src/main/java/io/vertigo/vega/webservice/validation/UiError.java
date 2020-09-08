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
package io.vertigo.vega.webservice.validation;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.locale.MessageText;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtObject;

/**
 * Message d'IHM.
 * @author npiedeloup
 */
public final class UiError {
	private final MessageText messageText;
	private final DtObject dtObject;
	private final DtField dtField;

	/**
	 * Constructeur.
	 * @param dtObject Message
	 * @param dtField Object portant le message
	 * @param messageText Champs portant le message
	 */
	UiError(final DtObject dtObject, final DtField dtField, final MessageText messageText) {
		Assertion.check()
				.isNotNull(dtObject)
				.isNotNull(dtField)
				.isNotNull(messageText);
		//-----
		this.dtObject = dtObject;
		this.dtField = dtField;
		this.messageText = messageText;
	}

	/**
	 * Static method factory for UiErrorBuilder
	 * @return UiErrorBuilder
	 */
	public static UiErrorBuilder builder() {
		return new UiErrorBuilder();
	}

	/**
	 * @return Objet porteur de l'erreur
	 */
	public DtObject getDtObject() {
		return dtObject;
	}

	/**
	 * @return Champ porteur de l'erreur
	 */
	public DtField getDtField() {
		return dtField;
	}

	/**
	 * @return Message d'erreur
	 */
	public MessageText getErrorMessage() {
		return messageText;
	}

	/**
	 * @return Nom du champ
	 */
	public String getFieldName() {
		return dtField.getName();
	}
}
