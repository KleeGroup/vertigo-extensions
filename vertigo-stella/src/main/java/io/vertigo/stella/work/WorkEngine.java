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
package io.vertigo.stella.work;

/**
 * Moteur d'exécution d'un travail.
 * Le moteur N'EST PAS THREADSAFE ; il doit donc être instancié à chaque utilisation.
 * Le moteur est créé par Injection de dépendances.
 *
 * @author   pchretien
 *
 * @param <R> result
 * @param <W> work
 */
public interface WorkEngine<W, R> {
	/**
	 * Exécute le travail.
	 * Le travail s'exécute dans la transaction courante si elle existe.
	 *  - Le moteur n'est pas responsable de de créer une transaction.
	 *  - En revanche si une telle transaction existe elle est utilisée.
	 * @param work paramétrage du WorkEngine
	 * @return WorkResult contenant les résultats
	 */
	R process(W work);
}
