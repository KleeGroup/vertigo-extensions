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

import javax.inject.Inject;

import com.lowagie.text.DocumentException;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.quarto.exporter.model.Export;
import io.vertigo.quarto.exporter.model.ExportFormat;
import io.vertigo.quarto.impl.exporter.ExporterPlugin;

/**
 * Plugin d'export PDF.
 *
 * @author pchretien, npiedeloup
 */
public final class RTFExporterPlugin implements ExporterPlugin {
	private final EntityStoreManager entityStoreManager;
	private final SmartTypeManager smartTypeManager;

	@Inject
	public RTFExporterPlugin(final EntityStoreManager entityStoreManager, final SmartTypeManager smartTypeManager) {
		this.entityStoreManager = entityStoreManager;
		this.smartTypeManager = smartTypeManager;
	}

	/** {@inheritDoc} */
	@Override
	public void exportData(final Export export, final OutputStream out) throws DocumentException {
		new RTFExporter(entityStoreManager, smartTypeManager).exportData(export, out);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final ExportFormat exportFormat) {
		Assertion.check().isNotNull(exportFormat);
		//---
		return exportFormat == ExportFormat.RTF;
	}

}
