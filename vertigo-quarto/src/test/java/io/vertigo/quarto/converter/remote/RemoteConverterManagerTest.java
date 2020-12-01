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
package io.vertigo.quarto.converter.remote;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.util.FileUtil;
import io.vertigo.core.util.TempFile;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.filestore.util.VFileUtil;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.quarto.QuartoFeatures;
import io.vertigo.quarto.converter.ConverterManager;

/**
 * Test de l'implémentation standard.
 *
 * @author npiedeloup
 */
public final class RemoteConverterManagerTest {
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
						.withRemoteOpenOfficeConverter(
								Param.of("unohost", "docker-vertigo.part.klee.lan.net"),
								Param.of("unoport", "8997"),
								Param.of("convertTimeoutSeconds", "45"))
						.build())
				.build();
	}

	/**
	 * Converssion de Odt vers Odt.
	 * Liste des formats dans io.vertigo.quarto.plugins.converter.openoffice.ConverterFormat
	 */
	@Test
	public void testConvertOdt2Odt() {
		final VFile inputFile = createVFile("../data/testFile.odt", this.getClass());
		Assertions.assertThrows(IllegalStateException.class, () -> {
			resultFile = converterManager.convert(inputFile, "ODT");
			log("Odt2Odt", resultFile);
		});
	}

	/**
	 * Converssion de Odt vers Doc.
	 */
	@Test
	public void testConvertOdt2Doc() {
		final VFile inputFile = createVFile("../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "DOC");

		log("Odt2Doc", resultFile);
	}

	/**
	 * Converssion de Odt vers Rtf.
	 */
	@Test
	public void testConvertOdt2Rtf() {
		final VFile inputFile = createVFile("../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "RTF");

		log("Odt2Rtf", resultFile);
	}

	/**
	 * Converssion de Odt vers Pdf.
	 */
	@Test
	public void testConvertOdt2Pdf() {
		final VFile inputFile = createVFile("../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "PDF");

		log("Odt2Pdf", resultFile);
	}

	/**
	 * Converssion de Odt vers Txt.
	 */
	@Test
	public void testConvertOdt2Txt() {
		final VFile inputFile = createVFile("../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "TXT");

		log("Odt2Txt", resultFile);
	}

	/**
	 * Converssion de Txt vers Odt.
	 */
	@Test
	public void testConvertTxt2Odt() {
		final VFile inputFile = createVFile("../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "ODT");

		log("Txt2Odt", resultFile);
	}

	/**
	 * Converssion de Txt vers Doc.
	 */
	@Test
	public void testConvertTxt2Doc() {
		final VFile inputFile = createVFile("../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "DOC");

		log("Txt2Doc", resultFile);
	}

	/**
	 * Converssion de Txt vers Rtf.
	 */
	@Test
	public void testConvertTxt2Rtf() {
		final VFile inputFile = createVFile("../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "RTF");

		log("Txt2Rtf", resultFile);
	}

	/**
	 * Converssion de Txt vers Pdf.
	 */
	@Test
	public void testConvertTxt2Pdf() {
		final VFile inputFile = createVFile("../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "PDF");

		log("Txt2Pdf", resultFile);
	}

	/**
	 * Converssion de Txt vers Pdf.
	 */
	@Test
	public void testConvertTxt2Txt() {
		final VFile inputFile = createVFile("../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "PDF");

		log("Txt2Txt", resultFile);
	}

	private void log(final String methode, final VFile vFile) {
		log.info(methode + " => " + VFileUtil.obtainReadOnlyPath(vFile).toFile()
				.getAbsolutePath());
	}

	private static VFile createVFile(final String fileName, final Class<?> baseClass) {
		try (final InputStream in = baseClass.getResourceAsStream(fileName)) {
			final File file = new TempFile("tmp", '.' + FileUtil.getFileExtension(fileName));
			FileUtil.copy(in, file);
			return FSFile.of(file.toPath());
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}
}
