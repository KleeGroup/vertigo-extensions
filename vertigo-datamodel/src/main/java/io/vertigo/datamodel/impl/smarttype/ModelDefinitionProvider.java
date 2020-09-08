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
package io.vertigo.datamodel.impl.smarttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.config.DefinitionResourceConfig;
import io.vertigo.core.node.definition.DefinitionProvider;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.DefinitionSupplier;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.datamodel.impl.smarttype.dynamic.DynamicDefinition;
import io.vertigo.datamodel.impl.smarttype.dynamic.DynamicDefinitionSolver;
import io.vertigo.datamodel.impl.smarttype.loaders.DtObjectsLoader;
import io.vertigo.datamodel.impl.smarttype.loaders.Loader;
import io.vertigo.datamodel.impl.smarttype.loaders.SmartTypesLoader;

public class ModelDefinitionProvider implements DefinitionProvider {

	private final Map<String, Loader> loadersByType;
	private final List<DefinitionResourceConfig> definitionResourceConfigs = new ArrayList<>();

	/**
	 * Constructeur injectable.
	 * @param resourceManager the component for finding resources
	 */
	@Inject
	public ModelDefinitionProvider(final ResourceManager resourceManager) {
		loadersByType = Map.of(
				"smarttypes", new SmartTypesLoader(),
				"dtobjects", new DtObjectsLoader());
	}

	@Override
	public void addDefinitionResourceConfig(final DefinitionResourceConfig definitionResourceConfig) {
		Assertion.check().isNotNull(definitionResourceConfig);
		//
		definitionResourceConfigs.add(definitionResourceConfig);
	}

	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		final Map<String, DynamicDefinition> dynamicDefinitions = new HashMap<>();

		final Loader smartTypesLoader = loadersByType.get("smarttypes");
		definitionResourceConfigs
				.stream()
				.filter(resourceConfig -> "smarttypes".equals(resourceConfig.getType()))
				.forEach(resourceConfig -> smartTypesLoader.load(resourceConfig.getPath(), dynamicDefinitions));

		final Loader dtobjectsLoader = loadersByType.get("dtobjects");
		definitionResourceConfigs
				.stream()
				.filter(resourceConfig -> "dtobjects".equals(resourceConfig.getType()))
				.forEach(resourceConfig -> dtobjectsLoader.load(resourceConfig.getPath(), dynamicDefinitions));

		return DynamicDefinitionSolver.solve(definitionSpace, dynamicDefinitions)
				.stream()
				.map(DynamicDefinition::getDefinitionSupplier)
				.collect(Collectors.toList());
	}

}
