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
package io.vertigo.social.mail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.util.TempFile;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.filestore.util.FileUtil;
import io.vertigo.datastore.impl.filestore.model.FSFile;

/**
 * Utilitaire pour construire des cas de tests.
 *
 * @author npiedeloup
 */
public final class TestUtil {
	/**
	 * Constructeur privé pour class utilitaire
	 *
	 */
	private TestUtil() {
		super();
	}

	/**
	 * Crée un VFile relativement d'un class de base.
	 * @param fileName Nom/path du fichier
	 * @param baseClass Class de base pour le chemin relatif
	 * @return VFile
	 */
	public static VFile createVFile(final String fileName, final Class<?> baseClass) {
		try (final InputStream in = baseClass.getResourceAsStream(fileName)) {
			Assertion.check().isNotNull(in, "fichier non trouvé : {0}", fileName);
			final File file = new TempFile("tmp", '.' + FileUtil.getFileExtension(fileName));
			FileUtil.copy(in, file);
			return FSFile.of(file.toPath());
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

}
