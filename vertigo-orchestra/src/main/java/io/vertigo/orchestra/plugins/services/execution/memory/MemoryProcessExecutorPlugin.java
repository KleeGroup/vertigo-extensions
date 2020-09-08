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
package io.vertigo.orchestra.plugins.services.execution.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.orchestra.definitions.ActivityDefinition;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.impl.services.execution.AbstractActivityEngine;
import io.vertigo.orchestra.impl.services.execution.ProcessExecutorPlugin;
import io.vertigo.orchestra.plugins.services.MapCodec;
import io.vertigo.orchestra.services.execution.ActivityEngine;
import io.vertigo.orchestra.services.execution.ActivityExecutionWorkspace;
import io.vertigo.orchestra.services.execution.ExecutionState;

/**
 * Executeur de processus non supervisés.
 * @author mlaroche
 *
 */
public class MemoryProcessExecutorPlugin implements ProcessExecutorPlugin, Activeable {

	private static final Logger LOGGER = LogManager.getLogger(MemoryProcessExecutorPlugin.class);

	private final ExecutorService localExecutor;
	private final MapCodec mapCodec = new MapCodec();

	@Inject
	private AnalyticsManager analyticsManager;

	/**
	 * Constructeur de l'executeur simple local.
	 * @param workersCount le nombre de workers du pool
	 */
	@Inject
	public MemoryProcessExecutorPlugin(@ParamValue("workersCount") final Integer workersCount) {
		Assertion.check().isNotNull(workersCount);
		// ---
		localExecutor = Executors.newFixedThreadPool(workersCount);
	}

	@Override
	public void start() {
		// nothing

	}

	@Override
	public void stop() {
		localExecutor.shutdown();

	}

	@Override
	public void execute(final ProcessDefinition processDefinition, final Optional<String> initialParams) {
		localExecutor.submit(() -> doSequentialExecute(processDefinition, initialParams));

	}

	private void doSequentialExecute(final ProcessDefinition processDefinition, final Optional<String> initialParams) {
		final Map<String, String> myInitialParams = new HashMap<>();
		myInitialParams.putAll(processDefinition.getTriggeringStrategy().getInitialParams());
		final ActivityExecutionWorkspace initialWorkspace = new ActivityExecutionWorkspace(myInitialParams);
		if (initialParams.isPresent()) {
			initialWorkspace.addExternalParams(mapCodec.decode(initialParams.get()));
		}

		analyticsManager.trace("jobs", processDefinition.getName(), tracer -> {
			tracer.setMeasure("success", 100.0); // initial value is 100
			ActivityExecutionWorkspace resultWorkspace = initialWorkspace;
			for (final ActivityDefinition activityDefinition : processDefinition.getActivities()) {
				final ActivityExecutionWorkspace workspaceIn = new ActivityExecutionWorkspace(resultWorkspace.asMap());
				resultWorkspace = analyticsManager.traceWithReturn("activity", processDefinition.getName(), tracerActivity -> {
					final ActivityExecutionWorkspace result = executeActivity(activityDefinition, workspaceIn);
					if (result.isFailure()) {
						tracerActivity.setMeasure("success", 0.0);
					} else {
						tracerActivity.setMeasure("success", 100.0);
					}
					return result;
				});
				if (resultWorkspace.isFailure()) {
					tracer.setMeasure("success", 0.0);
					break;
				}
			}
		});

	}

	private ActivityExecutionWorkspace executeActivity(final ActivityDefinition activityDefinition, final ActivityExecutionWorkspace workspaceIn) {
		ActivityExecutionWorkspace resultWorkspace = workspaceIn;
		try {
			// ---
			final ActivityEngine activityEngine = InjectorUtil.newInstance(activityDefinition.getEngineClass());

			try {

				// If the engine extends the abstractEngine we can provide the services associated (LOGGING,...) so we log the workspace
				if (activityEngine instanceof AbstractActivityEngine) {
					final String workspaceInLog = new StringBuilder("Workspace in :").append(mapCodec.encode(workspaceIn.asMap())).toString();
					((AbstractActivityEngine) activityEngine).getLogger().info(workspaceInLog);
				}
				// We try the execution and we keep the result
				resultWorkspace = activityEngine.execute(workspaceIn);
				Assertion.check()
						.isNotNull(resultWorkspace)
						.isNotNull(resultWorkspace.getValue("status"), "Le status est obligatoire dans le résultat");
				// we call the posttreament
				resultWorkspace = activityEngine.successfulPostTreatment(resultWorkspace);

			} catch (final Exception e) {
				// In case of failure we keep the current workspace
				resultWorkspace.setFailure();
				LOGGER.error("Erreur de l'activité : " + activityDefinition.getEngineClass().getName(), e);
				// we call the posttreament
				resultWorkspace = activityEngine.errorPostTreatment(resultWorkspace, e);

			}

		} catch (final Exception e) {
			// Informative log
			resultWorkspace.setFailure();
			LOGGER.error("Erreur de l'activité : " + activityDefinition.getEngineClass().getName(), e);
		}
		return resultWorkspace;
	}

	@Override
	public ProcessType getHandledProcessType() {
		return ProcessType.UNSUPERVISED;
	}

	@Override
	public void endPendingActivityExecution(final Long activityExecutionId, final String token, final ExecutionState state, final Optional<String> errorMessageOpt) {
		// unsupported
		throw new UnsupportedOperationException("A daemon execution cannot be set pending");

	}

	@Override
	public void setActivityExecutionPending(final Long activityExecutionId, final ActivityExecutionWorkspace workspace) {
		// unsupported
		throw new UnsupportedOperationException("A daemon execution cannot be set pending");

	}

}
