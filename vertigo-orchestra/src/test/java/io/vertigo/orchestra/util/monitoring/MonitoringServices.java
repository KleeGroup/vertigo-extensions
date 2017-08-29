/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.orchestra.util.monitoring;

import java.util.Optional;

import io.vertigo.core.component.Component;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.orchestra.domain.execution.OActivityExecution;
import io.vertigo.orchestra.domain.execution.OActivityLog;
import io.vertigo.orchestra.domain.execution.OActivityWorkspace;
import io.vertigo.orchestra.domain.execution.OJobRunning;
import io.vertigo.orchestra.domain.execution.OProcessExecution;
import io.vertigo.orchestra.domain.planification.OProcessNextRun;

/**
 * Services permettant de suivre le fonctionnement de la tour de controle.
 * @author mlaroche.
 */
public interface MonitoringServices extends Component {

	/**
	 * Récupère la liste des planifications liés à un processus.
	 * @param proId l'id du processus
	 * @return la liste des planification concernant ce processus.
	 */
	DtList<OProcessNextRun> getPlanificationsByProId(Long proId);

	/**
	 * Récupère la liste des executions d'un processus.
	 * @param proId
	 * @return la liste des executions concernant ce processus.
	 */
	DtList<OJobRunning> getRunningExecutionsByProId(Long proId);

	/**
	 * Récupère la liste des executions d'un processus.
	 * @param proId
	 * @return la liste des executions concernant ce processus.
	 */
	DtList<OProcessExecution> getExecutionsByProId(Long proId);

	/**
	 * Récupère la liste des tâches executées lors d'une execution de processus.
	 * @param preId l'id de l'execution de processus
	 * @return la liste des tâches executées pour cette execution de processus.
	 */
	DtList<OActivityExecution> getActivityExecutionsByPreId(Long preId);

	/**
	 * Récupère un workspace associé à une execution de tâche.
	 * @param aceId l'id d'execution de tâche
	 * @param isIn true : workspace entrant, false : workspace sortant
	 * @return un workspace de tâche.
	 */
	OActivityWorkspace getActivityWorkspaceByAceId(Long aceId, final boolean isIn);

	/**
	 * Récupère un log associé à une execution de tâche.
	 * @param aceId l'id d'execution de tâche
	 * @return un workspace de tâche.
	 */
	Optional<OActivityLog> getActivityLogByAceId(Long aceId);

}
