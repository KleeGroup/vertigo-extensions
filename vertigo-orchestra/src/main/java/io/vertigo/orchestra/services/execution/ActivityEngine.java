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
package io.vertigo.orchestra.services.execution;

/**
 * Interface d'un engine de tâche.
 *
 * @author mlaroche.
 * @version $Id$
 */
public interface ActivityEngine {

	/**
	 * Réalise l'exécution d'une tache.
	 * @param workspace le workspace d'entrée
	 * @return le workspace de sortie
	 */
	ActivityExecutionWorkspace execute(final ActivityExecutionWorkspace workspace);

	/**
	 * Post-traitement à effectuer en cas de succès.
	 * @param workspace le workspace d'entrée
	 * @return le workspace de sortie
	 */
	ActivityExecutionWorkspace successfulPostTreatment(final ActivityExecutionWorkspace workspace);

	/**
	 * Post-traitement à effectuer en cas d'erreur.
	 * @param workspace le workspace d'entrée
	 * @param e l'exception rencontrée lors du traitement
	 * @return le workspace de sortie
	 */
	ActivityExecutionWorkspace errorPostTreatment(final ActivityExecutionWorkspace workspace, final Exception e);

}
