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
package io.vertigo.quarto.plugins.converter.openoffice.stream;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;

import com.sun.star.io.IOException;
import com.sun.star.io.XOutputStream;

import io.vertigo.core.lang.Assertion;

/**
 * Implémentation d'une OutpuStream spécifique pour les appels distants de OpenOffice.
 * @author tchassagnette
 */
public final class OOoFileOutputStream implements XOutputStream {
	private final OutputStream fileOutputStream;

	/**
	 * Constructor.
	 * @param file Fichier
	 * @throws java.io.IOException
	 */
	public OOoFileOutputStream(final File file) throws java.io.IOException {
		Assertion.check().isNotNull(file);
		//-----
		fileOutputStream = Files.newOutputStream(file.toPath());
	}

	//
	// Implement XOutputStream
	//

	/** {@inheritDoc} */
	@Override
	public void writeBytes(final byte[] values) throws IOException {
		try {
			fileOutputStream.write(values);
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void closeOutput() throws IOException {
		try {
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void flush() throws IOException {
		try {
			fileOutputStream.flush();
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	private IOException createSunIOException(final java.io.IOException e) {
		return new IOException(e.getMessage(), this);
	}
}
