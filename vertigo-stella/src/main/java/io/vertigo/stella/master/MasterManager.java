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
package io.vertigo.stella.master;

import io.vertigo.core.node.component.Manager;
import io.vertigo.stella.work.WorkEngine;

/**
 * Gestion synchrone, asynchrone des taches à effectuer.
 * Chaque réalisation est effectuée par un {@link WorkEngine}.
 *
 * Les exécutions peuvent être
 *  - locales ou distribuées (par configuration)
 *  - synchrones ou asynchrones (selon la méthode appelée)
 *
 * Toutes les exécutions distribuées sont techniquement réalisées de façon asynchrones.
 * Dans le cas des appels synchrones de méthodes distribuées, un mécanisme resynchronise le résultat
 *
 * @author pchretien
 */
public interface MasterManager extends Manager {
	/**
	 * Exécution d'un travail de façon synchrone.
	 * @param <W> Type de Work (Travail)
	 * @param <R> Produit d'un work à l'issu de son exécution
	 * @param work Travail à exécuter
	 * @param workEngineProvider WorkEngine provider
	 * @return result
	 */
	<W, R> WorkPromise<R> process(final W work, final Class<? extends WorkEngine<W, R>> workEngineClass);

	/**
	 * Lancement asynchrone d'un travail 'dès que possible'.
	 * @param <W> Type de Work (Travail)
	 * @param <R> Produit d'un work à l'issu de son exécution
	 * @param work Travail à exécuter
	 * @param workEngineProvider WorkEngine provider
	 * @param workResultHandler Handler permettant un callback après exécution
	 */
	<W, R> void schedule(final W work, Class<? extends WorkEngine<W, R>> workEngineClass, WorkResultHandler<R> workResultHandler);

}
