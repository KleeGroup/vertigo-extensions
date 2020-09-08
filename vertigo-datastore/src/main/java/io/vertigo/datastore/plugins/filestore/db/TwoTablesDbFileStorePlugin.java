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
package io.vertigo.datastore.plugins.filestore.db;

import java.time.Instant;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.DataStream;
import io.vertigo.core.node.Node;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datastore.filestore.definitions.FileInfoDefinition;
import io.vertigo.datastore.filestore.model.FileInfo;
import io.vertigo.datastore.filestore.model.FileInfoURI;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.FileStorePlugin;
import io.vertigo.datastore.impl.filestore.model.StreamFile;

/**
 * Permet de gérer le CRUD sur un fichier stocké sur deux tables (Méta données / Données).
 *
 * @author sezratty
 */
public final class TwoTablesDbFileStorePlugin extends AbstractDbFileStorePlugin implements FileStorePlugin {

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 */
	private enum DtoFields implements DtFieldName {
		fileName, mimeType, lastModified, length, fileData, fmdId, fdtId
	}

	private final DtDefinition storeMetaDataDtDefinition;
	private final DtField storeMetaDataIdField;
	private final DtDefinition storeFileDtDefinition;

	/**
	 * Constructor.
	 * @param name This store name
	 * @param storeMetaDataDtDefinitionName MetaData storing dtDefinition
	 * @param storeFileDtDefinitionName File storing dtDefinition
	 */
	@Inject
	public TwoTablesDbFileStorePlugin(
			@ParamValue("name") final Optional<String> name,
			@ParamValue("storeMetaDataDtName") final String storeMetaDataDtDefinitionName,
			@ParamValue("storeFileDtName") final String storeFileDtDefinitionName,
			@ParamValue("fileInfoClass") final String fileInfoClassName) {
		super(name, fileInfoClassName);
		//-----
		storeMetaDataDtDefinition = Node.getNode().getDefinitionSpace().resolve(storeMetaDataDtDefinitionName, DtDefinition.class);
		storeFileDtDefinition = Node.getNode().getDefinitionSpace().resolve(storeFileDtDefinitionName, DtDefinition.class);
		storeMetaDataIdField = storeMetaDataDtDefinition.getIdField().get();
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo read(final FileInfoURI fileInfoUri) {
		checkDefinitionStoreBinding(fileInfoUri.getDefinition());
		// Ramène FileMetada
		final UID<Entity> dtoMetaDataUri = UID.of(storeMetaDataDtDefinition, fileInfoUri.getKeyAs(storeMetaDataIdField.getSmartTypeDefinition().getJavaClass()));
		final DtObject fileMetadataDto = getEntityStoreManager().readOne(dtoMetaDataUri);
		final Object fdtId = getValue(fileMetadataDto, DtoFields.fdtId, Object.class);

		// Ramène FileData
		final UID<Entity> dtoDataUri = UID.of(storeFileDtDefinition, fdtId);

		final DtObject fileDataDto = getEntityStoreManager().readOne(dtoDataUri);
		// Construction du vFile.
		final InputStreamBuilder inputStreamBuilder = new DataStreamInputStreamBuilder(getValue(fileDataDto, DtoFields.fileData, DataStream.class));
		final String fileName = getValue(fileMetadataDto, DtoFields.fileName, String.class);
		final String mimeType = getValue(fileMetadataDto, DtoFields.mimeType, String.class);
		final Instant lastModified = getValue(fileMetadataDto, DtoFields.lastModified, Instant.class);
		final Long length = getValue(fileMetadataDto, DtoFields.length, Long.class);
		final VFile vFile = StreamFile.of(fileName, mimeType, lastModified, length, inputStreamBuilder);

		//TODO passer par une factory de FileInfo à partir de la FileInfoDefinition (comme DomainFactory)
		final FileInfo fileInfo = new DatabaseFileInfo(fileInfoUri.getDefinition(), vFile);
		fileInfo.setURIStored(fileInfoUri);
		return fileInfo;
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo create(final FileInfo fileInfo) {
		checkReadonly();
		checkDefinitionStoreBinding(fileInfo.getDefinition());
		Assertion.check().isNull(fileInfo.getURI(), "Only file without any id can be created");
		//-----
		final Entity fileMetadataDto = createMetaDataEntity(fileInfo);
		final Entity fileEntity = createFileEntity(fileInfo);
		//-----
		getEntityStoreManager().create(fileEntity);
		setValue(fileMetadataDto, DtoFields.fdtId, DtObjectUtil.getId(fileEntity));
		getEntityStoreManager().create(fileMetadataDto);
		final FileInfoURI fileInfoUri = createURI(fileInfo.getDefinition(), DtObjectUtil.getId(fileMetadataDto));
		fileInfo.setURIStored(fileInfoUri);
		return fileInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		checkReadonly();
		checkDefinitionStoreBinding(fileInfo.getDefinition());
		Assertion.check().isTrue(fileInfo.getURI() != null, "Only file with id can be updated");
		//-----
		final Entity fileMetadataDto = createMetaDataEntity(fileInfo);
		final Entity fileDataDto = createFileEntity(fileInfo);
		//-----
		setIdValue(fileMetadataDto, fileInfo.getURI());
		// Chargement du FDT_ID
		final UID<Entity> dtoMetaDataUri = UID.of(storeMetaDataDtDefinition, fileInfo.getURI().getKeyAs(storeMetaDataIdField.getSmartTypeDefinition().getJavaClass()));
		final DtObject fileMetadataDtoOld = getEntityStoreManager().readOne(dtoMetaDataUri);
		final Object fdtId = getValue(fileMetadataDtoOld, DtoFields.fdtId, Object.class);
		setValue(fileMetadataDto, DtoFields.fdtId, fdtId);
		setValue(fileDataDto, DtoFields.fdtId, fdtId);
		getEntityStoreManager().update(fileDataDto);
		getEntityStoreManager().update(fileMetadataDto);
	}

	private static FileInfoURI createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new FileInfoURI(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final FileInfoURI fileInfoUri) {
		checkReadonly();
		checkDefinitionStoreBinding(fileInfoUri.getDefinition());
		//-----
		final UID<Entity> dtoMetaDataUri = UID.of(storeMetaDataDtDefinition, fileInfoUri.getKeyAs(storeMetaDataIdField.getSmartTypeDefinition().getJavaClass()));
		final DtObject fileMetadataDtoOld = getEntityStoreManager().readOne(dtoMetaDataUri);
		final Object fdtId = getValue(fileMetadataDtoOld, DtoFields.fdtId, Object.class);
		final UID<Entity> dtoDataUri = UID.of(storeFileDtDefinition, fdtId);

		getEntityStoreManager().delete(dtoMetaDataUri);
		getEntityStoreManager().delete(dtoDataUri);
	}

	@Override
	public Class<? extends FileInfo> getFileInfoClass() {
		return ClassUtil.classForName(getFileInfoClassName(), FileInfo.class);
	}

	private Entity createMetaDataEntity(final FileInfo fileInfo) {
		final Entity fileMetadataDto = DtObjectUtil.createEntity(storeMetaDataDtDefinition);
		final VFile vFile = fileInfo.getVFile();
		setValue(fileMetadataDto, DtoFields.fileName, vFile.getFileName());
		setValue(fileMetadataDto, DtoFields.mimeType, vFile.getMimeType());
		setValue(fileMetadataDto, DtoFields.lastModified, vFile.getLastModified());
		setValue(fileMetadataDto, DtoFields.length, vFile.getLength());
		return fileMetadataDto;
	}

	private Entity createFileEntity(final FileInfo fileInfo) {
		final Entity fileDataDto = DtObjectUtil.createEntity(storeFileDtDefinition);
		final VFile vFile = fileInfo.getVFile();
		setValue(fileDataDto, DtoFields.fileName, vFile.getFileName());
		setValue(fileDataDto, DtoFields.fileData, new VFileDataStream(vFile));
		return fileDataDto;
	}
}
