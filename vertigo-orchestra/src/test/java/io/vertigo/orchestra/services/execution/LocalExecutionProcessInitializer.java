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

import javax.inject.Inject;

import io.vertigo.core.node.component.ComponentInitializer;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.services.execution.engine.TestJob;
import io.vertigo.orchestra.services.execution.engine.TestJob2;
import io.vertigo.orchestra.services.execution.engine.TestJob3;
import io.vertigo.orchestra.services.execution.engine.TestJobScheduled;

public class LocalExecutionProcessInitializer implements ComponentInitializer {

	@Inject
	private OrchestraDefinitionManager orchestraDefinitionManager;

	@Override
	public void init() {

		final ProcessDefinition processDefinition = ProcessDefinition.legacyBuilder("ProTestUnsupervisedManual", TestJob.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition);

		final ProcessDefinition processDefinition2 = ProcessDefinition.legacyBuilder("ProTestUnsupervisedManual2", TestJob.class)
				.addActivity("Second", "second", TestJob2.class)
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition2);

		final ProcessDefinition processDefinition3 = ProcessDefinition.builder("ProTestUnsupervisedManual3", "ProTestUnsupervisedManual3")
				.addActivity("first", "first", TestJob3.class)
				.withProcessType(ProcessType.UNSUPERVISED)
				.addInitialParam(TestJob3.PARAM_KEY_1, "value1")
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition3);

		final ProcessDefinition processDefinition4 = ProcessDefinition.legacyBuilder("ProTestUnsupervisedRecurrent", TestJobScheduled.class)
				.withCronExpression("*/5 * * * * ?")
				.build();

		orchestraDefinitionManager.createOrUpdateDefinition(processDefinition4);

	}

}
