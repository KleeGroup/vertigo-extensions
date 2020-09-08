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
package io.vertigo.dashboard.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.dashboard.services.data.DataProvider;

public abstract class AbstractDashboardModuleControler implements DashboardModuleControler {

	@Inject
	private DataProvider dataProvider;

	protected final DataProvider getDataProvider() {
		return dataProvider;
	}

	@Override
	public Map<String, Object> buildModel(final Node node, final String moduleName) {
		final Map<String, Object> model = new HashMap<>();
		//---
		initModuleModel(node, model, moduleName);
		doBuildModel(node, model);
		return model;
	}

	private void initModuleModel(final Node node, final Map<String, Object> model, final String moduleName) {
		final Set<String> modules = node.getNodeConfig().getModuleConfigs().stream().map(ModuleConfig::getName).collect(Collectors.toSet());
		//---
		model.put("modules", modules);
		//---
		final List<HealthCheck> healthChecks = dataProvider.getHealthChecks();
		final Map<String, List<HealthCheck>> healthChecksByFeature = healthChecks
				.stream()
				.filter(healthCheck -> moduleName.equals(healthCheck.getModule()))
				.collect(Collectors.groupingBy(HealthCheck::getFeature, Collectors.toList()));

		final Set<String> features = healthChecks
				.stream()
				.filter(healthCheck -> moduleName.equals(healthCheck.getModule()))
				.map(HealthCheck::getFeature)
				.collect(Collectors.toSet());

		//---
		model.put("features", features);
		model.put("healthchecksByFeature", healthChecksByFeature);
		model.put("moduleName", moduleName);
	}

	public abstract void doBuildModel(final Node node, final Map<String, Object> model);

}
