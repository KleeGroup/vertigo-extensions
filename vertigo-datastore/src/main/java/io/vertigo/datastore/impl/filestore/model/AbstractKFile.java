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
package io.vertigo.datastore.impl.filestore.model;

import java.time.Instant;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Class générique de définition d'un fichier.
 * @author npiedeloup
 */
abstract class AbstractVFile implements VFile {
	private static final long serialVersionUID = 1L;
	private final String fileName;
	private final String mimeType;
	private final Instant lastModified;
	private final long length;

	/**
	 * Constructor.
	 * Associe un fichier à des méta-données
	 * @param fileName Nom d'origine du fichier
	 * @param mimeType Type mime du fichier
	 * @param lastModified Date de derniére modification du fichier
	 * @param length Longueur du fichier (en octet)
	 */
	protected AbstractVFile(final String fileName, final String mimeType, final Instant lastModified, final long length) {
		Assertion.check()
				.isNotNull(fileName)
				.isNotNull(mimeType)
				.isNotNull(lastModified)
				.isNotNull(length);
		//-----
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.lastModified = lastModified;
		this.length = length;
	}

	/** {@inheritDoc} */
	@Override
	public final String getFileName() {
		return fileName;
	}

	/** {@inheritDoc} */
	@Override
	public final String getMimeType() {
		return mimeType;
	}

	/** {@inheritDoc} */
	@Override
	public final Long getLength() {
		return length;
	}

	/** {@inheritDoc} */
	@Override
	public final Instant getLastModified() {
		return lastModified;
	}
}
