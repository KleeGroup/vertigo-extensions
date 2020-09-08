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
package io.vertigo.datamodel.impl.structure.metrics;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.metric.Metrics;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Component;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datamodel.task.definitions.TaskAttribute;
import io.vertigo.datamodel.task.definitions.TaskDefinition;

/**
 * Composant to provide Metrics about domain
 *
 * @author pchretien
 */
public final class StructureMetricsProvider implements Component {

	@Metrics
	public List<Metric> getFieldMetrics() {
		return Node.getNode().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.map(dtDefinition -> {
					return Metric.builder()
							.withSuccess()
							.withName("definitionFieldCount")
							.withFeature(dtDefinition.getName())
							.withValue(Double.valueOf(dtDefinition.getFields().size()))
							.build();
				})
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getDependencyMetrics() {
		return Node.getNode().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.map(dtDefinition -> Metric.builder()
						.withSuccess()
						.withName("definitionUsageInDao")
						.withFeature(dtDefinition.getName())
						.withValue(countTaskDependencies(dtDefinition))
						.build())
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getSmartTypeUsageTasksMetrics() {
		return Node.getNode().getDefinitionSpace().getAll(SmartTypeDefinition.class)
				.stream()
				.map(smartType -> Metric.builder()
						.withSuccess()
						.withName("smartTypeUsageInTasks")
						.withFeature(smartType.getName())
						.withValue(countTaskDependencies(smartType))
						.build())
				.collect(Collectors.toList());

	}

	@Metrics
	public List<Metric> getSmartTypeUsageDtDefinitionMetrics() {
		return Node.getNode().getDefinitionSpace().getAll(SmartTypeDefinition.class)
				.stream()
				.map(smartType -> Metric.builder()
						.withSuccess()
						.withName("smartTypeUsageInDtDefinitions")
						.withFeature(smartType.getName())
						.withValue(countDtDefinitionDependencies(smartType))
						.build())
				.collect(Collectors.toList());

	}

	private static double countTaskDependencies(final SmartTypeDefinition smartTypeDefinition) {
		Assertion.check().isNotNull(smartTypeDefinition);
		//---
		int count = 0;
		for (final TaskDefinition taskDefinition : Node.getNode().getDefinitionSpace().getAll(TaskDefinition.class)) {
			for (final TaskAttribute taskAttribute : taskDefinition.getInAttributes()) {
				if (smartTypeDefinition.equals(taskAttribute.getSmartTypeDefinition())) {
					count++;
				}
			}
			if (taskDefinition.getOutAttributeOption().isPresent()) {
				if (smartTypeDefinition.equals(taskDefinition.getOutAttributeOption().get().getSmartTypeDefinition())) {
					count++;
				}
			}
		}
		return count;
	}

	private static double countDtDefinitionDependencies(final SmartTypeDefinition smartTypeDefinition) {
		Assertion.check().isNotNull(smartTypeDefinition);
		//---
		return Node.getNode().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.flatMap(dtDefinition -> dtDefinition.getFields().stream())
				.filter(field -> smartTypeDefinition.equals(field.getSmartTypeDefinition()))
				.count();
	}

	private static double countTaskDependencies(final DtDefinition dtDefinition) {
		int count = 0;
		for (final TaskDefinition taskDefinition : Node.getNode().getDefinitionSpace().getAll(TaskDefinition.class)) {
			for (final TaskAttribute taskAttribute : taskDefinition.getInAttributes()) {
				count += count(dtDefinition, taskAttribute);
			}
			if (taskDefinition.getOutAttributeOption().isPresent()) {
				final TaskAttribute taskAttribute = taskDefinition.getOutAttributeOption().get();
				count += count(dtDefinition, taskAttribute);
			}
		}
		return count;
	}

	private static double count(final DtDefinition dtDefinition, final TaskAttribute taskAttribute) {
		if (taskAttribute.getSmartTypeDefinition().getScope().isDataObject()) {
			if (dtDefinition.equals(DtObjectUtil.findDtDefinition(taskAttribute.getSmartTypeDefinition().getJavaClass()))) {
				return 1;
			}
		}
		return 0;
	}

}
