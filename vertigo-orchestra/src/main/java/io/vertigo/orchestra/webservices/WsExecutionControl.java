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
package io.vertigo.orchestra.webservices;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.orchestra.services.execution.ExecutionState;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.InnerBodyParam;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.SessionLess;

/**
 * WebService API for managing Executions
 * @author mlaroche.
 * @version $Id$
 */
@PathPrefix("/orchestra/executionsControl")
public class WsExecutionControl implements WebServices {

	@Inject
	private OrchestraDefinitionManager orchestraDefinitionManager;

	@Inject
	private OrchestraServices orchestraServices;

	/**
	 * Termine une exécution mise en attente.
	 * @param activityExecutionId l'id de l'activité en attente
	 * @param token le token de sécurité
	 */
	@POST("/onSuccess")
	@SessionLess
	@AnonymousAccessAllowed
	public void endExecution(@InnerBodyParam("aceId") final Long activityExecutionId, @InnerBodyParam("token") final String token) {
		Assertion.check()
				.isNotNull(activityExecutionId)
				.isNotBlank(token);
		// ---
		orchestraServices.getExecutor()
				.endPendingActivityExecution(activityExecutionId, token, ExecutionState.DONE, Optional.empty());
	}

	/**
	 * Termine une exécution mise en attente.
	 * @param activityExecutionId l'id de l'activité en attente
	 * @param token le token de sécurité
	 * @param errorMessage Message d'erreur du tier
	 */
	@POST("/onError")
	@SessionLess
	@AnonymousAccessAllowed
	public void onError(@InnerBodyParam("aceId") final Long activityExecutionId, @InnerBodyParam("token") final String token, @InnerBodyParam("errorMessage") final String errorMessage) {
		Assertion.check()
				.isNotNull(activityExecutionId)
				.isNotBlank(token)
				.isNotBlank(errorMessage);
		// ---
		orchestraServices.getExecutor()
				.endPendingActivityExecution(activityExecutionId, token, ExecutionState.ERROR, Optional.of(errorMessage));
	}

	/**
	 * Lance l'execution d'un processus.
	 * @param processName le nom du processus à lancer
	 * @param initialParams des éventuels paramètres supplémentaire
	 */
	@POST("/execute")
	@SessionLess
	@AnonymousAccessAllowed
	public void executeNow(@InnerBodyParam("processName") final String processName, @InnerBodyParam("initialParams") final Map<String, String> initialParams) {
		Assertion.check().isNotNull(processName);
		// ---
		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition(processName);
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), initialParams);
	}

	/**
	 * Lance l'execution d'un processus avec son id.
	 * @param processName l'id du processus à lancer
	 */
	@POST("/executeNow")
	public void executeNowIhm(@InnerBodyParam("processName") final String processName) {
		Assertion.check().isNotBlank(processName);
		// ---
		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition(processName);
		orchestraServices.getScheduler()
				.scheduleAt(processDefinition, Instant.now(), Collections.emptyMap());
	}

}
