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
package io.vertigo.dashboard.ui.dynamo.model;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;

public class SmartTypeModel {
	private final SmartTypeDefinition smartTypeDefinition;
	private final Double taskCount; // may be null for UI (displayed as N/A)
	private final Double dtDefinitionCount; // may be null for UI (displayed as N/A)

	public SmartTypeModel(final SmartTypeDefinition smartTypeDefinition, final Double taskCount, final Double dtDefinitionCount) {
		Assertion.check().isNotNull(smartTypeDefinition);
		//---
		this.smartTypeDefinition = smartTypeDefinition;
		this.taskCount = taskCount;
		this.dtDefinitionCount = dtDefinitionCount;
	}

	public String getName() {
		return smartTypeDefinition.getName();
	}

	public boolean isOrphan() {
		if (taskCount != null && dtDefinitionCount != null) {
			return taskCount + dtDefinitionCount == 0;
		} else if (taskCount != null) {// dtDefinitionCount null
			return taskCount == 0;
		}
		// both nulls
		return false;
	}

	public Double getTaskCount() {
		return taskCount;
	}

	public Double getDtDefinitionCount() {
		return dtDefinitionCount;
	}

	public Double getUsageCount() {
		return taskCount + dtDefinitionCount;
	}

}
