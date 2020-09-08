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
package io.vertigo.commons.impl.app;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.commons.app.AppManager;
import io.vertigo.commons.app.AppNode;
import io.vertigo.commons.plugins.app.registry.single.SingleAppNodeRegistryPlugin;
import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.config.ModuleConfig;

/**
 * Implementation of the NodeManager.
 * @author mlaroche
 *
 */
public final class AppManagerImpl implements AppManager, Activeable {

	private static final int HEART_BEAT_SECONDS = 60;

	private final AppNodeRegistryPlugin nodeRegistryPlugin;
	private final Map<String, AppNodeInfosPlugin> nodeInfosPluginMap = new HashMap<>();

	@Inject
	public AppManagerImpl(
			final Optional<AppNodeRegistryPlugin> nodeRegistryPluginOpt,
			final List<AppNodeInfosPlugin> nodeInfosPlugins) {
		Assertion.check().isNotNull(nodeRegistryPluginOpt);
		// ---
		nodeRegistryPlugin = nodeRegistryPluginOpt.orElseGet(SingleAppNodeRegistryPlugin::new);
		nodeInfosPlugins
				.forEach(plugin -> {
					Assertion.check().isFalse(nodeInfosPluginMap.containsKey(plugin.getProtocol()), "A plugin for the protocol {0} is already registered", plugin.getProtocol());
					//---
					nodeInfosPluginMap.put(plugin.getProtocol(), plugin);
				});

	}

	@DaemonScheduled(name = "DmnUpdateNodeStatus", periodInSeconds = HEART_BEAT_SECONDS, analytics = false)
	public void updateNodeStatus() {
		nodeRegistryPlugin.updateStatus(toAppNode(Node.getNode()));
	}

	@Override
	public void start() {
		nodeRegistryPlugin.register(toAppNode(Node.getNode()));
	}

	@Override
	public void stop() {
		nodeRegistryPlugin.unregister(toAppNode(Node.getNode()));

	}

	@Override
	public Optional<AppNode> find(final String nodeId) {
		return nodeRegistryPlugin.find(nodeId);
	}

	@Override
	public List<AppNode> locateSkills(final String... skills) {
		return getTopology()
				.stream()
				.filter(node -> node.getSkills().containsAll(Arrays.asList(skills)))
				.collect(Collectors.toList());
	}

	@Override
	public List<AppNode> getTopology() {
		return nodeRegistryPlugin.getTopology();
	}

	@Override
	public AppNode getCurrentNode() {
		final String currentNodeId = Node.getNode().getNodeConfig().getNodeId();
		return find(currentNodeId)
				.orElseThrow(() -> new VSystemException("Current node with '{0}' cannot be found in the registry", currentNodeId));
	}

	@Override
	public List<AppNode> getDeadNodes() {
		return getTopology()
				.stream()
				// we wait two heartbeat to decide that a node is dead
				.filter(node -> node.getLastTouch().plus(2L * HEART_BEAT_SECONDS, ChronoUnit.SECONDS).isBefore(Instant.now()))
				.collect(Collectors.toList());
	}

	@Override
	public Map<String, List<HealthCheck>> getStatus() {
		return aggregateResults(node -> getInfosPlugin(node).getStatus(node));
	}

	@Override
	public Map<String, Object> getStats() {
		return aggregateResults(node -> getInfosPlugin(node).getStats(node));
	}

	@Override
	public Map<String, String> getConfig() {
		return aggregateResults(node -> getInfosPlugin(node).getConfig(node));
	}

	private <R> Map<String, R> aggregateResults(final Function<AppNode, R> functionToApply) {
		return nodeRegistryPlugin
				.getTopology()
				.stream()
				.collect(Collectors.toMap(
						AppNode::getId,
						functionToApply));

	}

	private AppNodeInfosPlugin getInfosPlugin(final AppNode node) {
		Assertion.check().isTrue(nodeInfosPluginMap.containsKey(node.getProtocol()), "No status plugin found for the protocol {0} when reach attempt on {1} ", node.getProtocol(), node.getEndPoint());
		//---
		return nodeInfosPluginMap.get(node.getProtocol());
	}

	private static AppNode toAppNode(final Node node) {
		return new AppNode(
				node.getNodeConfig().getNodeId(),
				node.getNodeConfig().getAppName(),
				NodeStatus.UP.name(),
				Instant.now(),
				node.getStart(),
				node.getNodeConfig().getEndPoint(),
				getSkills(node));
	}

	private static List<String> getSkills(final Node node) {
		return node.getNodeConfig().getModuleConfigs().stream()
				.map(ModuleConfig::getName)
				.collect(Collectors.toList());
	}

	public enum NodeStatus {
		UP, DOWN

	}

}
