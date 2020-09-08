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
package io.vertigo.quarto.impl.publisher;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.quarto.publisher.PublisherManager;
import io.vertigo.quarto.publisher.model.PublisherData;

/**
 * Implémentation standard du manager des éditions.
 *
 * @author pchretien, npiedeloup
 */
public final class PublisherManagerImpl implements PublisherManager {
	private final MergerPlugin mergerPlugin;

	/**
	 * Constructeur.
	 * @param scriptManager Manager des scripts
	 * @param fileManager Manager des fichiers
	 */
	@Inject
	public PublisherManagerImpl(final ScriptManager scriptManager, final MergerPlugin mergerPlugin) {
		Assertion.check()
				.isNotNull(scriptManager)
				.isNotNull(mergerPlugin);
		//-----
		this.mergerPlugin = mergerPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public VFile publish(final String fileName, final URL modelFileURL, final PublisherData data) {
		Assertion.check()
				.isNotNull(fileName)
				.isNotNull(modelFileURL)
				.isNotNull(data);
		//-----
		try {
			return generateFile(fileName, modelFileURL, data);
		} catch (final IOException e) {
			final String msg = "La generation du fichier a echoue.<!-- " + e.getMessage() + "--> pour le fichier " + fileName;
			throw WrappedException.wrap(e, msg);
		}
	}

	private VFile generateFile(final String fileName, final URL modelFileURL, final PublisherData data) throws IOException {
		// attention : pour ce generateFile le File retourné n'a pas le nom de fichier donné dans
		// mergeParameter.getOuputFileName() car on utilise cette méthode notamment dans send
		// ci-dessus pour plusieurs utilisateurs simultanément avec probablement le même
		// mergeParameter.getOuputFileName()
		//-----
		final File fileToExport = mergerPlugin.execute(modelFileURL, data);
		return FSFile.of(fileName, mergerPlugin.getPublisherFormat().getMimeType(), fileToExport.toPath());
	}
}
