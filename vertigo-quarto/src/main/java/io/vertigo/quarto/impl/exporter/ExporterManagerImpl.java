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

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.util.TempFile;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.quarto.exporter.ExporterManager;
import io.vertigo.quarto.exporter.model.Export;
import io.vertigo.quarto.exporter.model.ExportFormat;

/**
 * Implémentation standard du manager des exports.
 *
 * @author pchretien, npiedeloup
 */
public final class ExporterManagerImpl implements ExporterManager {
	private final List<ExporterPlugin> exporterPlugins;

	/**
	 * Constructor.
	 */
	@Inject
	public ExporterManagerImpl(final List<ExporterPlugin> exporterPlugins) {
		Assertion.check()
				.isNotNull(exporterPlugins);
		//-----
		this.exporterPlugins = Collections.unmodifiableList(exporterPlugins);
	}

	/**
	 * Récupère le plugin d'export associé au format.
	 *
	 * @param exportFormat Format d'export souhaité
	 * @return Plugin d'export associé au format
	 */
	private ExporterPlugin getExporterPlugin(final ExportFormat exportFormat) {
		Assertion.check().isNotNull(exportFormat);
		//-----
		return exporterPlugins
				.stream()
				.filter(exporterPlugin -> exporterPlugin.accept(exportFormat))
				.findFirst()
				.orElseThrow(() -> new VSystemException("aucun plugin trouve pour le format {0}", exportFormat));
	}

	/** {@inheritDoc} */
	@Override
	public VFile createExportFile(final Export export) {
		Assertion.check().isNotNull(export);
		//-----
		try {
			return generateFile(export);
		} catch (final Exception e) {
			// Quelle que soit l'exception on l'encapsule pour préciser le nom
			// du fichier.
			final String msg = "La génération du fichier a échoué.<!-- " + e.getMessage() + "--> pour le fichier " + export.getFileName();
			throw WrappedException.wrap(e, msg);
		}
	}

	/**
	 * Ecrire dans un fichier temporaire en passant par le writer le données (CSV, XML, DOC,...).
	 *
	 * @return Fichier temporaire généré
	 * @param export Paramètres de l'export
	 * @throws Exception Exception lors de la création du fichier
	 */
	private VFile generateFile(final Export export) throws Exception {
		final ExporterPlugin exporterPlugin = getExporterPlugin(export.getFormat());

		final File file = new TempFile("csvGenerated", "." + export.getFormat().name().toLowerCase(Locale.ENGLISH));
		try (final OutputStream fileOutputStream = Files.newOutputStream(file.toPath())) {
			exporterPlugin.exportData(export, fileOutputStream);
		} catch (final Exception e) {
			if (!file.delete()) {
				file.deleteOnExit();
			}
			throw e;
		}
		return FSFile.of(export.getFileName() + "." + export.getFormat().name().toLowerCase(Locale.ENGLISH), export.getFormat().getTypeMime(), file.toPath());
	}

}
