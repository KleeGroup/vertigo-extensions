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
package io.vertigo.orchestra.services.log;

import java.util.Optional;

import io.vertigo.datastore.filestore.model.VFile;

public interface ProcessLogger {

	/**
	 * Récupère le fichier de log d'une execution de processus.
	 * @param processExecutionId L'id de l'execution
	 * @return le fichier de log
	 */
	Optional<VFile> getLogFileForProcess(final Long processExecutionId);

	/**
	 * Récupère le fichier de log d'une execution d'activité.
	 * @param actityExecutionId l'id de l'activité
	 * @return le fichier de log de l'activité
	 */
	Optional<VFile> getActivityAttachment(Long actityExecutionId);

	/**
	 * Récupère sous forme de fichier le log technique d'une activité. (Si l'activité possède un log)
	 * @param actityExecutionId l'id de l'activité
	 * @return le fichier de log technique de l'activité
	 */
	Optional<VFile> getActivityLogFile(Long actityExecutionId);
}
