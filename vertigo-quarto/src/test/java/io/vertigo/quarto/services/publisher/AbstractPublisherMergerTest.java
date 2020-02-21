/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.quarto.services.publisher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.Home;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.quarto.impl.services.publisher.PublisherDataUtil;
import io.vertigo.quarto.services.publisher.data.domain.Address;
import io.vertigo.quarto.services.publisher.data.domain.Enquete;
import io.vertigo.quarto.services.publisher.data.domain.Enqueteur;
import io.vertigo.quarto.services.publisher.data.domain.MisEnCause;
import io.vertigo.quarto.services.publisher.data.domain.PublisherMock;
import io.vertigo.quarto.services.publisher.data.domain.Ville;
import io.vertigo.quarto.services.publisher.metamodel.PublisherDataDefinition;
import io.vertigo.quarto.services.publisher.model.PublisherData;
import io.vertigo.quarto.services.publisher.model.PublisherNode;
import io.vertigo.util.TempFile;

/**
 * Test de l'implémentation standard.
 *
 * @author npiedeloup
 */
public abstract class AbstractPublisherMergerTest extends AbstractTestCaseJU5 {
	private static final boolean KEEP_OUTPUT_FILE = false;
	//Répertoire de test
	private static String OUTPUT_PATH = "c:/tmp/";

	private static final String DATA_PACKAGE = "io/vertigo/quarto/services/publisher/data/documents/";

	@Inject
	private PublisherManager publisherManager;
	@Inject
	private ResourceManager resourceManager;
	@Inject
	private FileManager fileManager;

	/**
	 * @return Extension du model utilisé
	 */
	protected abstract String getExtension();

	@Test
	public void testMergerSimple() {
		final PublisherMock reportData = createTestPublisher();

		final PublisherData publisherData = createPublisherData("PuPublisherMock");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModel." + getExtension());
		final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusion." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		Assertions.assertNotNull(result);
	}

