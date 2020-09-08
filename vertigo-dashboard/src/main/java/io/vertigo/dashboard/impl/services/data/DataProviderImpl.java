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
package io.vertigo.dashboard.impl.services.data;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthMeasureBuilder;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dashboard.services.data.DataProvider;
import io.vertigo.database.timeseries.ClusteredMeasure;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.TabularDatas;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.database.timeseries.TimeSeriesManager;
import io.vertigo.database.timeseries.TimedDatas;

public final class DataProviderImpl implements DataProvider {

	private final TimeSeriesManager timeSeriesManager;
	private final String appName;

	@Inject
	public DataProviderImpl(
			@ParamValue("appName") final Optional<String> appNameOpt,
			final TimeSeriesManager timeSeriesManager) {
		Assertion.check()
				.isNotNull(appNameOpt)
				.isNotNull(timeSeriesManager);
		//---
		appName = appNameOpt.orElseGet(() -> Node.getNode().getNodeConfig().getAppName());
		this.timeSeriesManager = timeSeriesManager;
	}

	@Override
	public TimedDatas getTimeSeries(final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.check()
				.isNotNull(measures)
				.isNotNull(dataFilter)
				.isNotNull(timeFilter.getDim());// we check dim is not null because we need it
		//---
		return timeSeriesManager.getTimeSeries(appName, measures, dataFilter, timeFilter);

	}

	@Override
	public TimedDatas getClusteredTimeSeries(final ClusteredMeasure clusteredMeasure, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.check()
				.isNotNull(dataFilter)
				.isNotNull(timeFilter)
				.isNotNull(timeFilter.getDim()) // we check dim is not null because we need it
				.isNotNull(clusteredMeasure)
				//---
				.isNotBlank(clusteredMeasure.getMeasure())
				.isNotNull(clusteredMeasure.getThresholds())
				.isFalse(clusteredMeasure.getThresholds().isEmpty(), "For clustering the measure '{0}' you need to provide at least one threshold", clusteredMeasure.getMeasure());
		//we use the natural order
		clusteredMeasure.getThresholds().sort(Comparator.naturalOrder());
		//---
		return timeSeriesManager.getClusteredTimeSeries(appName, clusteredMeasure, dataFilter, timeFilter);
	}

	@Override
	public TimedDatas getTabularTimedData(final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy) {
		return timeSeriesManager.getTabularTimedData(appName, measures, dataFilter, timeFilter, groupBy);
	}

	@Override
	public TabularDatas getTabularData(final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy) {
		return timeSeriesManager.getTabularData(appName, measures, dataFilter, timeFilter, groupBy);
	}

	@Override
	public TabularDatas getTops(final String measure, final DataFilter dataFilter, final TimeFilter timeFilter, final String groupBy, final int maxRows) {
		return timeSeriesManager.getTops(appName, measure, dataFilter, timeFilter, groupBy, maxRows);
	}

	@Override
	public List<String> getTagValues(final String measurement, final String tag) {
		return timeSeriesManager.getTagValues(appName, measurement, tag);
	}

	@Override
	public List<HealthCheck> getHealthChecks() {

		final List<String> measures = List.of("status:last", "message:last", "name:last", "module:last", "feature:last", "checker:last");
		final DataFilter dataFilter = DataFilter.builder("healthcheck").build();
		final TimeFilter timeFilter = TimeFilter.builder("now() - 5w", "now()").build();// before 5 weeks we consider that we don't have data

		return getTabularTimedData(measures, dataFilter, timeFilter, "name", "feature")
				.getTimedDataSeries()
				.stream()
				.map(timedDataSerie -> new HealthCheck(
						(String) timedDataSerie.getValues().get("name:last"),
						(String) timedDataSerie.getValues().get("checker:last"),
						(String) timedDataSerie.getValues().get("module:last"),
						(String) timedDataSerie.getValues().get("feature:last"),
						timedDataSerie.getTime(),
						buildHealthMeasure(
								(Double) timedDataSerie.getValues().get("status:last"),
								(String) timedDataSerie.getValues().get("message:last"))))
				.collect(Collectors.toList());

	}

	private static HealthMeasure buildHealthMeasure(final Double status, final String message) {
		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();
		switch (status.intValue()) {
			case 0:
				healthMeasureBuilder
						.withRedStatus(message, null);
				break;
			case 1:
				healthMeasureBuilder
						.withYellowStatus(message, null);
				break;
			case 2:
				healthMeasureBuilder
						.withGreenStatus(message);
				break;
			default:
				throw new IllegalArgumentException("HealthStatus with number '" + status + "' is unknown");
		}
		return healthMeasureBuilder.build();
	}

	@Override
	public List<Metric> getMetrics() {
		final List<String> measures = List.of("value:last", "name:last", "feature:last");
		final DataFilter dataFilter = DataFilter.builder("metric").build();
		final TimeFilter timeFilter = TimeFilter.builder("now() - 5w", "now()").build();// before 5 weeks we consider that we don't have data

		return getTabularTimedData(measures, dataFilter, timeFilter, "name", "feature")
				.getTimedDataSeries()
				.stream()
				.filter(timedDataSerie -> timedDataSerie.getValues().get("value:last") != null)
				.map(timedDataSerie -> Metric.builder()
						.withName((String) timedDataSerie.getValues().get("name:last"))
						.withFeature((String) timedDataSerie.getValues().get("feature:last"))
						.withMeasureInstant(timedDataSerie.getTime())
						.withValue((Double) timedDataSerie.getValues().get("value:last"))
						.withSuccess()
						.build())
				.collect(Collectors.toList());

	}

}
