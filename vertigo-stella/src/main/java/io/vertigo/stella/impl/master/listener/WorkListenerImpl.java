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
package io.vertigo.stella.impl.master.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.stella.master.MasterManager;

/**
 * Implémentation standard du Listener de réception des événements produits par l'exécution des taches.
 * @author pchretien
 */
public final class WorkListenerImpl implements WorkListener {

	/** Mécanisme de log utilisé pour les taches. */
	private static final Logger LOGGER = LogManager.getLogger(MasterManager.class);

	/** Mécanisme de log utilisé pour les performances. */
	private static final Logger LOGGER_PERFORMANCE = LogManager.getLogger("Performance");

	private static void logWorkStart(final String workName) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Execution tache :  {}", workName);
		}
	}

	private static void logWorkFinish(final String workName, final long elapsedTime, final boolean success) {
		if (LOGGER_PERFORMANCE.isInfoEnabled()) {
			LOGGER_PERFORMANCE.info(">> Tache : {} : time = {}", workName, elapsedTime);
		}
		if (LOGGER.isInfoEnabled()) {
			if (success) {
				LOGGER.info("Execution tache : {} reussie en  ( {} ms)", workName, elapsedTime);
			} else {
				LOGGER.info("Execution tache : {} interrompue apres ( {} ms)", workName, elapsedTime);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onStart(final String workName) {
		logWorkStart(workName);
	}

	/** {@inheritDoc} */
	@Override
	public void onFinish(final String workName, final long elapsedTime, final boolean success) {
		logWorkFinish(workName, elapsedTime, success);
	}
}
