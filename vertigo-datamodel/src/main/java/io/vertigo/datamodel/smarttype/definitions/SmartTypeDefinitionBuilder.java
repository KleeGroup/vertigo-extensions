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
package io.vertigo.datamodel.smarttype.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.core.lang.Builder;
import io.vertigo.datamodel.smarttype.AdapterConfig;
import io.vertigo.datamodel.smarttype.ConstraintConfig;
import io.vertigo.datamodel.smarttype.FormatterConfig;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition.Scope;
import io.vertigo.datamodel.structure.definitions.Properties;

/**
 * This class must be used to build a Domain.
 * @author pchretien
 */
public final class SmartTypeDefinitionBuilder implements Builder<SmartTypeDefinition> {
	private final String myName;
	private SmartTypeDefinition.Scope myScope;

	private final Class myValueObjectClass;
	private final List<AdapterConfig> myAdapterConfigs = new ArrayList<>();

	/** Formatter. */
	private FormatterConfig myformatterConfig;

	/** list of constraints */
	private List<ConstraintConfig> myConstraintConfigs;

	/** List of property-value tuples */
	private Properties myProperties;

	/**
	 * Constructor.
	 * @param name the name of the smartType
	 * @param dataType the dataType of the smartType
	 */
	SmartTypeDefinitionBuilder(final String name, final BasicType dataType) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(dataType);
		//---
		myName = name;
		myScope = SmartTypeDefinition.Scope.PRIMITIVE;

		myValueObjectClass = dataType.getJavaClass();
	}

	/**
	 * Constructor.
	 * @param name the name of the smartType
	 * @param valueObjectClass the value-object class of the smartType
	 */
	SmartTypeDefinitionBuilder(final String name, final Class valueObjectClass) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(valueObjectClass);
		//---
		myName = name;
		myScope = SmartTypeDefinition.Scope.VALUE_OBJECT;
		myValueObjectClass = valueObjectClass;
	}

	/**
	 * @param formatterDefinition the FormatterDefinition
	 * @return this builder
	 */
	public SmartTypeDefinitionBuilder withScope(final Scope scope) {
		Assertion.check().isNotNull(scope);
		//---
		myScope = scope;
		return this;
	}

	/**
	 * @param formatterDefinition the FormatterDefinition
	 * @return this builder
	 */
	public SmartTypeDefinitionBuilder withFormatter(final FormatterConfig formatterConfig) {
		Assertion.check().isNotNull(formatterConfig);
		//---
		myformatterConfig = formatterConfig;
		return this;
	}

	/**
	 * @param constraintDefinitions the list of constraintDefinitions
	 * @return this builder
	 */
	public SmartTypeDefinitionBuilder withConstraints(final List<ConstraintConfig> constraintConfigs) {
		Assertion.check().isNotNull(constraintConfigs);
		//---
		myConstraintConfigs = constraintConfigs;
		return this;
	}

	/**
	* @param properties the properties
	* @return this builder
	*/
	public SmartTypeDefinitionBuilder withProperties(final Properties properties) {
		Assertion.check().isNotNull(properties);
		//---
		myProperties = properties;
		return this;
	}

	/**
	* @param properties the properties
	* @return this builder
	*/
	public SmartTypeDefinitionBuilder addAdapter(final String type, final Class<? extends BasicTypeAdapter> adapterClass, final BasicType targetDataType) {
		Assertion.check()
				.isNotNull(adapterClass)
				.isNotNull(targetDataType);
		//---
		myAdapterConfigs.add(new AdapterConfig(type, adapterClass, targetDataType));
		return this;
	}

	@Override
	public SmartTypeDefinition build() {
		return new SmartTypeDefinition(
				myName,
				myScope,
				myValueObjectClass.getName(),
				myAdapterConfigs,
				myformatterConfig,
				myConstraintConfigs == null ? Collections.emptyList() : myConstraintConfigs,
				myProperties == null ? Properties.builder().build() : myProperties);
	}
}
