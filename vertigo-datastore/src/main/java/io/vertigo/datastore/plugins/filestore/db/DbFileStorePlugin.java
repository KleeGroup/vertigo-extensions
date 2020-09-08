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
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datastore.filestore.model.FileInfo;
import io.vertigo.datastore.filestore.model.FileInfoURI;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.FileStorePlugin;
import io.vertigo.datastore.impl.filestore.model.StreamFile;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup
 */
public final class DbFileStorePlugin extends AbstractDbFileStorePlugin implements FileStorePlugin, Activeable {

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 * @author npiedeloup
	 */
	private enum DtoFields implements DtFieldName {
		fileName, mimeType, lastModified, length, fileData
	}

	private final String storeDtDefinitionName;
	private DtField storeIdField;
	private DtDefinition storeDtDefinition;

	/**
	 * Constructor.
	 * @param name Store name
	 * @param storeDtDefinitionName Nom du dt de stockage
	 */
	@Inject
	public DbFileStorePlugin(
			@ParamValue("name") final Optional<String> name,
			@ParamValue("storeDtName") final String storeDtDefinitionName,
			@ParamValue("fileInfoClass") final String fileInfoClassName) {
		super(name, fileInfoClassName);
		Assertion.check()
				.isNotBlank(storeDtDefinitionName);
		//-----
		this.storeDtDefinitionName = storeDtDefinitionName;
	}

	@Override
	public void start() {
		storeDtDefinition = Node.getNode().getDefinitionSpace().resolve(storeDtDefinitionName, DtDefinition.class);
		storeIdField = storeDtDefinition.getIdField().get();
	}

	@Override
	public void stop() {
		// nothing

	}

	/** {@inheritDoc} */
	@Override
	public FileInfo read(final FileInfoURI uri) {
		Assertion.check().isNotNull(uri);
		checkDefinitionStoreBinding(uri.getDefinition());
		//-----
		final UID<Entity> dtoUri = UID.of(storeDtDefinition, uri.getKeyAs(storeIdField.getSmartTypeDefinition().getJavaClass()));
		final Entity fileInfoDto = getEntityStoreManager().readOne(dtoUri);
		final InputStreamBuilder inputStreamBuilder = new DataStreamInputStreamBuilder(getValue(fileInfoDto, DtoFields.fileData, DataStream.class));
		final String fileName = getValue(fileInfoDto, DtoFields.fileName, String.class);
		final String mimeType = getValue(fileInfoDto, DtoFields.mimeType, String.class);
		final Instant lastModified = getValue(fileInfoDto, DtoFields.lastModified, Instant.class);
		final Long length = getValue(fileInfoDto, DtoFields.length, Long.class);
		final VFile vFile = StreamFile.of(fileName, mimeType, lastModified, length, inputStreamBuilder);
		return new DatabaseFileInfo(uri.getDefinition(), vFile);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo create(final FileInfo fileInfo) {
		checkReadonly();
		Assertion.check().isNotNull(fileInfo.getURI() == null, "Only file without any id can be created.");
		checkDefinitionStoreBinding(fileInfo.getDefinition());
		//-----
		final Entity fileInfoDto = createFileInfoDto(fileInfo);
		//-----
		getEntityStoreManager().create(fileInfoDto);
		//-----
		final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
		Assertion.check().isNotNull(fileInfoDtoId, "File's id must be set");
		final FileInfoURI uri = new FileInfoURI(fileInfo.getDefinition(), fileInfoDtoId);
		fileInfo.setURIStored(uri);
		return fileInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		checkReadonly();
		Assertion.check().isNotNull(fileInfo.getURI() != null, "Only file with an id can be updated.");
		checkDefinitionStoreBinding(fileInfo.getDefinition());
		//-----
		final Entity fileInfoDto = createFileInfoDto(fileInfo);
		//-----
		getEntityStoreManager().update(fileInfoDto);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final FileInfoURI uri) {
		checkReadonly();
		Assertion.check().isNotNull(uri, "uri du fichier doit être renseignée.");
		checkDefinitionStoreBinding(uri.getDefinition());
		//-----
		final UID<Entity> dtoUri = UID.of(storeDtDefinition, uri.getKeyAs(storeIdField.getSmartTypeDefinition().getJavaClass()));
		getEntityStoreManager().delete(dtoUri);
	}

	@Override
	public Class<? extends FileInfo> getFileInfoClass() {
		return ClassUtil.classForName(getFileInfoClassName(), FileInfo.class);
	}

	private Entity createFileInfoDto(final FileInfo fileInfo) {
		//Il doit exister un DtObjet associé à storeDtDefinition avec la structure attendue.
		final Entity fileInfoDto = DtObjectUtil.createEntity(storeDtDefinition);
		//-----

		final VFile vFile = fileInfo.getVFile();
		setValue(fileInfoDto, DtoFields.fileName, vFile.getFileName());
		setValue(fileInfoDto, DtoFields.mimeType, vFile.getMimeType());
		setValue(fileInfoDto, DtoFields.lastModified, vFile.getLastModified());
		setValue(fileInfoDto, DtoFields.length, vFile.getLength());
		setValue(fileInfoDto, DtoFields.fileData, new VFileDataStream(vFile));

		if (fileInfo.getURI() != null) {
			setIdValue(fileInfoDto, fileInfo.getURI());
		}
		return fileInfoDto;
	}

}
