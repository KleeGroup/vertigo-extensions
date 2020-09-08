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
package io.vertigo.datastore.impl.filestore;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.datastore.filestore.FileStoreManager;
import io.vertigo.datastore.filestore.definitions.FileInfoDefinition;
import io.vertigo.datastore.filestore.model.FileInfo;
import io.vertigo.datastore.filestore.model.FileInfoURI;

/**
 * Implementation of FileStore.
 * @author pchretien
 */
public final class FileStoreManagerImpl implements FileStoreManager, SimpleDefinitionProvider {

	private final FileStoreConfig fileStoreConfig;
	private final List<FileStorePlugin> fileStorePlugins;

	/**
	 * Constructor.
	 * @param fileStoreConfig Config of the fileStore
	 */
	@Inject
	public FileStoreManagerImpl(
			final List<FileStorePlugin> fileStorePlugins) {
		Assertion.check().isNotNull(fileStorePlugins);
		//-----
		this.fileStorePlugins = fileStorePlugins;
		fileStoreConfig = new FileStoreConfig(fileStorePlugins);
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return fileStorePlugins.stream()
				.map(fileStorePlugin -> new FileInfoDefinition(
						FileInfoDefinition.PREFIX + fileStorePlugin.getFileInfoClass().getSimpleName(),
						fileStorePlugin.getName()))
				.collect(Collectors.toList());
	}

	private FileStorePlugin getPhysicalStore(final FileInfoDefinition fileInfoDefinition) {
		return fileStoreConfig.getPhysicalFileStore(fileInfoDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo create(final FileInfo fileInfo) {
		Assertion.check().isNotNull(fileInfo);
		//-----
		return getPhysicalStore(fileInfo.getDefinition()).create(fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.check().isNotNull(fileInfo);
		//-----
		getPhysicalStore(fileInfo.getDefinition()).update(fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final FileInfoURI uri) {
		Assertion.check().isNotNull(uri);
		//-----
		getPhysicalStore(uri.getDefinition()).delete(uri);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo read(final FileInfoURI uri) {
		Assertion.check().isNotNull(uri);
		//-----
		final FileInfo fileInfo = getPhysicalStore(uri.getDefinition()).read(uri);
		//-----
		Assertion.check().isNotNull(fileInfo, "Le fichier {0} n''a pas été trouvé", uri);
		return fileInfo;
	}

}
