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

import java.util.Map;
import java.util.Map.Entry;

import io.vertigo.core.lang.Assertion;

/**
 * ActivityExecutionWorkspace.
 * This is limited to a key(string) value(string) workspace.
 *
 * @author mlaroche.
 * @version $Id$
 */
public final class ActivityExecutionWorkspace {

	public static final String STATUS_KEY = "status";
	public static final String ACE_ID_KEY = "activityExecutionId";
	public static final String PRE_ID_KEY = "processExecutionId";
	public static final String PROCESS_NAME_KEY = "processName";
	public static final String TOKEN_KEY = "token";
	public static final String ATTACHMENT_KEY = "attachment";
	public static final String PARSING_ERROR_KEY = "parsingError";

	private final Map<String, String> params;

	/**
	 * Construction d'un workspace.
	 * @param params un workspace sous forme de Map
	 */
	public ActivityExecutionWorkspace(final Map<String, String> params) {
		Assertion.check().isNotNull(params);
		//---
		this.params = params;
	}

	/**
	 * Retourne la valeur stockée dans le workspace correspondant à une clé.
	 * @param key la clé
	 * @return la valeur
	 */
	public String getValue(final String key) {
		Assertion.check().isNotNull(key);
		//---
		return params.get(key);
	}

	/**
	 * Permet de savoir si une clé est déjà définie dans le workspace.
	 * @param key la clé à tester
	 * @return true si la clé existe
	 */
	public boolean containsKey(final String key) {
		Assertion.check().isNotNull(key);
		//---
		return params.containsKey(key);
	}

	/**
	 * Affecte la valeur stockée dans le workspace correspondant à une clé.
	 * @param key la clé
	 * @param value la valeur
	 */
	public void setValue(final String key, final String value) {
		Assertion.check()
				.isNotNull(key)
				.isFalse(STATUS_KEY.equals(key), "Status cannot be set directly");
		// ---
		params.put(key, value);
	}

	/**
	 * Retire une propriété du workspace.
	 * @param key la clé à retirer
	 */
	public void removeKey(final String key) {
		Assertion.check().isNotNull(key);
		// ---
		params.remove(key);
	}

	/**
	 * Ajoute de paramètres externe au workspace.
	 * @param extParams des paramètres suppplémentaire
	 */
	public void addExternalParams(final Map<String, String> extParams) {
		for (final Entry<String, String> entry : extParams.entrySet()) {
			// we support only string params
			if (entry.getValue() != null) {
				setValue(entry.getKey(), entry.getValue());
			}
		}
	}

	// --- Always here

	/**
	 * Affecte l'id de l'execution en cours.
	 * @param aceId l'id de l'activité
	 */
	public void setActivityExecutionId(final Long aceId) {
		params.put(ACE_ID_KEY, aceId.toString());
	}

	/**
	 * Retourne l'id de l'execution en cours.
	 * @return l'id de l'exécution
	 */
	public Long getActivityExecutionId() {
		return Long.valueOf(getValue(ACE_ID_KEY));
	}

	/**
	 * Affecte l'id de l'execution en cours.
	 * @param preId l'id du processus
	 */
	public void setProcessExecutionId(final Long preId) {
		params.put(PRE_ID_KEY, preId.toString());
	}

	/**
	 * Retourne l'id de l'execution en cours.
	 * @return l'id de l'exécution
	 */
	public Long getProcessExecutionId() {
		return Long.valueOf(getValue(PRE_ID_KEY));
	}

	/**
	 * Affecte le nom du processus en cours de traitement.
	 * @param processName le nom du processus en cours
	 */
	public void setProcessName(final String processName) {
		params.put(PROCESS_NAME_KEY, processName);
	}

	/**
	 * Retourne le nom du processus en cours de traitement.
	 * @return le nom du processus
	 */
	public String getProcessName() {
		return getValue(PROCESS_NAME_KEY);
	}

	/**
	 * Assigne le token.
	 * @param token le token
	 */
	public void setToken(final String token) {
		params.put(TOKEN_KEY, token);
	}

	/**
	 * Retourne le token de sécurité de l'activité.
	 * @return le token
	 */
	public String getToken() {
		return getValue(TOKEN_KEY);
	}

	/**
	 * Assigne le chemin relatif du fichier de log (par rapport au root orchestra).
	 */
	public void setAttachment(final String logFile) {
		params.put(ATTACHMENT_KEY, logFile);
	}

	/**
	 * Retourne le chemin relatif du fichier de log.
	 */
	public String getAttachment() {
		return getValue(ATTACHMENT_KEY);
	}

	/**
	 * Remet à zéro la variable spécifiant le fichier de log.
	 */
	public void resetAttachment() {
		params.remove(ATTACHMENT_KEY);
	}

	// --- Execution State in workspace

	/**
	 * Passe l'état à succès.
	 */
	public void setSuccess() {
		params.put(STATUS_KEY, "ok");
	}

	/**
	 * Passe l'état à KO.
	 */
	public void setFailure() {
		params.put(STATUS_KEY, "ko");
	}

	/**
	 * Passe l'état à en attente.
	 */
	public void setPending() {
		params.put(STATUS_KEY, "pending");
	}

	/**
	 * Passe l'état à fini.
	 */
	public void setFinished() {
		params.put(STATUS_KEY, "finished");
	}

	/**
	 * Le status de l'activité est-il succès.
	 * @return true si le statut est succès
	 */
	public boolean isSuccess() {
		return "ok".equals(getValue(STATUS_KEY));
	}

	/**
	 * Le status de l'activité est-il KO.
	 * @return true si le statut est KO
	 */
	public boolean isFailure() {
		return "ko".equals(getValue(STATUS_KEY));
	}

	/**
	 * Le status de l'activité est-il en attente.
	 * @return true si le statut est en attente
	 */
	public boolean isPending() {
		return "pending".equals(getValue(STATUS_KEY));
	}

	/**
	 * Le status de l'activité est-il fini.
	 * @return true si le statut est fini
	 */
	public boolean isFinished() {
		return "finished".equals(getValue(STATUS_KEY));
	}

	/**
	 * Reset le status du workspace
	 */
	public void resetStatus() {
		params.remove(STATUS_KEY);
	}

	public Map<String, String> asMap() {
		return params;
	}

}
