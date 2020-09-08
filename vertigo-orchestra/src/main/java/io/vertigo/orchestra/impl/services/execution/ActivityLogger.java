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
package io.vertigo.orchestra.impl.services.execution;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;

/**
 * Activity Logger.
 *
 * @author mlaroche.
 */
public final class ActivityLogger {
	private final Logger loggerActivity;
	private final StringBuilder log = new StringBuilder();
	private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").withZone(ZoneId.systemDefault());

	/**
	 * Constructeur.
	 * @param engineName le nom de l'engine de l'activité
	 */
	ActivityLogger(final String engineName) {
		Assertion.check().isNotBlank(engineName);
		// ---
		// Creates or get the logger
		loggerActivity = LogManager.getLogger(engineName);
	}

	/**
	 * Ajoute une information dans le log.
	 * @param message le message
	 */
	public void info(final String message) {

		// We log in Orchestra
		log.append(dateFormat.format(Instant.now())).append(" [Info] ").append(message).append('\n');
		// We log in Log4j
		loggerActivity.info(message);
	}

	/**
	 * Ajoute un avertissement dans le log.
	 * @param message le message
	 */
	public void warn(final String message) {
		// We log in Orchestra
		log.append(dateFormat.format(Instant.now())).append(" [Warn] ").append(message).append('\n');
		// We log in Log4j
		loggerActivity.warn(message);
	}

	/**
	 * Ajoute une erreur dans le log.
	 * @param message le message
	 */
	public void error(final String message) {
		// We log in Orchestra
		log.append(dateFormat.format(Instant.now())).append(" [Error] ").append(message).append('\n');
		// We log in Log4j
		loggerActivity.error(message);
	}

	/**
	 * Récupère le log global sous forme de string.
	 * @return le log complet
	 */
	public String getLogAsString() {
		return log.toString();
	}

}
