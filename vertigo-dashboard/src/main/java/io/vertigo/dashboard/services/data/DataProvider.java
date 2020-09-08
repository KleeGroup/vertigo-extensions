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
package io.vertigo.dashboard.services.data;

import java.util.List;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.node.component.Component;
import io.vertigo.database.timeseries.ClusteredMeasure;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.TabularDatas;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.database.timeseries.TimedDatas;

public interface DataProvider extends Component {

	TimedDatas getTimeSeries(final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter);

	TimedDatas getClusteredTimeSeries(final ClusteredMeasure clusteredMeasure, final DataFilter dataFilter, final TimeFilter timeFilter);

	List<HealthCheck> getHealthChecks();

	List<Metric> getMetrics();

	TabularDatas getTabularData(final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy);

	TimedDatas getTabularTimedData(final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final String... groupBy);

	TabularDatas getTops(final String measure, final DataFilter dataFilter, final TimeFilter timeFilter, final String groupBy, final int maxRows);

	List<String> getTagValues(final String measurement, final String tag);

}
