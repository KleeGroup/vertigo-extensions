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
package io.vertigo.dashboard.webservices;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dashboard.services.data.DataProvider;
import io.vertigo.database.timeseries.ClusteredMeasure;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.TabularDatas;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.database.timeseries.TimedDatas;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.InnerBodyParam;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.SessionLess;

@PathPrefix("/dashboard/data")
public class DashboardDataProviderWebServices implements WebServices {

	@Inject
	private DataProvider dataProvider;

	@SessionLess
	@AnonymousAccessAllowed
	@POST("/series")
	public TimedDatas getTimedDatas(
			@InnerBodyParam("measures") final List<String> measures,
			@InnerBodyParam("dataFilter") final DataFilter dataFilter,
			@InnerBodyParam("timeFilter") final TimeFilter timeFilter) {

		return dataProvider.getTimeSeries(measures, dataFilter, timeFilter);
	}

	@SessionLess
	@AnonymousAccessAllowed
	@POST("/series/clustered")
	public TimedDatas getClusteredTimedDatas(
			@InnerBodyParam("clusteredMeasure") final ClusteredMeasure clusteredMeasure,
			@InnerBodyParam("dataFilter") final DataFilter dataFilter,
			@InnerBodyParam("timeFilter") final TimeFilter timeFilter) {

		return dataProvider.getClusteredTimeSeries(clusteredMeasure, dataFilter, timeFilter);
	}

	@SessionLess
	@AnonymousAccessAllowed
	@POST("/tabular")
	public TabularDatas getTabularDatas(
			@InnerBodyParam("measures") final List<String> measures,
			@InnerBodyParam("dataFilter") final DataFilter dataFilter,
			@InnerBodyParam("timeFilter") final TimeFilter timeFilter,
			@InnerBodyParam("groupBy") final String groupBy) {

		return dataProvider.getTabularData(measures, dataFilter, timeFilter, groupBy);
	}

	@SessionLess
	@AnonymousAccessAllowed
	@POST("/tabular/tops")
	public TabularDatas getTops(
			@InnerBodyParam("measures") final List<String> measures,
			@InnerBodyParam("dataFilter") final DataFilter dataFilter,
			@InnerBodyParam("timeFilter") final TimeFilter timeFilter,
			@InnerBodyParam("groupBy") final String groupBy,
			@InnerBodyParam("maxRows") final int maxRows) {
		Assertion.check().isTrue(measures.size() == 1, "One and only one measure must be queried for a top request");
		//---
		return dataProvider.getTops(measures.get(0), dataFilter, timeFilter, groupBy, maxRows);
	}

}
