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
package io.vertigo.quarto.exporter;

import io.vertigo.core.node.component.Manager;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.quarto.exporter.model.Export;

/**
 * Gestionnaire centralisé des éditions de données.
 * Le choix du type de report est fait par l'appelant qui fournit les paramètres adaptés à son besoin.
 *
 * @author pchretien, npiedeloup
 */
public interface ExporterManager extends Manager {

	/**
	 * Create a file from a config.
	 * @param export Config of the export
	 * @return sFile
	 */
	VFile createExportFile(final Export export);

}
