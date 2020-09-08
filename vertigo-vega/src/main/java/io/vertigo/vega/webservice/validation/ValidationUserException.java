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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VUserException;
import io.vertigo.core.locale.MessageText;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * Validation exception on a object's field.
 * @author npiedeloup (16 janv. 2015 15:03:33)
 */
public final class ValidationUserException extends VUserException {
	private static final long serialVersionUID = 7214302356640340103L;

	private static final MessageText VALIDATE_ERROR_MESSAGE_TEXT = MessageText.of("Il y a des erreurs, vous devez corriger votre saisie :");

	private final List<UiError> uiErrors = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public ValidationUserException() {
		this(Collections.emptyList());
	}

	/**
	 * Constructor.
	 * @param uiErrors Ui errors
	 */
	public ValidationUserException(final List<UiError> uiErrors) {
		super(VALIDATE_ERROR_MESSAGE_TEXT);
		this.uiErrors.addAll(uiErrors);
	}

	/**
	 * Create a UserException on a field
	 * @param messageText Message text
	 * @param dto object
	 * @param fieldName field
	 */
	public ValidationUserException(final MessageText messageText, final DtObject dto, final DtFieldName fieldName) {
		this(messageText, fieldName.name(), dto);
	}

	/**
	 * Create a UserException on a field
	 * @param messageText Message text
	 * @param dto object
	 * @param fieldName fieldName in CamelCase
	 */
	public ValidationUserException(final MessageText messageText, final DtObject dto, final String fieldName) {
		this(messageText, fieldName, dto);
	}

	private ValidationUserException(final MessageText messageText, final String constFieldName, final DtObject dto) {
		super(messageText);
		Assertion.check()
				.isNotNull(dto, "L'objet est obligatoire")
				.isNotBlank(constFieldName, "Le champs est obligatoire");
		//-----
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getField(constFieldName);
		uiErrors.add(new UiError(dto, dtField, messageText));
	}

	/**
	 * @param uiMessageStack Message stack to populate with this current exception
	 */
	public void flushToUiMessageStack(final UiMessageStack uiMessageStack) {
		for (final UiError uiError : uiErrors) {
			if (uiError.getDtObject() != null) {
				uiMessageStack.error(uiError.getErrorMessage().getDisplay(), uiError.getDtObject(), uiError.getFieldName());
			} else {
				uiMessageStack.error(uiError.getErrorMessage().getDisplay());
			}
		}
	}
}
