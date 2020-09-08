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
package io.vertigo.account.plugins.account.cache.redis;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Objet redis correspondant à un VFile. Le contenu du VFile est stocké sous forme base64.
 *
 * @author jmforhan
 */
final class Base64File implements VFile {

	private static final long serialVersionUID = -6930931781248326088L;

	private final String fileName;
	private final String mimeType;
	private final Long length;
	private final Instant lastModified;
	private final String base64Content;
	private final CodecManager codecManager;

	/**
	 * File read from a base64 content.
	 * @param fileName file name
	 * @param mimeType type mime
	 * @param length File length
	 * @param lastModified file lastModified date
	 * @param base64Content encodage en base64.
	 */
	public Base64File(
			final CodecManager codecManager,
			final String fileName,
			final String mimeType,
			final Long length,
			final Instant lastModified,
			final String base64Content) {
		Assertion.check().isNotNull(codecManager);
		//---
		this.codecManager = codecManager;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.length = length;
		this.lastModified = lastModified;
		this.base64Content = base64Content;
	}

	/**
	 * @return fileName.
	 */
	@Override
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return mimeType.
	 */
	@Override
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @return length.
	 */
	@Override
	public Long getLength() {
		return length;
	}

	/**
	 *@return lastModified.
	 */
	@Override
	public Instant getLastModified() {
		return lastModified;
	}

	/**
	 * @return base64Content.
	 */
	public String getBase64Content() {
		return base64Content.replace('_', '+')
				.replace('-', '/');
	}

	/** {@inheritDoc} */
	@Override
	public InputStream createInputStream() {
		return new ByteArrayInputStream(codecManager.getBase64Codec().decode(base64Content));
	}
}
