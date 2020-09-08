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
package io.vertigo.orchestra.util.monitoring;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.orchestra.dao.execution.OActivityExecutionDAO;
import io.vertigo.orchestra.dao.execution.OActivityLogDAO;
import io.vertigo.orchestra.dao.execution.OActivityWorkspaceDAO;
import io.vertigo.orchestra.dao.execution.OProcessExecutionDAO;
import io.vertigo.orchestra.dao.planification.OProcessPlanificationDAO;
import io.vertigo.orchestra.domain.execution.OActivityExecution;
import io.vertigo.orchestra.domain.execution.OActivityLog;
import io.vertigo.orchestra.domain.execution.OActivityWorkspace;
import io.vertigo.orchestra.domain.execution.OProcessExecution;
import io.vertigo.orchestra.domain.planification.OProcessPlanification;

/**
 * Implémentation des services de monitoring de la tour de contrôle.
 *
 * @author mlaroche.
 * @version $Id$
 */
@Transactional
public class MonitoringServicesImpl implements MonitoringServices {

	@Inject
	private OProcessExecutionDAO processExecutionDAO;
	@Inject
	private OActivityExecutionDAO activityExecutionDAO;

	@Inject
	private OActivityWorkspaceDAO activityWorkspaceDAO;
	@Inject
	private OActivityLogDAO activityLogDAO;
	@Inject
	private OProcessPlanificationDAO processPlanificationDAO;

	/** {@inheritDoc} */
	@Override
	public DtList<OProcessPlanification> getPlanificationsByProId(final Long proId) {
		Assertion.check().isNotNull(proId);
		// ---
		return processPlanificationDAO.getPlanificationsByProId(proId);
	}

	/** {@inheritDoc} */
	@Override
	public DtList<OProcessExecution> getExecutionsByProId(final Long proId) {
		Assertion.check().isNotNull(proId);
		// ---
		return processExecutionDAO.getExecutionsByProId(proId);
	}

	/** {@inheritDoc} */
	@Override
	public DtList<OActivityExecution> getActivityExecutionsByPreId(final Long preId) {
		Assertion.check().isNotNull(preId);
		// ---
		return activityExecutionDAO.getActivityExecutionsByPreId(preId);
	}

	/** {@inheritDoc} */
	@Override
	public OActivityWorkspace getActivityWorkspaceByAceId(final Long aceId, final boolean isIn) {
		Assertion.check().isNotNull(aceId);
		// ---
		return activityWorkspaceDAO.getActivityWorkspace(aceId, isIn).get();
	}

	/** {@inheritDoc} */
	@Override
	public Optional<OActivityLog> getActivityLogByAceId(final Long aceId) {
		Assertion.check().isNotNull(aceId);
		// ---
		return activityLogDAO.getActivityLogByAceId(aceId);

	}

}
