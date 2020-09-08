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
package io.vertigo.datastore.filestore.data.domain.fileinfo;

import io.vertigo.datastore.filestore.definitions.FileInfoDefinition;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.AbstractFileInfo;

/**
 * Attention cette classe est générée automatiquement !
 * Objet représentant un fichier persistant FileInfoTemp
 */
public final class FileInfoFs extends AbstractFileInfo {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur par défaut.
	 * @param vFile Données du fichier
	 */
	public FileInfoFs(final VFile vFile) {
		super(FileInfoDefinition.findFileInfoDefinition(FileInfoFs.class), vFile);
	}
}
