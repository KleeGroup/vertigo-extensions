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
package io.vertigo.quarto.exporter.model;

/**
 * Formats de sortie supportés par le manager d'édition.
 * @author pchretien, npiedeloup
 */
public enum ExportFormat {

	/**
	 * Document Excel.
	 */
	XLS("application/vnd.ms-excel"),

	/**
	 * Document RTF.
	 */
	RTF("text/rtf"),

	/**
	 * Comma Separated Value.
	 */
	CSV("application/csv.ms-excel"),

	/**
	 * Document PDF.
	 */
	PDF("application/pdf"),

	/**
	 * Document ODS.
	 */
	ODS("application/vnd.oasis.opendocument.spreadsheet");

	private final String typeMime;

	/**
	 * Constructor.
	 * @param typeMime Type mime associé
	 */
	private ExportFormat(final String typeMime) {
		this.typeMime = typeMime;
	}

	/**
	 * @return Type mime associé
	 */
	public String getTypeMime() {
		return typeMime;
	}
}
