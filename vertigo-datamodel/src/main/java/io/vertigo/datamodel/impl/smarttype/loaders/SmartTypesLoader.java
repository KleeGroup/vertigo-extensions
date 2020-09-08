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
package io.vertigo.datamodel.impl.smarttype.loaders;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.impl.smarttype.dynamic.DynamicDefinition;
import io.vertigo.datamodel.smarttype.AdapterConfig;
import io.vertigo.datamodel.smarttype.ConstraintConfig;
import io.vertigo.datamodel.smarttype.FormatterConfig;
import io.vertigo.datamodel.smarttype.annotations.Adapter;
import io.vertigo.datamodel.smarttype.annotations.Constraint;
import io.vertigo.datamodel.smarttype.annotations.Constraints;
import io.vertigo.datamodel.smarttype.annotations.Formatter;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeProperty;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition.Scope;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtProperty;
import io.vertigo.datamodel.structure.definitions.Properties;
import io.vertigo.datamodel.structure.definitions.PropertiesBuilder;
import io.vertigo.datamodel.structure.definitions.Property;
import io.vertigo.datamodel.structure.model.DtObject;

public class SmartTypesLoader implements Loader {

	@Override
	public void load(final String resourcePath, final Map<String, DynamicDefinition> dynamicDefinitions) {
		final Class<? extends Enum> smartTypesDictionnaryClass = (Class<? extends Enum>) ClassUtil.classForName(resourcePath);
		Stream.of(smartTypesDictionnaryClass.getEnumConstants())
				.map(enumConstant -> {
					try {
						final SmartTypeDefinition smartTypeDefinition = readSmartTypeDefinition(smartTypesDictionnaryClass.getField(enumConstant.name()));
						return new DynamicDefinition(smartTypeDefinition.getName(), Collections.emptyList(), ds -> smartTypeDefinition);
					} catch (NoSuchFieldException | SecurityException e) {
						throw WrappedException.wrap(e);
					}
				}).forEach(dynamicDefinition -> {
					Assertion.check().isFalse(dynamicDefinitions.containsKey(dynamicDefinition.getName()), "SmartType with name {0} is declared twice", dynamicDefinition.getName());
					dynamicDefinitions.put(dynamicDefinition.getName(), dynamicDefinition);
				});

	}

	private static SmartTypeDefinition readSmartTypeDefinition(final Field field) {
		final String smartTypeName = "STy" + field.getName();
		final Scope scope;
		final Class targetJavaClass = field.getAnnotation(io.vertigo.datamodel.smarttype.annotations.SmartTypeDefinition.class).value();
		final Optional<BasicType> dataTypeOpt = BasicType.of(targetJavaClass);
		final FormatterConfig formatterConfig;
		final List<ConstraintConfig> constraintConfigs;
		final List<AdapterConfig> adapterConfigs = new ArrayList<>();
		final PropertiesBuilder propertiesBuilder = Properties.builder();

		// DataType and Mapper
		if (dataTypeOpt.isPresent()) {
			//we are a primitive
			scope = Scope.PRIMITIVE;
		} else {
			//we are not primitive, we need a mapper
			final Adapter[] adapters = field.getAnnotationsByType(Adapter.class);
			for (final Adapter adapter : adapters) {
				adapterConfigs.add(new AdapterConfig(adapter.type(), adapter.clazz(), adapter.targetBasicType()));
			}
			if (DtObject.class.isAssignableFrom(targetJavaClass)) {
				scope = Scope.DATA_OBJECT;
				Assertion.check().isTrue(field.getName().equals(DtDefinition.PREFIX + targetJavaClass.getSimpleName()), "The name of the SmartType {0} is not consistent with the class {1}", field.getName(), targetJavaClass);
			} else {
				Assertion.check().isTrue(adapters.length > 0,
						"Your smarttype '{0}' is associated with a value object, you need to specify a mapper to a targeted DataType with the @Adapter annotation", smartTypeName);
				scope = Scope.VALUE_OBJECT;
			}
		}

		// Formatter
		if (field.isAnnotationPresent(Formatter.class)) {
			final Formatter formatter = field.getAnnotation(Formatter.class);
			formatterConfig = new FormatterConfig(formatter.clazz(), formatter.arg());
		} else {
			formatterConfig = null;
		}
		// Constraints
		if (field.isAnnotationPresent(Constraint.class) || field.isAnnotationPresent(Constraints.class)) {
			final Constraint[] constraints = field.getAnnotationsByType(Constraint.class);
			constraintConfigs = Arrays.stream(constraints)
					.map(contraint -> new ConstraintConfig(contraint.clazz(), contraint.arg(), contraint.msg()))
					.collect(Collectors.toList());

			constraintConfigs
					.forEach(constraintConfig -> {
						final Optional<String> msgOpt = StringUtil.isBlank(constraintConfig.getMsg()) ? Optional.empty() : Optional.of(constraintConfig.getMsg());
						final Constructor<? extends io.vertigo.datamodel.structure.definitions.Constraint> constructor = ClassUtil.findConstructor(constraintConfig.getConstraintClass(), new Class[] { String.class, Optional.class });
						final io.vertigo.datamodel.structure.definitions.Constraint newConstraint = ClassUtil.newInstance(constructor, new Object[] { constraintConfig.getArg(), msgOpt });
						propertiesBuilder.addValue(newConstraint.getProperty(), newConstraint.getPropertyValue());
					});

		} else {
			constraintConfigs = Collections.emptyList();
		}

		for (final SmartTypeProperty smartTypeProperty : field.getAnnotationsByType(SmartTypeProperty.class)) {
			propertiesBuilder.addValue((Property<String>) DtProperty.valueOf(StringUtil.camelToConstCase(smartTypeProperty.property())), smartTypeProperty.value());
			//TODO
		}

		return new SmartTypeDefinition(
				smartTypeName,
				scope,
				targetJavaClass.getName(),
				adapterConfigs,
				formatterConfig,
				constraintConfigs,
				propertiesBuilder.build());

	}

	@Override
	public String getType() {
		return "smarttypes";
	}

}
