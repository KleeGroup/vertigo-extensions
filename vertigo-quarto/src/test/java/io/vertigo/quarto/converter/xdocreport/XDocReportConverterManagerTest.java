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
package io.vertigo.quarto.converter.xdocreport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.util.FileUtil;
import io.vertigo.core.util.TempFile;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.filestore.util.VFileUtil;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.quarto.QuartoFeatures;
import io.vertigo.quarto.converter.ConverterManager;

/**
 * Test de l'implémentation avec la librairie XDocReport.
 *
 * @author jgarnier
 */
public final class XDocReportConverterManagerTest {
	/** Logger. */
	private final Logger log = LogManager.getLogger(getClass());

	@Inject
	private ConverterManager converterManager;

	private VFile resultFile;

	private AutoCloseableNode node;

	@BeforeEach
	public void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public void tearDown() {
		resultFile = null; //Les fichiers temporaires étant en WeakRef, cela supprime le fichier
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new CommonsFeatures().build())
				.addModule(new DataStoreFeatures().build())
				.addModule(new QuartoFeatures()
						.withConverter()
						.withXDocReportConverter()
						.build())
				.build();
	}

	/**
	 * Converssion de Odt vers Pdf.
	 */
	@Test
	public void testConvertOdt2Pdf() throws IOException {
		final VFile inputFile = createVFile("../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "PDF");

		log("Odt2Pdf", resultFile);
	}

	@Test
	public void testConvertDocx2Pdf() throws IOException {
		final VFile inputFile = createVFile("../data/testFile.docx", this.getClass());
		resultFile = converterManager.convert(inputFile, "PDF");

		log("Docx2Pdf", resultFile);
	}

	private void log(final String methode, final VFile vFile) {
		log.info(methode + " => " + VFileUtil.obtainReadOnlyPath(vFile).toFile().getAbsolutePath());
	}

	private static VFile createVFile(final String fileName, final Class<?> baseClass) throws IOException {
		try (final InputStream in = baseClass.getResourceAsStream(fileName)) {
			Assertion.check().isNotNull(in, "fichier non trouvé : {0}", fileName);
			final String fileExtension = FileUtil.getFileExtension(fileName);
			final File file = new TempFile("tmpTest", '.' + fileExtension);
			FileUtil.copy(in, file);

			final String mimeType;
			if ("docx".equalsIgnoreCase(fileExtension)) {
				mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			} else if ("odt".equalsIgnoreCase(fileExtension)) {
				mimeType = "application/vnd.oasis.opendocument.text";
			} else {
				throw new IllegalArgumentException("File type not supported (" + fileExtension + ")");
			}

			return FSFile.of(file.getName(), mimeType, file.toPath());
		}
	}
}
