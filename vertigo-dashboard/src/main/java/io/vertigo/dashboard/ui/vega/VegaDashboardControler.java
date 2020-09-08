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
package io.vertigo.dashboard.ui.vega;

import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.VUserException;
import io.vertigo.core.node.Node;
import io.vertigo.dashboard.ui.AbstractDashboardModuleControler;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.database.timeseries.TimedDatas;

public final class VegaDashboardControler extends AbstractDashboardModuleControler {

	@Override
	public void doBuildModel(final Node node, final Map<String, Object> model) {
		addGlobalIndicators(model);
		//
	}

	private void addGlobalIndicators(final Map<String, Object> model) {
		final DataFilter dataFilter = DataFilter.builder("webservices").build();
		final DataFilter dataFilterExceptions = DataFilter.builder("webservices")
				.withAdditionalWhereClause("\"exception\" != '' and \"exception\" != '" + VUserException.class.getCanonicalName() + "'")
				.build();
		final TimeFilter timeFilter = TimeFilter.builder("now() - 1w", "now()").withTimeDim("3w").build();
		//---
		final TimedDatas countAndMeanDuration = getDataProvider().getTimeSeries(List.of("duration:count", "duration:mean"), dataFilter, timeFilter);
		final TimedDatas numOfTechnicalExceptions = getDataProvider().getTimeSeries(List.of("duration:count"), dataFilterExceptions, timeFilter);

		double count = 0;
		final double meanDuration = 0;
		double exceptionRate = 0;
		if (!countAndMeanDuration.getTimedDataSeries().isEmpty()) {
			// we have one and only one result
			final Map<String, Object> values = countAndMeanDuration.getTimedDataSeries().get(0).getValues();
			final Object meanDurationValue = values.get("duration:count");
			if (meanDurationValue != null) {
				count = (Double) meanDurationValue;
			}
		}
		if (count > 0 && !numOfTechnicalExceptions.getTimedDataSeries().isEmpty()) {
			// we have one and only one result
			final Map<String, Object> values = numOfTechnicalExceptions.getTimedDataSeries().get(0).getValues();
			exceptionRate = (((Double) values.get("duration:count")) / count * 100);
		}
		model.put("webservicesCount", count);
		model.put("webservicesMeanDuration", meanDuration);
		model.put("webservicesExceptionRate", exceptionRate);

		//--- locations
		model.put("locations", getDataProvider().getTagValues("webservices", "location"));
	}

}
