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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.DataStream;
import io.vertigo.core.node.Node;
import io.vertigo.core.param.ParamValue;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.filestore.FileStoreManager;
import io.vertigo.datastore.filestore.definitions.FileInfoDefinition;
import io.vertigo.datastore.filestore.model.FileInfoURI;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.AbstractFileInfo;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup
 */
abstract class AbstractDbFileStorePlugin {
	private static final String STORE_READ_ONLY = "Le store est en readOnly";

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final String name;
	private final String fileInfoClassName;

	/**
	 * Constructor.
	 * @param name Store name
	 */
	AbstractDbFileStorePlugin(
			@ParamValue("name") final Optional<String> name,
			final String fileInfoClassName) {
		Assertion.check()
				.isNotNull(name)
				.isNotBlank(fileInfoClassName);
		//-----
		readOnly = false;
		this.name = name.orElse(FileStoreManager.MAIN_DATA_SPACE_NAME);
		this.fileInfoClassName = fileInfoClassName;
	}

	/**
	 * @return This store name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the fileInfo className
	 */
	public String getFileInfoClassName() {
		return fileInfoClassName;
	}

	/**
	 * Check readonly.
	 */
	protected void checkReadonly() {
		Assertion.check().isFalse(readOnly, STORE_READ_ONLY);
	}

	/**
	 * Check this store is the good one for this definition.
	 * @param fileInfoDefinition Definition du FileInfo
	 */
	protected void checkDefinitionStoreBinding(final FileInfoDefinition fileInfoDefinition) {
		Assertion.check().isTrue(
				name.equals(fileInfoDefinition.getStoreName()),
				"This store {0} isn't the good one for this definition {1}, expect {2}.", name, fileInfoDefinition.getName(), fileInfoDefinition.getStoreName());
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 * @param <V> Type de la valeur	 *
	 * @param dto DtObject
	 * @param fieldName Nom du champ
	 * @param valueClass Type du champ
	 * @return Valeur typé du champ
	 */
	protected static <V> V getValue(final DtObject dto, final DtFieldName fieldName, final Class<V> valueClass) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(fieldName.name());
		return valueClass.cast(dtField.getDataAccessor().getValue(dto));
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 *
	 * @param dto DtObject
	 * @param fieldName Nom du champs
	 * @param value Valeur
	 */
	protected static void setValue(final DtObject dto, final DtFieldName fieldName, final Object value) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(fieldName.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	/**
	 * @param dto DtObject
	 * @param value Pk value
	 */
	protected static void setIdValue(final DtObject dto, final FileInfoURI uri) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField idField = dtDefinition.getIdField().get();
		idField.getDataAccessor().setValue(dto, uri.getKeyAs(idField.getSmartTypeDefinition().getJavaClass()));
	}

	/**
	 * Database fileinfo.
	 * @author npiedeloup
	 */
	protected static class DatabaseFileInfo extends AbstractFileInfo {

		private static final long serialVersionUID = 1629356922141393996L;

		/**
		 * Constructor.
		 * @param fileInfoDefinition FileInfo definition
		 * @param vFile File data
		 */
		protected DatabaseFileInfo(final FileInfoDefinition fileInfoDefinition, final VFile vFile) {
			super(fileInfoDefinition, vFile);
		}
	}

	/**
	 * DataStream from VFile.
	 */
	protected static final class VFileDataStream implements DataStream {
		private final VFile vFile;

		/**
		 * Constructor.
		 * @param vFile File data
		 */
		VFileDataStream(final VFile vFile) {
			this.vFile = vFile;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return vFile.createInputStream();
		}

		/** {@inheritDoc} */
		@Override
		public long getLength() {
			return vFile.getLength();
		}
	}

	/**
	 * InputStreamBuilder from DataStream.
	 */
	protected static final class DataStreamInputStreamBuilder implements InputStreamBuilder {
		private final DataStream dataStream;

		/**
		 * Constructor.
		 * @param dataStream Data Stream
		 */
		DataStreamInputStreamBuilder(final DataStream dataStream) {
			this.dataStream = dataStream;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return dataStream.createInputStream();
		}
	}

	/**
	 * @return StoreManager
	 */
	protected static EntityStoreManager getEntityStoreManager() {
		return Node.getNode().getComponentSpace().resolve(EntityStoreManager.class);
	}
}
