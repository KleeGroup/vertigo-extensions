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
package io.vertigo.datastore.plugins.filestore.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.transaction.VTransaction;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.filestore.FileStoreManager;
import io.vertigo.datastore.filestore.definitions.FileInfoDefinition;
import io.vertigo.datastore.filestore.model.FileInfo;
import io.vertigo.datastore.filestore.model.FileInfoURI;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.filestore.util.FileUtil;
import io.vertigo.datastore.impl.filestore.FileStorePlugin;
import io.vertigo.datastore.impl.filestore.model.AbstractFileInfo;
import io.vertigo.datastore.impl.filestore.model.StreamFile;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup, skerdudou
 */
public final class FsFileStorePlugin implements FileStorePlugin, Activeable {
	private static final String STORE_READ_ONLY = "Le store est en readOnly";

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 *
	 * @author npiedeloup
	 */
	private enum DtoFields {
		/** Champ FileName */
		fileName,
		/** Champ MimeType */
		mimeType,
		/** Champ LastModified */
		lastModified,
		/** Champ Length */
		length,
		/** Champ FilePath */
		filePath
	}

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final String name;
	private final String documentRoot;
	private DtField storeIdField;
	private DtDefinition storeDtDefinition;
	private final String storeDtDefinitionName;
	private final VTransactionManager transactionManager;
	private final String fileInfoClassName;

	/**
	 * Constructor.
	 * @param name Store name
	 * @param storeDtDefinitionName Nom du dt de stockage
	 * @param path le chemin jndi pour récupérer le paramètre path dans le context
	 * @param transactionManager Manager des transactions
	 */
	@Inject
	public FsFileStorePlugin(
			@ParamValue("name") final Optional<String> name,
			@ParamValue("storeDtName") final String storeDtDefinitionName,
			@ParamValue("path") final String path,
			@ParamValue("fileInfoClass") final String fileInfoClassName,
			final VTransactionManager transactionManager) {
		Assertion.check()
				.isNotNull(name)
				.isNotBlank(storeDtDefinitionName)
				.isNotBlank(path)
				.isNotBlank(fileInfoClassName)
				.isNotNull(transactionManager)
				.isTrue(path.endsWith("/"), "store path must ends with / ({0})", path);
		//-----
		this.name = name.orElse(FileStoreManager.MAIN_DATA_SPACE_NAME);
		readOnly = false;
		this.transactionManager = transactionManager;
		documentRoot = FileUtil.translatePath(path);
		this.storeDtDefinitionName = storeDtDefinitionName;
		this.fileInfoClassName = fileInfoClassName;
	}

	@Override
	public void start() {
		storeDtDefinition = Node.getNode().getDefinitionSpace().resolve(storeDtDefinitionName, DtDefinition.class);
		storeIdField = storeDtDefinition.getIdField().get();
	}