	@Test
	public void testMergerSpecialsCharacters() {
		final PublisherMock reportData = createTestPublisher();

		final PublisherData publisherData = createPublisherData("PuPublisherMock");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());
		publisherData.getRootNode().setString("commentaire", " euro:" + (char) 128 + "\n gt:>\n lt:<\n tab:>\t<\n cr:>\n<\n  amp:&\n dquote:\"\n squote:\'\n");

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModel." + getExtension());
		final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusionSpecialsCharacters." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		Assertions.assertNotNull(result);
	}

	@Test
	public void testMergerErrorModel() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			final PublisherMock reportData = createTestPublisher();
			final PublisherData publisherData = createPublisherData("PuPublisherMock");
			PublisherDataUtil.populateData(reportData, publisherData.getRootNode());
			final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelError." + getExtension());
			final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusionError." + getExtension(), modelFileURL, publisherData);
			nop(result);
			Assertions.fail("La fusion ne doit pas être possible quand le modèle contient une erreur");
		});
	}

	@Test
	public void testMergerModelIfEquals() {
		final PublisherMock reportData = createTestPublisher();
		final PublisherData publisherData = createPublisherData("PuPublisherMock");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());
		publisherData.getRootNode().setString("titre", "NOM");
		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelIfEquals." + getExtension());
		final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusionIfEquals." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		Assertions.assertNotNull(result);
	}

	@Test
	public void testMergerModelIfNotEquals() {
		final PublisherMock reportData = createTestPublisher();
		final PublisherData publisherData = createPublisherData("PuPublisherMock");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());
		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelIfEquals." + getExtension());
		final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusionIfNotEquals." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		Assertions.assertNotNull(result);
	}

	@Test
	public void testMergerEnquete() {
		final Enquete dtoEnquete = DataHelper.createEnquete();
		final Enqueteur dtoEnqueteur = DataHelper.createEnqueteur();
		final Address dtoAdresse = DataHelper.createAdresse();
		final Ville dtoVille = DataHelper.createVille();
		final DtList<? extends MisEnCause> dtcMisEnCause = DataHelper.createMisEnCauseList();

		final PublisherData publisherData = createPublisherData("PuEnquete");
		final PublisherNode pnEnquete = publisherData.getRootNode();
		PublisherDataUtil.populateData(dtoEnquete, pnEnquete);

		final PublisherNode pnEnqueteur = pnEnquete.createNode("enqueteur");
		PublisherDataUtil.populateData(dtoEnqueteur, pnEnqueteur);
		pnEnquete.setNode("enqueteur", pnEnqueteur);

		final PublisherNode pnAdresse = pnEnqueteur.createNode("adresseRatachement");
		PublisherDataUtil.populateData(dtoAdresse, pnAdresse);
		pnEnqueteur.setNode("adresseRatachement", pnAdresse);

		final PublisherNode pnVille = pnAdresse.createNode("ville");
		PublisherDataUtil.populateData(dtoVille, pnVille);
		pnAdresse.setNode("ville", pnVille);

		final List<PublisherNode> publisherNodes = new ArrayList<>();
		for (final MisEnCause dtoMisEnCause : dtcMisEnCause) {
			final PublisherNode pnMisEnCause = pnEnquete.createNode("misEnCause");
			PublisherDataUtil.populateData(dtoMisEnCause, pnMisEnCause);
			publisherNodes.add(pnMisEnCause);
		}
		pnEnquete.setNodes("misEnCause", publisherNodes);

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelEnquete." + getExtension());
		final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusionEnquete." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		Assertions.assertNotNull(result);
	}

	@Test
	public void testMergerEnquetePerField() {
		final Enquete dtoEnquete = DataHelper.createEnquete();
		final Enqueteur dtoEnqueteur = DataHelper.createEnqueteur();
		final Address dtoAdresse = DataHelper.createAdresse();
		final Ville dtoVille = DataHelper.createVille();
		final DtList<? extends MisEnCause> dtcMisEnCause = DataHelper.createMisEnCauseList();

		final PublisherData publisherData = createPublisherData("PuEnquete");
		final PublisherNode pnEnquete = publisherData.getRootNode();
		PublisherDataUtil.populateData(dtoEnquete, pnEnquete);

		final PublisherNode pnEnqueteur = PublisherDataUtil.populateField(pnEnquete, "enqueteur", dtoEnqueteur);
		final PublisherNode pnAdresse = PublisherDataUtil.populateField(pnEnqueteur, "adresseRatachement", dtoAdresse);
		PublisherDataUtil.populateField(pnAdresse, "ville", dtoVille);
		PublisherDataUtil.populateField(pnEnquete, "misEnCause", dtcMisEnCause);

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelEnquete." + getExtension());
		final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusionEnquetePerField." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		Assertions.assertNotNull(result);
	}

	@Test
	public void testMergerBlock() {
		final PublisherMock reportData = createTestPublisher();

		final PublisherData publisherData = createPublisherData("PuPublisherMock");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModel2." + getExtension());
		final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusionBlock." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		Assertions.assertNotNull(result);
	}

	@Test
	public void testMergerImage() throws IOException {
		final PublisherMock reportData = createTestPublisher();

		final PublisherData publisherData = createPublisherData("PuPublisherMock");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelImage." + getExtension());
		final VFile image = createVFile(fileManager, "/" + DATA_PACKAGE + "logo.jpg", getClass());
		publisherData.getRootNode().setImage("logo", image);
		//
		final VFile result = publisherManager.publish(OUTPUT_PATH + "testFusionImage." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		Assertions.assertNotNull(result);
	}

	//	/**
	//	 */
	//	public void testMergerImageJpg() {
	//		final DtObject reportData = createTestPublisher();
	//		final PublisherDataDtoExtractor dataExtractor = new PublisherDataDtoExtractor(reportData);
	//
	//		final VFile image = TestUtil.createVFile("data/logo.jpg", getClass());
	//		final PublisherData publisherData = dataExtractor.getPublisherData();
	//		publisherData.getRoot().addImage("LOGO", image);
	//		final PublisherWork publisherWork = publisherManager.createWork("c:/xxx/testFusionImage."+getExtension(), PACKAGE + "/data/ExempleModelImage."+getExtension(), "report", publisherData);
	//
	//		final VFile result = workManager.process(publisherWork);
	//		log.trace("testMergerImageJpg result : " + result);
	//		assertTrue(true);
	//	}
	//
	//	/**
	//	 */
	//	public void testMergerImageRatio() {
	//		final DtObject reportData = createTestPublisher();
	//		final PublisherDataDtoExtractor dataExtractor = new PublisherDataDtoExtractor(reportData);
	//
	//		final VFile image = TestUtil.createVFile("data/logoMasque.gif", getClass());
	//		final PublisherData publisherData = dataExtractor.getPublisherData();
	//		publisherData.getRoot().addImage("LOGO", image);
	//		final PublisherWork publisherWork = publisherManager.createWork("c:/xxx/testFusionImage2."+getExtension(), PACKAGE + "/data/ExempleModelImage2."+getExtension(), "report", publisherData);
	//
	//		final VFile result = workManager.process(publisherWork);
	//		log.trace("testMergerImageRatio result : " + result);
	//		assertTrue(true);
	//	}

	protected static PublisherMock createTestPublisher() {
		final PublisherMock reportData = new PublisherMock();
		reportData.setTitre("Mon titre");
		reportData.setNom("BITUM");
		reportData.setPrenom("John");
		reportData.setAddress("38, rue de la corniche\n01345 - rivera");
		reportData
				.setCommentaire(
						"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\tLorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\t1-\tLorem ipsum dolor\n\t2-\tLorem ipsum dolor\n\t3-\tLorem ipsum dolor");
		reportData.setBoolean1(true);
		reportData.setBoolean2(false);
		reportData.setBoolean3(true);
		reportData.setTestDummy(
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.");
		reportData.setTestLong(45676L);
		reportData.setTestDouble(79613D);
		reportData.setTestInteger(79413);
		reportData.setTestDate(LocalDate.now());

		//		final DtList<DtObject> testList = TestHelper.createDtList(AbstractPublisherMock.DEFINITION_URN);
		//		for (int i = 0; i < 50; i++) {
		//			testList.add(createTestPublisherRandom(String.valueOf(i)));
		//		}
		//		setFieldValue(reportData, "DTC_TEST", testList);

		return reportData;
	}

	//	private DtObject createTestPublisherRandom(final String name) {
	//		final DtObject reportData = TestHelper.createDtObject(AbstractPublisherMock.DEFINITION_URN);
	//		setFieldValue(reportData, "TITRE", "Obj dyn " + name);
	//		setFieldValue(reportData, "NOM", "BITUM");
	//		setFieldValue(reportData, "PRENOM", "John");
	//		setFieldValue(reportData, "ADDRESS", "38, rue de la corniche\n01345 - rivera");
	//		setFieldValue(reportData, "COMMENTAIRE", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\tLorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\t1-\tLorem ipsum dolor\n\t2-\tLorem ipsum dolor\n\t3-\tLorem ipsum dolor");
	//		setFieldValue(reportData, "TEST_LONG", Math.round(Math.random() * 1000));
	//		setFieldValue(reportData, "TEST_DOUBLE", Math.random() * 1000);
	//		setFieldValue(reportData, "TEST_INTEGER", (int) Math.round(Math.random() * 100));
	//		setFieldValue(reportData, "TEST_DATE", new Date());
	//		return reportData;
	//	}

	protected static PublisherData createPublisherData(final String definitionName) {
		final PublisherDataDefinition publisherDataDefinition = Home.getApp().getDefinitionSpace().resolve(definitionName, PublisherDataDefinition.class);
		Assertions.assertNotNull(publisherDataDefinition);

		final PublisherData publisherData = new PublisherData(publisherDataDefinition);
		Assertions.assertNotNull(publisherData);

		return publisherData;
	}

	private static void save(final VFile result) {
		try (final InputStream in = result.createInputStream()) {
			FileUtil.copy(in, new File(result.getFileName()));
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	private static VFile createVFile(final FileManager fileManager, final String fileName, final Class<?> baseClass) throws IOException {
		try (final InputStream in = baseClass.getResourceAsStream(fileName)) {
			Assertion.checkNotNull(in, "fichier non trouvé : {0}", fileName);
			final String fileExtension = FileUtil.getFileExtension(fileName);
			final File file = new TempFile("tmp", '.' + fileExtension);
			FileUtil.copy(in, file);

			final String mimeType;
			if ("jpg".equalsIgnoreCase(fileExtension)) {
				mimeType = "image/jpeg";
			} else if ("png".equalsIgnoreCase(fileExtension)) {
				mimeType = "image/png";
			} else {
				throw new IllegalArgumentException("File type not supported (" + fileExtension + ")");
			}

			return fileManager.createFile(file.getName(), mimeType, file);
		}
	}

}
