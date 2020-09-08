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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;

/**
 * VFile implementation from a provided InputStreamBuilder.
 * @author npiedeloup
 */
public final class StreamFile extends AbstractVFile {
	private static final long serialVersionUID = -4565434303879706815L;

	private final InputStreamBuilder inputStreamBuilder;

	/**
	 * Constructor.
	 * @param fileName File name
	 * @param mimeType Mime type
	 * @param lastModified Last modified date
	 * @param length file size
	 * @param inputStreamBuilder Data stream builder
	 */
	public StreamFile(final String fileName, final String mimeType, final Instant lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		super(fileName, mimeType, lastModified, length);
		this.inputStreamBuilder = inputStreamBuilder;
	}

	/** {@inheritDoc} */
	@Override
	public InputStream createInputStream() throws IOException {
		return inputStreamBuilder.createInputStream();
	}

	public static StreamFile of(final String fileName, final Instant lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		return of(fileName, URLConnection.guessContentTypeFromName(fileName), lastModified, length, inputStreamBuilder);
	}

	public static StreamFile of(final String fileName, final String typeMime, final URL resourceUrl) {
		final long length;
		final Instant lastModified;
		try {
			final URLConnection connection = resourceUrl.openConnection();
			try {
				length = connection.getContentLength();
				lastModified = Instant.ofEpochMilli(connection.getLastModified());
			} finally {
				connection.getInputStream().close();
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't get file meta from url");
		}
		Assertion.check().isTrue(length >= 0, "Can't get file meta from url");
		final InputStreamBuilder inputStreamBuilder = resourceUrl::openStream;
		return of(fileName, typeMime, lastModified, length, inputStreamBuilder);
	}

	public static StreamFile of(final String fileName, final String typeMime, final Instant lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		return new StreamFile(fileName, typeMime, lastModified, length, inputStreamBuilder);
	}
}
