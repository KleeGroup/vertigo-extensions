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

import java.util.Optional;

import io.vertigo.orchestra.definitions.ProcessDefinition;

public interface ProcessExecutor {
	/**
	 * Execute un processus.
	 * @param processDefinition le processus à lancer
	 * @param initialParams paramètres initiaux supplémentaires
	 */
	void execute(ProcessDefinition processDefinition, Optional<String> initialParams);

	/**
	 * Termine une execution mise en attente.
	 * @param activityExecutionId L'id de l'execution à terminer
	 * @param token Le ticket associé permettant de s'assurer que n'importe qui ne termine pas une activity (seulement un callback)
	 * @param state L'état futur de l'activité
	 * @param errorMessage an optional ErrorMessage
	 *
	 */
	void endPendingActivityExecution(final Long activityExecutionId, final String token, final ExecutionState state, final Optional<String> errorMessageOpt);

	/**
	 * Mets une execution en attente.
	 * @param activityExecutionId L'id de l'execution à mettre en attente
	 * @param workspace Le workspace avant la mise en attente
	 */
	void setActivityExecutionPending(final Long activityExecutionId, final ActivityExecutionWorkspace workspace);

}
