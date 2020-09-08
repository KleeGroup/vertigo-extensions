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
package io.vertigo.dashboard.ui.dynamo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.node.Node;
import io.vertigo.dashboard.ui.AbstractDashboardModuleControler;
import io.vertigo.dashboard.ui.dynamo.model.EntityModel;
import io.vertigo.dashboard.ui.dynamo.model.SmartTypeModel;
import io.vertigo.dashboard.ui.dynamo.model.TaskModel;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.TabularDataSerie;
import io.vertigo.database.timeseries.TabularDatas;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtStereotype;
import io.vertigo.datamodel.task.definitions.TaskDefinition;

public final class DynamoDashboardControler extends AbstractDashboardModuleControler {

	@Override
	public void doBuildModel(final Node node, final Map<String, Object> model) {
		final List<Metric> metrics = getDataProvider().getMetrics();
		buildEntityModel(model, metrics);
		buildDomainModel(model, metrics);
		buildTaskModel(model);
	}

	private void buildTaskModel(final Map<String, Object> model) {
		final DataFilter dataFilter = DataFilter.builder("tasks").build();
		final TimeFilter timeFilter = TimeFilter.builder("now() - 1d", "now()").build();
		final TabularDatas tabularDatas = getDataProvider().getTabularData(Arrays.asList("duration:median", "duration:count"), dataFilter, timeFilter, "name");

		final List<TaskModel> tasks = Node.getNode().getDefinitionSpace().getAll(TaskDefinition.class)
				.stream()
				.map(taskDefinition -> new TaskModel(
						taskDefinition,
						getValue(tabularDatas, "/execute/" + taskDefinition.getName(), "duration:count"),
						getValue(tabularDatas, "/execute/" + taskDefinition.getName(), "duration:median")))
				.collect(Collectors.toList());

		model.put("tasks", tasks);

	}

	private static Double getValue(final TabularDatas tabularDatas, final String serieName, final String measureName) {
		final Optional<TabularDataSerie> tabularDataSerieOpt = tabularDatas.getTabularDataSeries()
				.stream()
				.filter(timedDataSerie -> timedDataSerie.getValues().containsKey("name") && measureName.equals(timedDataSerie.getValues().get("name")))
				.findAny();

		if (tabularDataSerieOpt.isPresent()) {
			final TabularDataSerie tabularDataSerie = tabularDataSerieOpt.get();
			if (tabularDataSerie.getValues().containsKey(serieName)) {
				return (Double) tabularDataSerie.getValues().get(serieName);
			}
		}
		return null;
	}

	private static void buildEntityModel(final Map<String, Object> model, final List<Metric> metrics) {
		final Map<String, Double> entityCounts = new HashMap<>();

		metrics
				.stream()
				.filter(metric -> "entityCount".equals(metric.getName()))
				.forEach(metric -> entityCounts.put(metric.getFeature(), metric.getValue()));

		final Map<String, Double> taskCounts = new HashMap<>();
		metrics
				.stream()
				.filter(metric -> "definitionUsageInDao".equals(metric.getName()))
				.forEach(metric -> taskCounts.put(metric.getFeature(), metric.getValue()));

		final Map<String, Double> fieldCount = new HashMap<>();
		metrics
				.stream()
				.filter(metric -> "definitionFieldCount".equals(metric.getName()))
				.forEach(metric -> fieldCount.put(metric.getFeature(), metric.getValue()));

		final Collection<DtDefinition> dtDefinitions = Node.getNode().getDefinitionSpace().getAll(DtDefinition.class);
		final List<EntityModel> entities = dtDefinitions
				.stream()
				.filter(DtDefinition::isPersistent)
				.map(dtDefinition -> new EntityModel(
						dtDefinition,
						entityCounts.get(dtDefinition.getName()),
						taskCounts.get(dtDefinition.getName()),
						fieldCount.get(dtDefinition.getName())))
				.collect(Collectors.toList());
		model.put("entities", entities);
		//---
		model.put("entityCount", entities.size());
		final long keyConceptCount = dtDefinitions.stream().filter(dtDefinition -> dtDefinition.getStereotype() == DtStereotype.KeyConcept).count();
		model.put("keyConceptCount", keyConceptCount);
	}

	private static void buildDomainModel(final Map<String, Object> model, final List<Metric> metrics) {
		final Map<String, Double> taskCount = new HashMap<>();

		metrics
				.stream()
				.filter(metric -> "domainUsageInTasks".equals(metric.getName()))
				.forEach(metric -> taskCount.put(metric.getFeature(), metric.getValue()));

		final Map<String, Double> dtDefinitionCount = new HashMap<>();
		metrics
				.stream()
				.filter(metric -> "domainUsageInDtDefinitions".equals(metric.getName()))
				.forEach(metric -> dtDefinitionCount.put(metric.getFeature(), metric.getValue()));

		final Collection<SmartTypeDefinition> smartTypes = Node.getNode().getDefinitionSpace().getAll(SmartTypeDefinition.class);
		final List<SmartTypeModel> smartTypeModels = smartTypes
				.stream()
				.filter(smartType -> smartType.getScope().isPrimitive()) // we display only primitives
				.map(smartType -> new SmartTypeModel(
						smartType,
						taskCount.get(smartType.getName()),
						dtDefinitionCount.get(smartType.getName())))
				.collect(Collectors.toList());

		model.put("smartTypes", smartTypeModels);

	}

}