	/* (non-Javadoc)
	 * @see io.vertigo.core.component.Activeable#stop()
	 */
	@Override
	public void stop() {
		//NOP
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo read(final FileInfoURI uri) {
		// récupération de l'objet en base
		final UID<Entity> dtoUri = createDtObjectURI(uri);
		final DtObject fileInfoDto = getEntityStoreManager().readOne(dtoUri);

		// récupération du fichier
		final String fileName = getValue(fileInfoDto, DtoFields.fileName, String.class);
		final String mimeType = getValue(fileInfoDto, DtoFields.mimeType, String.class);
		final Instant lastModified = getValue(fileInfoDto, DtoFields.lastModified, Instant.class);
		final Long length = getValue(fileInfoDto, DtoFields.length, Long.class);
		final String filePath = getValue(fileInfoDto, DtoFields.filePath, String.class);

		final InputStreamBuilder inputStreamBuilder = new FileInputStreamBuilder(new File(documentRoot + filePath));
		final VFile vFile = StreamFile.of(fileName, mimeType, lastModified, length, inputStreamBuilder);

		// retourne le fileinfo avec le fichier et son URI
		final FsFileInfo fsFileInfo = new FsFileInfo(uri.getDefinition(), vFile);
		fsFileInfo.setURIStored(uri);
		return fsFileInfo;
	}

	private static class FsFileInfo extends AbstractFileInfo {
		private static final long serialVersionUID = -1610176974946554828L;

		protected FsFileInfo(final FileInfoDefinition fileInfoDefinition, final VFile vFile) {
			super(fileInfoDefinition, vFile);
		}
	}

	private Entity createFileInfoEntity(final FileInfo fileInfo) {
		final Entity fileInfoDto = createFileInfoEntity(fileInfo.getDefinition());
		//-----
		final VFile vFile = fileInfo.getVFile();
		setValue(fileInfoDto, DtoFields.fileName, vFile.getFileName());
		setValue(fileInfoDto, DtoFields.mimeType, vFile.getMimeType());
		setValue(fileInfoDto, DtoFields.lastModified, vFile.getLastModified());
		setValue(fileInfoDto, DtoFields.length, vFile.getLength());
		if (fileInfo.getURI() == null) {
			// cas de la création, on ajoute en base un chemin fictif (colonne not null)
			setValue(fileInfoDto, DtoFields.filePath, "/dev/null");
		} else {
			// cas de l'update
			setIdValue(fileInfoDto, fileInfo.getURI());

			// récupération de l'objet en base pour récupérer le path du fichier et ne pas modifier la base
			final UID<Entity> dtoUri = createDtObjectURI(fileInfo.getURI());
			final DtObject fileInfoDtoBase = getEntityStoreManager().readOne(dtoUri);
			final String pathToSave = getValue(fileInfoDtoBase, DtoFields.filePath, String.class);
			setValue(fileInfoDto, DtoFields.filePath, pathToSave);
		}
		return fileInfoDto;
	}

	private void saveFile(final FileInfo fileInfo, final String pathToSave) {
		try (InputStream inputStream = fileInfo.getVFile().createInputStream()) {
			getCurrentTransaction().addAfterCompletion(new FileActionSave(inputStream, documentRoot + pathToSave));
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't read uploaded file.");
		}
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo create(final FileInfo fileInfo) {
		Assertion.check()
				.isFalse(readOnly, STORE_READ_ONLY)
				.isNotNull(fileInfo.getURI() == null, "Only file without any id can be created.");
		//-----
		final Entity fileInfoDto = createFileInfoEntity(fileInfo);
		//-----
		getEntityStoreManager().create(fileInfoDto);

		// cas de la création
		final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
		Assertion.check().isNotNull(fileInfoDtoId, "File's id must be set.");
		final FileInfoURI uri = createURI(fileInfo.getDefinition(), fileInfoDtoId);
		fileInfo.setURIStored(uri);

		// on met a jour la base
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd/", Locale.FRANCE);
		final String pathToSave = format.format(LocalDate.now()) + fileInfoDtoId;
		setValue(fileInfoDto, DtoFields.filePath, pathToSave);
		//-----
		getEntityStoreManager().update(fileInfoDto);
		//-----
		saveFile(fileInfo, pathToSave);
		return fileInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.check()
				.isFalse(readOnly, STORE_READ_ONLY)
				.isNotNull(fileInfo.getURI() != null, "Only file with an id can be updated.");
		//-----
		final Entity fileInfoDto = createFileInfoEntity(fileInfo);
		//-----
		getEntityStoreManager().update(fileInfoDto);

		final String pathToSave = getValue(fileInfoDto, DtoFields.filePath, String.class);
		//-----
		saveFile(fileInfo, pathToSave);
	}

	private static FileInfoURI createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new FileInfoURI(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final FileInfoURI uri) {
		Assertion.check().isFalse(readOnly, STORE_READ_ONLY);

		final UID<Entity> dtoUri = createDtObjectURI(uri);
		//-----suppression du fichier
		final DtObject fileInfoDto = getEntityStoreManager().readOne(dtoUri);
		final String path = getValue(fileInfoDto, DtoFields.filePath, String.class);
		getCurrentTransaction().addAfterCompletion(new FileActionDelete(documentRoot + path));
		//-----suppression en base
		getEntityStoreManager().delete(dtoUri);
	}

	@Override
	public Class<? extends FileInfo> getFileInfoClass() {
		return ClassUtil.classForName(fileInfoClassName, FileInfo.class);
	}

	/**
	 * Création d'une URI de DTO à partir de l'URI de FileInfo
	 *
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker.
	 */
	private UID<Entity> createDtObjectURI(final FileInfoURI uri) {
		Assertion.check().isNotNull(uri, "file uri must be provided.");
		//-----
		// Il doit exister un DtObjet associé, avec la structure attendue.
		return UID.of(storeDtDefinition, uri.getKeyAs(storeIdField.getSmartTypeDefinition().getJavaClass()));
	}

	/**
	 * Création d'un DTO à partir d'une definition de FileInfo
	 *
	 * @param fileInfoDefinition Definition de FileInfo
	 * @return DTO utilisé en BDD pour stocker.
	 */
	private Entity createFileInfoEntity(final FileInfoDefinition fileInfoDefinition) {
		Assertion.check().isNotNull(fileInfoDefinition, "fileInfoDefinition must be provided.");
		//-----
		// Il doit exister un DtObjet associé, avec la structure attendue.
		return DtObjectUtil.createEntity(storeDtDefinition);
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @return Valeur typé du champ
	 */
	private static <V> V getValue(final DtObject dto, final DtoFields field, final Class<V> valueClass) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		return valueClass.cast(dtField.getDataAccessor().getValue(dto));
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @param value Valeur
	 */
	private static void setValue(final DtObject dto, final DtoFields field, final Object value) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getField(field.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static void setIdValue(final DtObject dto, final FileInfoURI uri) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getIdField().get();
		dtField.getDataAccessor().setValue(dto, uri.getKeyAs(dtField.getSmartTypeDefinition().getJavaClass()));
	}

	private static final class FileInputStreamBuilder implements InputStreamBuilder {

		private final File file;

		FileInputStreamBuilder(final File file) {
			this.file = file;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return Files.newInputStream(file.toPath());
		}
	}

	private static EntityStoreManager getEntityStoreManager() {
		return Node.getNode().getComponentSpace().resolve(EntityStoreManager.class);
	}

	/** récupère la transaction courante. */
	private VTransaction getCurrentTransaction() {
		return transactionManager.getCurrentTransaction();
	}

	/**
	 * récupère la valeur de documentRoot.
	 *
	 * @return valeur de documentRoot
	 */
	public String getDocumentRoot() {
		return documentRoot;
	}

}
