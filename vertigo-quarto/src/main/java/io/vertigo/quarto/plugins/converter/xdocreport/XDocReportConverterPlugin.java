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
package io.vertigo.quarto.plugins.converter.xdocreport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.Type3Font;

import fr.opensagres.xdocreport.converter.ConverterRegistry;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.IConverter;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.XDocConverterException;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.util.TempFile;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.quarto.impl.converter.ConverterPlugin;

/**
 * Plugin de conversion du format ODT au format PDF.
 * Utilisant la librairie xdocreport.
 *
 * @author jgarnier
 */
public final class XDocReportConverterPlugin implements ConverterPlugin {

	@Override
	public VFile convertToFormat(final VFile file, final String targetFormat) {
		final ConverterFormat targetConverterFormat = ConverterFormat.find(targetFormat);
		Assertion.check().isFalse(targetConverterFormat.getTypeMime().equals(file.getMimeType()),
				"Le format de sortie est identique à celui d'entrée ; la conversion est inutile");
		final DocumentKind inputFormat = DocumentKind.fromMimeType(file.getMimeType());
		Assertion.check()
				.isNotNull(inputFormat,
						"Seul les formats " + Arrays.toString(DocumentKind.values()) + " peuvent être utilisés en entrée (typeMime " + file.getMimeType() + " non supporté)")
				.isTrue(targetFormat.equalsIgnoreCase(ConverterFormat.PDF.name()),
						"Seul le format PDF peut être utilisé en sortie");
		//-----

		final Options options = Options.getFrom(inputFormat).to(ConverterTypeTo.PDF);
		final IConverter converter = ConverterRegistry.getRegistry().getConverter(options);
		final SnapshotBuiltinFont snapshotBuiltinFont = new SnapshotBuiltinFont();

		try (InputStream in = file.createInputStream()) {
			String fileName = file.getFileName();
			final int lastPeriod = fileName.lastIndexOf('.');
			if (lastPeriod > -1) {
				fileName = fileName.substring(0, lastPeriod);
			}
			final TempFile resultFile = new TempFile(fileName, '.' + targetFormat.toLowerCase(Locale.ENGLISH));
			try (final OutputStream out = Files.newOutputStream(resultFile.toPath())) {
				converter.convert(in, out, options);
			}
			return FSFile.of(resultFile.getName(), ConverterFormat.PDF.getTypeMime(), resultFile.toPath());
		} catch (final IOException | XDocConverterException e) {
			throw WrappedException.wrap(e);
		} finally {
			snapshotBuiltinFont.restoreDefaultBuiltinFonts(); //xdocreport surcharge les font par défaut d'iText.
		}
	}

	/**
	 * Used to saved iText builtin font, overrided by xDocReport.
	 * @author npiedeloup
	 */
	protected static final class SnapshotBuiltinFont extends Type3Font {
		private final Map<String, PdfName> savedBuiltinFonts14;

		/**
		 * Constructor.
		 * Take snapshot of iText's BuiltinFonts14.
		 */
		public SnapshotBuiltinFont() {
			super(null, false);
			synchronized (BuiltinFonts14) {
				Assertion.check().isTrue(BuiltinFonts14.size() >= 14, "Default iText BuiltinFonts14, not correclty loaded (only {0} elements instead of 14)", BuiltinFonts14.size());
				//----
				savedBuiltinFonts14 = Collections.unmodifiableMap(new HashMap<String, PdfName>(BuiltinFonts14));
			}
		}

		/**
		 * Restore snapshot of iText's BuiltinFonts14.
		 */
		public void restoreDefaultBuiltinFonts() {
			synchronized (BuiltinFonts14) {
				BuiltinFonts14.putAll(savedBuiltinFonts14);
			}
		}

	}
}
