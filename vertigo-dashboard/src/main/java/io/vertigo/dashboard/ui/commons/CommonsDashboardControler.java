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
package io.vertigo.dashboard.ui.commons;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.commons.eventbus.definitions.EventBusSubscriptionDefinition;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.health.HealthStatus;
import io.vertigo.core.daemon.DaemonManager;
import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.node.Node;
import io.vertigo.dashboard.ui.AbstractDashboardModuleControler;
import io.vertigo.dashboard.ui.commons.model.CacheModel;
import io.vertigo.dashboard.ui.commons.model.DaemonModel;
import io.vertigo.dashboard.ui.commons.model.EventBusModel;
import io.vertigo.datastore.cache.definitions.CacheDefinition;

public class CommonsDashboardControler extends AbstractDashboardModuleControler {

	@Override
	public void doBuildModel(final Node node, final Map<String, Object> model) {
		buildDaemonsModel(node, model);
		buildEventBusModel(node, model);
		buildCacheModel(node, model);
	}

	private static void buildDaemonsModel(final Node node, final Map<String, Object> model) {
		final DaemonManager daemonManager = node.getComponentSpace().resolve(DaemonManager.class);
		final List<DaemonStat> daemonStats = daemonManager.getStats();
		//---
		final List<DaemonModel> daemonModels = Node.getNode().getDefinitionSpace().getAll(DaemonDefinition.class)
				.stream()
				.map(daemonDefinition -> new DaemonModel(
						daemonDefinition,
						daemonStats
								.stream()
								.filter(stat -> daemonDefinition.getName().equals(stat.getDaemonName()))
								.findFirst()
								.get()))
				.collect(Collectors.toList());
		model.put("daemons", daemonModels);
		//---
		model.put("daemonsStatus", getHealthStatus(node, model, "daemons"));

	}

	private static void buildEventBusModel(final Node node, final Map<String, Object> model) {
		final Collection<EventBusSubscriptionDefinition> eventBusSubscriptions = Node.getNode().getDefinitionSpace().getAll(EventBusSubscriptionDefinition.class);
		final List<EventBusModel> events = eventBusSubscriptions
				.stream()
				.collect(Collectors.groupingBy(EventBusSubscriptionDefinition::getEventType))
				.entrySet()
				.stream()
				.map(entry -> new EventBusModel(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		model.put("events", events);
		//---
		model.put("eventSubcriptionsCount", eventBusSubscriptions.size());
		model.put("eventsStatus", getHealthStatus(node, model, "events"));
	}

	private static void buildCacheModel(final Node node, final Map<String, Object> model) {
		final List<CacheModel> caches = Node.getNode().getDefinitionSpace().getAll(CacheDefinition.class)
				.stream()
				.map(CacheModel::new)
				.collect(Collectors.toList());
		model.put("caches", caches);
		model.put("cacheStatus", getHealthStatus(node, model, "cache"));
	}

	private static HealthStatus getHealthStatus(final Node node, final Map<String, Object> model, final String... topics) {
		final AnalyticsManager analyticsManager = node.getComponentSpace().resolve(AnalyticsManager.class);
		final Map<String, List<HealthCheck>> healthCheckByTopic = (Map<String, List<HealthCheck>>) model.get("healthchecksByFeature");
		final List<String> myTopics = Arrays.asList(topics);
		final List<HealthCheck> healtChecksToAggregate = healthCheckByTopic
				.entrySet()
				.stream()
				.filter(entry -> myTopics.contains(entry.getKey()))
				.flatMap(entry -> entry.getValue().stream())
				.collect(Collectors.toList());

		return analyticsManager.aggregate(healtChecksToAggregate);
	}

}
