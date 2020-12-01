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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.TestUtil;
import io.vertigo.datastore.filestore.data.TestSmartTypes;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.filestore.util.VFileUtil;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.datastore.impl.filestore.model.StreamFile;

/**
 * Test de l'implémentation standard.
 *
 * @author dchallas
 */
public final class VFileTest {

	private AutoCloseableNode node;

	@BeforeEach
	public void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DataStoreFeatures()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
								.build())
						.build())
				.build();
	}

	@Test
	public void testCreateTempFile() {
		final Path file = TestUtil.getFile("data/testFile.txt", getClass());
		final VFile vFile = FSFile.of(file);
		checkVFile(vFile, "testFile.txt", null, "text/plain", 71092L);
	}

	@Test
	public void testObtainReadOnlyFile() {
		final Path file = TestUtil.getFile("data/testFile.txt", getClass());
		final VFile vFile = FSFile.of(file);
		checVFile(VFileUtil.obtainReadOnlyPath(vFile), file);
	}

	@Test
	public void testCreateTempFileWithFixedNameAndMime() {
		final String fileName = "monTestFile.txt";
		final String typeMime = "monTypeMime";
		final Path file = TestUtil.getFile("data/testFile.txt", getClass());
		final VFile vFile = FSFile.of(fileName, typeMime, file);
		checkVFile(vFile, fileName, null, typeMime, 71092L);
	}

	@Test
	public void testCreateTempFileWithNoFileNoMime() {
		final String fileName = "monTestFile.txt";
		final Instant lastModified = Instant.now();
		final long length = 123;
		final InputStreamBuilder inputStreamBuilder = new InputStreamBuilder() {
			@Override
			public InputStream createInputStream() {
				return new StringBufferInputStream("Contenu test");
			}
		};
		final VFile vFile = StreamFile.of(fileName, lastModified, length, inputStreamBuilder);
		checkVFile(vFile, fileName, lastModified, "text/plain", length);
	}

	@Test
	public void testCreateTempFileWithNoFile() {
		final String fileName = "monTestFile.txt";
		final String typeMime = "monTypeMime";
		final Instant lastModified = Instant.now();
		final long length = 123;
		final InputStreamBuilder inputStreamBuilder = new InputStreamBuilder() {
			@Override
			public InputStream createInputStream() {
				return new StringBufferInputStream("Contenu test");
			}
		};
		final VFile vFile = StreamFile.of(fileName, typeMime, lastModified, length, inputStreamBuilder);
		checkVFile(vFile, fileName, lastModified, typeMime, length);
	}

	private static void checkVFile(final VFile vFile, final String fileName, final Instant lastModified, final String mimeType, final Long length) {
		Assertions.assertEquals(fileName, vFile.getFileName());
		if (lastModified != null) { //le lastModified peut être inconnu du test
			Assertions.assertEquals(lastModified, vFile.getLastModified());
		}
		Assertions.assertEquals(mimeType, vFile.getMimeType());
		Assertions.assertEquals(length, vFile.getLength(), length * 0.1); //+ or - 10%

		try (InputStream in = vFile.createInputStream()) {
			nop(in);
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	private static void nop(final InputStream in) {
		//nada
	}

	private static void checVFile(final Path outFile, final Path inFile) {
		Assertions.assertEquals(inFile.toFile().getAbsolutePath(), outFile.toFile().getAbsolutePath());
	}
}
