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
package io.vertigo.quarto.impl.converter;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Plugin de Conversion des fichiers.
 * 
 * @author npiedeloup
 */
public interface ConverterPlugin extends Plugin {
	/**
	 * Retourne le fichier converti
	 * L'appel à l'OOO distant est synchronisé, car il supporte mal les converssions concurrentes.
	 * @param file Fichier à convertir
	 * @param targetFormat Type de document de sortie ODT,RTF,DOC,CSV,PDF
	 * @return Fichier converti
	 */
	VFile convertToFormat(final VFile file, final String targetFormat);

}
