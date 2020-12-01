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
package io.vertigo.datastore.filestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.util.FileUtil;
import io.vertigo.datastore.TestUtil;
import io.vertigo.datastore.filestore.data.domain.fileinfo.FileInfoStd;
import io.vertigo.datastore.filestore.data.domain.fileinfo.FileInfoTemp;
import io.vertigo.datastore.filestore.model.FileInfo;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public abstract class AbstractFileStoreManagerTest {
	@Inject
	protected FileStoreManager fileStoreManager;
	@Inject
	protected VTransactionManager transactionManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() throws Exception {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		doSetUp();
	}

	protected abstract void doSetUp() throws Exception;

	@AfterEach
	public final void tearDown() throws Exception {
		if (node != null) {
			try {
				doTearDown();
			} finally {
				node.close();
			}
		}

	}

	protected abstract void doTearDown() throws Exception;

	protected abstract NodeConfig buildNodeConfig();

	@Test
	public void testCreateFile() throws Exception {
		doCreateFile(this::createFileInfo);
	}

	@Test
	public void testUpdateFile() throws Exception {
		doUpdateFile(this::createFileInfo);
	}

	@Test
	public void testDeleteFile() throws Exception {
		doDeleteFile(this::createFileInfo);
	}

	protected void doCreateFile(final Function<VFile, FileInfo> createFileInfoFct) throws Exception {
		//1.Création du fichier depuis un fichier texte du FS
		final VFile vFile = TestUtil.createVFile("./data/lautreamont.txt", AbstractFileStoreManagerTest.class);
		//2. Sauvegarde en BDD
		final FileInfo fileInfo = createFileInfoFct.apply(vFile);
		final FileInfo createdFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//2. Sauvegarde en Temp
			createdFileInfo = fileStoreManager.create(fileInfo);
			transaction.commit(); //can't read file if not commited (TODO ?)

			System.out.println("doCreateFile " + createdFileInfo.getURI().toURN());
		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//3.relecture du fichier
			final FileInfo readFileInfo = fileStoreManager.read(createdFileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.

			final String source;
			try (final OutputStream sourceOS = new java.io.ByteArrayOutputStream()) {
				try (final InputStream in = vFile.createInputStream()) {
					FileUtil.copy(in, sourceOS);
				}
				source = sourceOS.toString();
			}

			final String read;
			try (final OutputStream readOS = new java.io.ByteArrayOutputStream()) {
				try (final InputStream in = readFileInfo.getVFile().createInputStream()) {
					FileUtil.copy(in, readOS);
				}
				read = readOS.toString();
			}
			//on vérifie que le contenu des fichiers est identique.
			//assertEquals("toto", "toto");
			//assertEquals("toto", "ti");
			Assertions.assertEquals(source, read);
			Assertions.assertTrue(read.startsWith("Chant I"), "Test contenu du fichier");
			Assertions.assertTrue(read.indexOf("ses notes langoureuses,") > 0, "Test contenu du fichier : " + secureSubString(read, 16711, "ses notes langoureuses,"));
			Assertions.assertTrue(read.indexOf("mal : \"Adolescent,") > 0, "Test contenu du fichier : " + secureSubString(read, 11004, "mal : \"Adolescent,"));

			//On désactive pour l'instant
			//Ne marche pas sur la PIC pour cause de charset sur le àé			//Assertions.assertTrue("Test contenu du fichier : " + secureSubString(read, 15579, "adieu !à ;"), read.indexOf("adieu !à ;") > 0);
		}
	}

	protected void doDeleteFile(final Function<VFile, FileInfo> createFileInfoFct) throws Exception {
		//1.Création du fichier depuis un fichier texte du FS
		final VFile vFile = TestUtil.createVFile("./data/lautreamont.txt", AbstractFileStoreManagerTest.class);
		//2. Sauvegarde en BDD
		final FileInfo fileInfo = createFileInfoFct.apply(vFile);
		final FileInfo createdFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//2. Sauvegarde en Temp
			createdFileInfo = fileStoreManager.create(fileInfo);
			transaction.commit(); //can't read file if not commited (TODO ?)

			System.out.println("doDeleteFile " + createdFileInfo.getURI().toURN());
		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//3.relecture du fichier
			fileStoreManager.read(createdFileInfo.getURI());
		}
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//4. suppression.
			fileStoreManager.delete(createdFileInfo.getURI());
			transaction.commit();
		}
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//3.relecture du fichier
			Assertions.assertThrows(NullPointerException.class, () -> {
				fileStoreManager.read(createdFileInfo.getURI());
			});
		}
	}

	protected void doUpdateFile(final Function<VFile, FileInfo> createFileInfoFct) throws Exception {
		//1.Création du fichier depuis un fichier texte du FS

		final VFile vFile = TestUtil.createVFile("./data/testFile.txt", AbstractFileStoreManagerTest.class);
		final VFile vFile2 = TestUtil.createVFile("./data/lautreamont.txt", AbstractFileStoreManagerTest.class);
		//2. Sauvegarde en BDD
		final FileInfo createdFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final FileInfo fileInfo = createFileInfoFct.apply(vFile);
			createdFileInfo = fileStoreManager.create(fileInfo);
			transaction.commit();
			System.out.println("doUpdateFile " + createdFileInfo.getURI().toURN());
		}
		final FileInfo readFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//3. relecture du fichier
			readFileInfo = fileStoreManager.read(createdFileInfo.getURI());
		}

		//4. comparaison du fichier créé et du fichier lu.
		final String source = readFileContent(vFile2);
		final String read = readFileContent(readFileInfo.getVFile());

		//on vérifie que le contenu des fichiers est différent.
		Assertions.assertNotEquals(source, read);

		//2. Mise à jour en BDD
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final FileInfo fileInfo2 = createFileInfoFct.apply(vFile2);
			fileInfo2.setURIStored(createdFileInfo.getURI());
			fileStoreManager.update(fileInfo2);
			transaction.commit();
			System.out.println("doUpdateFile2 " + createdFileInfo.getURI().toURN());
		}
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {

			//3. relecture du fichier
			final FileInfo readFileInfo2 = fileStoreManager.read(createdFileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.
			final String read2 = readFileContent(readFileInfo2.getVFile());

			//on vérifie que le contenu des fichiers est identique.
			//assertEquals("toto", "toto");
			//assertEquals("toto", "ti");
			Assertions.assertEquals(source, read2);
			Assertions.assertTrue(read2.startsWith("Chant I"), "Test contenu du fichier");
			Assertions.assertTrue(read2.indexOf("ses notes langoureuses,") > 0, "Test contenu du fichier : " + secureSubString(read2, 16711, "ses notes langoureuses,"));
			Assertions.assertTrue(read2.indexOf("mal : \"Adolescent,") > 0, "Test contenu du fichier : " + secureSubString(read2, 11004, "mal : \"Adolescent,"));
		}

	}

	@Test
	public void testOtherStoreFile() throws Exception {
		final VFile vFile = TestUtil.createVFile("./data/lautreamont.txt", AbstractFileStoreManagerTest.class);
		//1.Création du fichier depuis un fichier texte du FS
		final FileInfo fileInfo = new FileInfoTemp(vFile);
		final FileInfo createdFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//2. Sauvegarde en Temp
			createdFileInfo = fileStoreManager.create(fileInfo);
			transaction.commit(); //can't read file if not commited (TODO ?)
			System.out.println("testOtherStoreFile " + createdFileInfo.getURI().toURN());

		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {

			//3.relecture du fichier
			final FileInfo readFileInfo = fileStoreManager.read(createdFileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.

			final String source;
			try (final OutputStream sourceOS = new ByteArrayOutputStream()) {
				try (final InputStream sourceIS = vFile.createInputStream()) {
					FileUtil.copy(sourceIS, sourceOS);
				}
				source = sourceOS.toString();
			}

			final String read;
			try (final OutputStream readOS = new ByteArrayOutputStream()) {
				try (final InputStream in = readFileInfo.getVFile().createInputStream()) {
					FileUtil.copy(in, readOS);
				}
				read = readOS.toString();
			}
			//on vérifie que le contenu des fichiers est identique.
			//assertEquals("toto", "toto");
			//assertEquals("toto", "ti");
			Assertions.assertEquals(source, read);
			Assertions.assertTrue(read.startsWith("Chant I"), "Test contenu du fichier");
			Assertions.assertTrue(read.indexOf("ses notes langoureuses,") > 0, "Test contenu du fichier : " + secureSubString(read, 16711, "ses notes langoureuses,"));
			Assertions.assertTrue(read.indexOf("mal : \"Adolescent,") > 0, "Test contenu du fichier : " + secureSubString(read, 11004, "mal : \"Adolescent,"));

			//On désactive pour l'instant
			//Ne marche pas sur la PIC pour cause de charset sur le àé
			//Assertions.assertTrue("Test contenu du fichier : " + secureSubString(read, 15579, "adieu !à ;"), read.indexOf("adieu !à ;") > 0);
		}
	}

	private String readFileContent(final VFile vFile) throws IOException {
		try (final OutputStream sourceOS = new ByteArrayOutputStream()) {
			try (final InputStream fileIS = vFile.createInputStream()) {
				FileUtil.copy(fileIS, sourceOS);
			}
			return sourceOS.toString();
		}
	}

	protected FileInfo createFileInfo(final VFile vFile) {
		return new FileInfoStd(vFile);
	}

	protected static String secureSubString(final String read, final int index, final String searchString) {
		if (read != null && read.length() > index) {
			return read.substring(index, Math.min(read.length() - 1, index + searchString.length()));
		}
		return "N/A";
	}

}
