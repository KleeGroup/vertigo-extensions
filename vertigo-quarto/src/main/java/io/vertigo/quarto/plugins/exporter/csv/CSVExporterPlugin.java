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
package io.vertigo.quarto.plugins.exporter.csv;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.quarto.exporter.model.Export;
import io.vertigo.quarto.exporter.model.ExportFormat;
import io.vertigo.quarto.impl.exporter.ExporterPlugin;

/**
 * This plugin is responsible for the export to CSV format.
 *
 * @author pchretien, npiedeloup
 */
public final class CSVExporterPlugin implements ExporterPlugin {
	private final CodecManager codecManager;
	private final EntityStoreManager entityStoreManager;
	private final SmartTypeManager smartTypeManager;

	/**
	 * Constructor.
	 *
	 * @param storeManager the storeManager
	 * @param codecManager the codecmanager
	 */
	@Inject
	public CSVExporterPlugin(
			final EntityStoreManager entityStoreManager,
			final CodecManager codecManager,
			final SmartTypeManager smartTypeManager) {
		Assertion.check()
				.isNotNull(entityStoreManager)
				.isNotNull(codecManager)
				.isNotNull(smartTypeManager);
		//-----
		this.codecManager = codecManager;
		this.entityStoreManager = entityStoreManager;
		this.smartTypeManager = smartTypeManager;
	}

	/** {@inheritDoc} */
	@Override
	public void exportData(final Export export, final OutputStream out) throws IOException {
		new CSVExporter(codecManager, entityStoreManager, smartTypeManager).exportData(export, out);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final ExportFormat exportFormat) {
		Assertion.check().isNotNull(exportFormat);
		//---
		return exportFormat == ExportFormat.CSV;
	}
}
