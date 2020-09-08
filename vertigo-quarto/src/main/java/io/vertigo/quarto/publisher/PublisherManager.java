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
package io.vertigo.quarto.publisher;

import java.net.URL;

import io.vertigo.core.node.component.Manager;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.quarto.publisher.model.PublisherData;

/**
 * Gestionnaire centralisé des éditions.
 * Le choix du type d'édition est fait par l'appelant qui fournit les paramètres adaptés à son besoin.
 *
 * @author pchretien, npiedeloup
 */
public interface PublisherManager extends Manager {
	/**
	 * Création d'une nouvelle édition.
	 * @param fileName Nom du document à générer (! pas son emplacement de stockage !)
	 * @param modelFileURL Chemin vers le fichier model
	 * @param data Données à fusionner avec le model
	 * @return Tache permettant la production d'un document au format passé en paramètre
	 */
	VFile publish(String fileName, URL modelFileURL, PublisherData data);
}
