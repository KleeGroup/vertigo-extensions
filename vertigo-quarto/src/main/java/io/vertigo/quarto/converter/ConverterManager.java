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
package io.vertigo.quarto.converter;

import io.vertigo.core.node.component.Manager;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Gestionnaire centralisé des conversions de documents.
 *
 * Exemple :
 *  - doc-->pdf
 *  - odt-->doc
 *
 * @author pchretien, npiedeloup
 */
public interface ConverterManager extends Manager {
	/**
	 * Conversion d'un document à un format cible.
	 *
	 * @param inputFile Document source à convertir
	 * @param format Format du document à cible
	 * @return Document converti au format passé en paramètre.
	 */
	VFile convert(VFile inputFile, String format);
}
