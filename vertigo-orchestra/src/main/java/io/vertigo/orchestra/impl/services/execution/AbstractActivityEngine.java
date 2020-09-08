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

import javax.inject.Inject;

import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.orchestra.services.execution.ActivityEngine;
import io.vertigo.orchestra.services.execution.ActivityExecutionWorkspace;

/**
 * Activity engine abstrait offrant des services communs (logger)
 *
 * @author mlaroche.
 * @version $Id$
 */
public abstract class AbstractActivityEngine implements ActivityEngine {

	private final ActivityLogger activityLogger = new ActivityLogger(getClass().getName());

	@Inject
	private OrchestraServices processExecutionManager;

	/**
	 * Getter for the logger
	 * @return.
	 */
	public ActivityLogger getLogger() {
		return activityLogger;
	}

	/** {@inheritDoc} */
	@Override
	public ActivityExecutionWorkspace successfulPostTreatment(final ActivityExecutionWorkspace workspace) {
		//nothing
		return workspace;
	}

	/** {@inheritDoc} */
	@Override
	public ActivityExecutionWorkspace errorPostTreatment(final ActivityExecutionWorkspace workspace, final Exception e) {
		//nothing
		return workspace;
	}

	/**
	 * Place une activité en attente
	 * @param workspace le worspace de l'activité
	 * @return le workspace modifié
	 */
	public ActivityExecutionWorkspace setActivityPending(final ActivityExecutionWorkspace workspace) {
		workspace.setPending();
		processExecutionManager.getExecutor().setActivityExecutionPending(workspace.getActivityExecutionId(), workspace);
		return workspace;
	}

}
