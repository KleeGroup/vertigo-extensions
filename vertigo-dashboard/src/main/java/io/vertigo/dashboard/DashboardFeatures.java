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
package io.vertigo.dashboard;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.dashboard.impl.services.data.DataProviderImpl;
import io.vertigo.dashboard.services.data.DataProvider;
import io.vertigo.dashboard.ui.DashboardUiManager;
import io.vertigo.dashboard.webservices.DashboardDataProviderWebServices;

public class DashboardFeatures extends Features<DashboardFeatures> {

	public DashboardFeatures() {
		super("vertigo-dashboard");
	}

	@Feature("analytics")
	public DashboardFeatures withAnalytics(final Param... params) {
		if (params.length > 0) {
			Assertion.check().isTrue(params.length == 1 && "appName".equals(params[0].getName()), "appName param should be provided ");
			//---
			getModuleConfigBuilder()
					.addComponent(DataProvider.class, DataProviderImpl.class, params);
		} else {
			getModuleConfigBuilder()
					.addComponent(DataProvider.class, DataProviderImpl.class);
		}
		getModuleConfigBuilder().addComponent(DashboardDataProviderWebServices.class);
		return this;
	}

	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(DashboardUiManager.class);

	}

}
