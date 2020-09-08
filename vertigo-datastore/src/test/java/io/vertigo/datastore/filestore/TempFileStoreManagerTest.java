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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.h2.Driver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.TestUtil;
import io.vertigo.datastore.entitystore.sql.SqlUtil;
import io.vertigo.datastore.filestore.data.DtDefinitions;
import io.vertigo.datastore.filestore.data.TestSmartTypes;
import io.vertigo.datastore.filestore.data.domain.fileinfo.FileInfoFs;
import io.vertigo.datastore.filestore.data.domain.fileinfo.FileInfoTemp;
import io.vertigo.datastore.filestore.model.FileInfo;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.filestore.util.FileUtil;
import io.vertigo.datastore.impl.filestore.FileStorePlugin;
import io.vertigo.datastore.plugins.filestore.fs.FsFullFileStorePlugin;

public class TempFileStoreManagerTest {

	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private TaskManager taskManager;
	@Inject
	private FileStoreManager fileStoreManager;
	@Inject
	private List<FileStorePlugin> fileStorePlugins;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		//A chaque test on recrée la table famille
		final List<String> requests = List.of(
				" create table VX_FILE_INFO(FIL_ID BIGINT , FILE_NAME varchar(255), MIME_TYPE varchar(255), LENGTH BIGINT, LAST_MODIFIED date, FILE_PATH varchar(255), FILE_DATA BLOB)",
				" create sequence SEQ_VX_FILE_INFO start with 10001 increment by 1");
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				requests,
				"TkInitMain",
				Optional.empty());
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			try {
				SqlUtil.execRequests(
						transactionManager,
						taskManager,
						Arrays.asList(
								" drop table if exists VX_FILE_INFO ",
								" drop sequence if exists SEQ_VX_FILE_INFO"),
						"TkShutDown",
						Optional.empty());
			} finally {
				node.close();
			}
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures()
						.withScript()
						.withJaninoScript()
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.withC3p0(
								Param.of("dataBaseClass", H2DataBase.class.getCanonicalName()),
								Param.of("jdbcDriver", Driver.class.getCanonicalName()),
								Param.of("jdbcUrl", "jdbc:h2:mem:database"))
						.build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withMemoryCache()
						.withEntityStore()
						.withFileStore()
						.withSqlEntityStore()
						.withFsFileStore(Param.of("name", "fsStore"),
								Param.of("path", "${java.io.tmpdir}/testFsVertigo/"),
								Param.of("storeDtName", "DtVxFileInfo"),
								Param.of("fileInfoClass", FileInfoFs.class.getName()))
						.withFsFullFileStore(
								Param.of("name", "temp"),
								Param.of("path", "${java.io.tmpdir}/testVertigo/"),
								Param.of("purgeDelayMinutes", "0"),
								Param.of("fileInfoClass", FileInfoTemp.class.getName()))
						.build())
				.addModule(ModuleConfig.builder("definition")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
								.build())
						.build())
				.build();
	}

	@Test
	public void testRemovedOldFileDaemon() throws Exception {
		final Path tempDir = Paths.get(FileUtil.translatePath("${java.io.tmpdir}/testVertigo/"));
		final long firstCount = fileCount(tempDir);

		Thread.sleep(1000); //wait remove dir

		final VFile vFile = TestUtil.createVFile("./data/lautreamont.txt", this.getClass());
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//1.Création du fichier depuis un fichier texte du FS
			final FileInfo fileInfo = new FileInfoTemp(vFile);
			//2. Sauvegarde en Temp
			final FileInfo createdFileInfo = fileStoreManager.create(fileInfo);
			System.out.println(createdFileInfo.getURI().toURN());
			transaction.commit(); //can't read file if not commited (TODO ?)
		}

		final long beforeCount = fileCount(tempDir);

		Assertions.assertEquals(firstCount + 2, beforeCount, "Created file wasn't store in temp dir");

		Thread.sleep(1000); //wait remove dir

		fileStorePlugins.stream()
				.filter((plugin) -> plugin instanceof FsFullFileStorePlugin)
				.findFirst().ifPresent(
						(plugin) -> ((FsFullFileStorePlugin) plugin).deleteOldFiles());

		Thread.sleep(1000); //wait remove dir

		final long afterCount = fileCount(tempDir);

		Assertions.assertEquals(0, afterCount, "Clean of temp dir wasn't complete");

	}

	public long fileCount(final Path dir) throws IOException {
		try (Stream<Path> subFiles = Files.walk(dir)) {
			return subFiles.filter(p -> !Files.isDirectory(p))
					.count();
		}
	}

}
