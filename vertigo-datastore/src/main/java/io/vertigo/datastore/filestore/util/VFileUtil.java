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
package io.vertigo.datastore.filestore.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.util.FileUtil;
import io.vertigo.core.util.TempFile;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.FSFile;

/**
 * Utilitaire de gestion des fichiers et flux associés.
 *
 * @author npiedeloup
 */
public final class VFileUtil {


	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private VFileUtil() {
		//rien
	}

	
	/**
	 * @param vFile FileInfo à lire
	 * @return Fichier physique readOnly (pour lecture d'un FileInfo)
	 */
	public static Path obtainReadOnlyPath(final VFile vFile) {
		final Path inputFile;
		if (vFile instanceof FSFile) {
			inputFile = ((FSFile) vFile).getFile();
		} else {
			inputFile = createTempFile(vFile);
		}
		return inputFile;
	}

	/**
	 * Crée un fichier temporaire à partir d'un fileInfo.
	 * Attention le processus appelant doit s'assurer de la suppression de ce fichier temporaire.
	 * @param vFile FileInfo à utiliser
	 * @return Fichier temporaire.
	 */
	private static Path createTempFile(final VFile vFile) {
		// TODO voir a ajouter une WeakRef sur FileInfo pour vérifier la suppression des fichiers temp après usage
		try {
			return doCreateTempPath(vFile); 
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't create temp file for FileInfo {0}", vFile.getFileName());
		}
	}

	private static Path doCreateTempPath(final VFile fileInfo) throws IOException {
		final File tmpFile = new TempFile("fileInfo", '.' + FileUtil.getFileExtension(fileInfo.getFileName()));
		try (final InputStream inputStream = fileInfo.createInputStream()) { 
			FileUtil.copy(inputStream, tmpFile);
			return tmpFile.toPath();
		}
	}
}
