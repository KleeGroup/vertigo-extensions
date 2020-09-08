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
package io.vertigo.quarto.impl.exporter;

import java.util.List;
import java.util.Map;

/**
 * Interface que doivent implémenter les contrôleurs possédant une action d'export.
 * 
 */
public interface ListExporter {

	/**
	 * Retourne le nom du fichier résultat de l'export.
	 * 
	 * @return nom du fichier résultat de l'export
	 */
	String getFileNameToExport();

	/**
	 * Retourne le nom du document résultat de l'export.<br/>
	 * 
	 * @return nom du document résultat de l'export
	 */
	String getDocumentTitle();

	/**
	 * Get the list of the columns that must be exported.
	 * 
	 * @return list of the columns that must be exported
	 */
	List<String> getColumnNameListToExport();

	/**
	 * Get the list of the columns that must be excluded when exporting the criterion.
	 * 
	 * @return list of the columns that must be excluded when exporting the criterion
	 */
	List<String> getExcludedCriterionColumnNameList();

	/**
	 * Get the map giving specific label to use for the column.
	 * 
	 * @return Map containing the specific label, or null if no specific label are needed
	 */
	Map<String, String> getSpecificLabelMap();

}
