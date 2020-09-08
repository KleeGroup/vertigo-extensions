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
package io.vertigo.basics.formatter;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.structure.definitions.Formatter;
import io.vertigo.datamodel.structure.definitions.FormatterException;

/**
 * Gestion des formattages des identifiants.
 *
 * @author npiedeloup
 */
public final class FormatterId implements Formatter {
	/**
	 * Constructor.
	 * @param args Arguments
	 */
	public FormatterId(final String args) {
		//nothing
	}

	/** {@inheritDoc} */
	@Override
	public Long stringToValue(final String strValue, final BasicType dataType) throws FormatterException {
		Assertion.check().isTrue(dataType == BasicType.Long, "Formatter ne s'applique qu'aux Long");
		//---
		if (StringUtil.isBlank(strValue)) {
			return null;
		}
		try {
			return Long.valueOf(strValue.trim());
		} catch (final NumberFormatException e) {
			// cas des erreurs sur les formats de nombre
			throw (FormatterException) new FormatterException(Resources.DYNAMOX_NUMBER_NOT_FORMATTED)
					.initCause(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(final Object objValue, final BasicType dataType) {
		Assertion.check().isTrue(dataType == BasicType.Long, "Formatter ne s'applique qu'aux Long");
		//---
		if (objValue == null) {
			return "";
		}
		return String.valueOf(objValue);
	}
}
