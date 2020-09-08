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
package io.vertigo.quarto.plugins.exporter.rtf;

import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfPageNumber;
import com.lowagie.text.rtf.field.RtfTotalPageNumber;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;

import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.quarto.plugins.exporter.pdfrtf.AbstractExporterIText;

/**
 * Handler export RTF with iText. Configure by ExportParametersRTF.
 *
 * @author evernat
 */
final class RTFExporter extends AbstractExporterIText {
	// l'implémentation de ExportHandlerRTF est majoritairement commune avec ExportHandlerPDF

	/**
	 * Constructor.
	 * @param storeManager Store manager
	 */
	RTFExporter(final EntityStoreManager entityStoreManager, final SmartTypeManager smartTypeManager) {
		super(entityStoreManager, smartTypeManager);
	}

	/** {@inheritDoc} */
	@Override
	protected void createWriter(final Document document, final OutputStream out) {
		RtfWriter2.getInstance(document, out);

		// advanced page numbers : x/y
		final Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
		//-----
		final Paragraph footerParagraph = new Paragraph();
		footerParagraph.add(new RtfPageNumber(font));
		footerParagraph.add(new Phrase(" / ", font));
		footerParagraph.add(new RtfTotalPageNumber(font));
		footerParagraph.setAlignment(Element.ALIGN_CENTER);
		//-----
		final HeaderFooter footer = new RtfHeaderFooter(footerParagraph);
		footer.setBorder(Rectangle.TOP);
		document.setFooter(footer);
	}
}
