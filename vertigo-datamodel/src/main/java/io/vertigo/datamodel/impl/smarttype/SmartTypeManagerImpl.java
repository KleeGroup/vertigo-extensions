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

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.core.lang.Tuple;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.smarttype.AdapterConfig;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.Constraint;
import io.vertigo.datamodel.structure.definitions.ConstraintException;
import io.vertigo.datamodel.structure.definitions.Formatter;
import io.vertigo.datamodel.structure.definitions.FormatterException;

public class SmartTypeManagerImpl implements SmartTypeManager, Activeable {

	private Map<String, Formatter> formatterBySmartType;
	private Map<String, List<Constraint>> constraintsBySmartType;
	private final Map<String, Map<Class, BasicTypeAdapter>> adaptersByType = new HashMap<>();
	private final Map<Class, BasicTypeAdapter> wildcardAdapters = new HashMap<>();

	@Override
	public void start() {
		formatterBySmartType = Node.getNode().getDefinitionSpace().getAll(SmartTypeDefinition.class)
				.stream()
				.filter(smartTypeDefinition -> smartTypeDefinition.getFormatterConfig() != null)
				.collect(Collectors.toMap(SmartTypeDefinition::getName, SmartTypeManagerImpl::createFormatter));

		constraintsBySmartType = Node.getNode().getDefinitionSpace().getAll(SmartTypeDefinition.class)
				.stream()
				.collect(Collectors.toMap(SmartTypeDefinition::getName, SmartTypeManagerImpl::createConstraints));

		Node.getNode().getDefinitionSpace().getAll(SmartTypeDefinition.class)
				.stream()
				.filter(smartTypeDefinition -> smartTypeDefinition.getAdapterConfigs().containsKey("*"))
				.forEach(smartTypeDefinition -> {
					final AdapterConfig wildcardAdapterConfig = smartTypeDefinition.getAdapterConfigs().get("*");
					Assertion.check()
							.when(wildcardAdapters.containsKey(smartTypeDefinition.getJavaClass()), () -> Assertion.check()
									.isTrue(wildcardAdapterConfig.getAdapterClass().equals(wildcardAdapters.get(smartTypeDefinition.getJavaClass()).getClass()),
											"SmartType {0} defines an adapter for the class {1} and the type {2}. An adapter for the same type and class is already registered", smartTypeDefinition.getName(), smartTypeDefinition.getJavaClass(), wildcardAdapterConfig.getType()));
					//--
					wildcardAdapters.put(smartTypeDefinition.getJavaClass(), createBasicTypeAdapter(wildcardAdapterConfig));
				});

		Node.getNode().getDefinitionSpace().getAll(SmartTypeDefinition.class)
				.stream()
				.flatMap(smartTypeDefinition -> smartTypeDefinition.getAdapterConfigs().values().stream().map(adapterConfig -> Tuple.of(smartTypeDefinition, adapterConfig)))
				.filter(tuple -> !"*".equals(tuple.getVal2().getType()))
				.forEach(tuple -> {
					final Map<Class, BasicTypeAdapter> registeredAdapaters = adaptersByType.computeIfAbsent(tuple.getVal2().getType(), k -> new HashMap<>());
					Assertion.check()
							.when(registeredAdapaters.containsKey(tuple.getVal1()), () -> Assertion.check()
									.isTrue(tuple.getVal2().getAdapterClass().equals(registeredAdapaters.get(tuple.getVal1()).getClass()),
											"SmartType {0} defines an adapter for the class {1} and the type {2}. An adapter for the same type and class is already registered", tuple.getVal1().getName(), tuple.getVal1(), tuple.getVal2().getType()));
					registeredAdapaters.put(tuple.getVal1().getJavaClass(), createBasicTypeAdapter(tuple.getVal2()));
				});

	}

	private static Formatter createFormatter(final SmartTypeDefinition smartTypeDefinition) {
		final Constructor<? extends Formatter> constructor = ClassUtil.findConstructor(smartTypeDefinition.getFormatterConfig().getFormatterClass(), new Class[] { String.class });
		return ClassUtil.newInstance(constructor, new Object[] { smartTypeDefinition.getFormatterConfig().getArg() });
	}

	private static List<Constraint> createConstraints(final SmartTypeDefinition smartTypeDefinition) {
		return smartTypeDefinition.getConstraintConfigs()
				.stream()
				.map(constraintConfig -> {
					final Optional<String> msgOpt = StringUtil.isBlank(constraintConfig.getMsg()) ? Optional.empty() : Optional.of(constraintConfig.getMsg());
					final Constructor<? extends Constraint> constructor = ClassUtil.findConstructor(constraintConfig.getConstraintClass(), new Class[] { String.class, Optional.class });
					return ClassUtil.newInstance(constructor, new Object[] { constraintConfig.getArg(), msgOpt });
				})
				.collect(Collectors.toList());
	}

	private static BasicTypeAdapter createBasicTypeAdapter(final AdapterConfig adapterConfig) {
		return ClassUtil.newInstance(adapterConfig.getAdapterClass());
	}

	@Override
	public void stop() {
		// nothing
	}

	@Override
	public void checkValue(final SmartTypeDefinition smartTypeDefinition, final Object value) {
		if (smartTypeDefinition.getScope().isPrimitive()) {
			smartTypeDefinition.getBasicType().checkValue(value);
		}

	}

	@Override
	public void checkConstraints(final SmartTypeDefinition smartTypeDefinition, final Object value) throws ConstraintException {
		checkValue(smartTypeDefinition, value);
		//---
		if (constraintsBySmartType.containsKey(smartTypeDefinition.getName())) {
			for (final Constraint constraint : constraintsBySmartType.get(smartTypeDefinition.getName())) {
				//when a constraint fails, there is no validation
				if (!constraint.checkConstraint(value)) {
					throw new ConstraintException(constraint.getErrorMessage());
				}
			}
		}

	}

	@Override
	public String valueToString(final SmartTypeDefinition smartTypeDefinition, final Object objValue) {
		return formatterBySmartType.get(smartTypeDefinition.getName()).valueToString(objValue, smartTypeDefinition.getBasicType());
	}

	@Override
	public Object stringToValue(final SmartTypeDefinition smartTypeDefinition, final String strValue) throws FormatterException {
		return formatterBySmartType.get(smartTypeDefinition.getName()).stringToValue(strValue, smartTypeDefinition.getBasicType());
	}

	@Override
	public Map<Class, BasicTypeAdapter> getTypeAdapters(final String adapterType) {
		return new MapBuilder<Class, BasicTypeAdapter>()
				.putAll(wildcardAdapters)// we start with basic ones
				.putAll(adaptersByType.getOrDefault(adapterType, Collections.emptyMap())) // we add specialized ones
				.unmodifiable() // we dont want modifs
				.build();
	}

}
