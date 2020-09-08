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
package io.vertigo.quarto.plugins.exporter.pdf;

import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.quarto.plugins.exporter.pdfrtf.AbstractExporterIText;

/**
 * Export PDF avec iText. Configuré par ExportParametersPDF.
 *
 * @author evernat
 */
final class PDFExporter extends AbstractExporterIText {
	/**
	 * Constructor.
	 * @param storeManager Store manager
	 */
	PDFExporter(final EntityStoreManager entityStoreManager, final SmartTypeManager smartTypeManager) {
		super(entityStoreManager, smartTypeManager);
	}

	/** {@inheritDoc} */
	@Override
	protected void createWriter(final Document document, final OutputStream out) throws DocumentException {
		final PdfWriter writer = PdfWriter.getInstance(document, out);
		// add the event handler for advanced page numbers : x/y
		writer.setPageEvent(new PDFAdvancedPageNumberEvents());
	}
}
