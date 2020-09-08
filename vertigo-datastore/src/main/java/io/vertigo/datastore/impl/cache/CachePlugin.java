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
package io.vertigo.datastore.impl.cache;

import java.io.Serializable;

import io.vertigo.core.analytics.health.HealthChecked;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthMeasureBuilder;
import io.vertigo.core.node.component.Plugin;

/**
 * Plugin de gestion de cache.
 *
 * @author pchretien
 */
public interface CachePlugin extends Plugin {
	/**
	 * Ajoute Objet dans le cache.
	 * Si le context n'existe pas, il est crée.
	 * Si la clé existe déjà, l'objet précédent est remplacé.
	 *
	 * @param cacheName Contexte de cache
	 * @param key Clé de l'objet à insérer
	 * @param value Objet à insérer
	 */
	void put(final String cacheName, final Serializable key, final Object value);

	/**
	 * Cette methode rend l'objet désigne par le contexte et le handle donnée en entrée.
	 * Si le contexte n'existe pas, une exception IllegalArgumentException.
	 * Si le handle n'existe pas, ou l'objet n'a pas ou plus de reference en cache, l'objet renvoyé et un null.
	 *
	 * @param cacheName Contexte de cache
	 * @param key Clé de l'objet à récupérer
	 * @return Objet demandé ou null si non trouvé
	 */
	Object get(String cacheName, Serializable key);

	/**
	 * Suppression du cache de l'objet référencé par sa clé.
	 *
	 * @param cacheName Contexte de cache
	 * @param key Clé de l'objet à supprimer
	 * @return Si objet supprimé
	 */
	boolean remove(String cacheName, Serializable key);

	/**
	 * Effacement du contenu d'un contexte.
	 * Supprime tous les items du cache.
	 *
	 * @param cacheName Contexte de cache
	 */
	void clear(String cacheName);

	/**
	 * Effacement du contenu de TOUS les Contextes de cache.
	 */
	void clearAll();

	/**
	 * @return HealthMeasure of this plugin
	 */
	@HealthChecked(name = "io", feature = "cache")
	default HealthMeasure checkIo() {
		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();
		try {
			put("CacheHealthVertigo", "healthcheckkey", "healthcheckvalue");
			get("CacheHealthVertigo", "healthcheckkey");
			remove("CacheHealthVertigo", "healthcheckkey");
			healthMeasureBuilder.withGreenStatus();
		} catch (final Exception e) {
			healthMeasureBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthMeasureBuilder.build();

	}
}
