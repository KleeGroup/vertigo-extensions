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
package io.vertigo.quarto.plugins.converter.xdocreport;

import java.util.Locale;

import io.vertigo.core.lang.Assertion;

/**
 * Formats d'entrée et sortie supportés par le lpugin XDocReport.
 * @author jgarnier
 */
enum ConverterFormat {

	/**
	 * OpenOffice Text.
	 */
	ODT("application/vnd.oasis.opendocument.text"),

	/**
	 * Document Word XML.
	 */
	DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

	/**
	 * Document PDF.
	 */
	PDF("application/pdf"),;

	private final String typeMime;

	/**
	 * Constructeur.
	 * @param typeMime Type mime associé
	 */
	private ConverterFormat(final String typeMime) {
		this.typeMime = typeMime;
	}

	/**
	 * @return Type mime associé
	 */
	String getTypeMime() {
		return typeMime;
	}

	/**
	 * Récupère le Format associé à ce code de format.
	 * @param sFormat code de format (non null et doit être en majuscule)
	 * @return Format associé.
	 */
	static ConverterFormat find(final String sFormat) {
		Assertion.check()
				.isNotNull(sFormat)
				.isTrue(sFormat.equals(sFormat.trim().toUpperCase(Locale.ENGLISH)), "Le format doit être en majuscule, et sans espace");
		//-----
		return valueOf(sFormat);
	}
}
