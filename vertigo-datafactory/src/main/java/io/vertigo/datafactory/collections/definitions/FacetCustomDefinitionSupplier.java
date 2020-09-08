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
package io.vertigo.datafactory.collections.definitions;

import java.util.HashMap;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.locale.MessageText;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.DefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetDefinition.FacetOrder;
import io.vertigo.datamodel.structure.definitions.DtDefinition;

public final class FacetCustomDefinitionSupplier implements DefinitionSupplier {

	private final String myName;
	private String myDtDefinitionName;
	private String myFieldName;
	private String myLabel;
	private final Map<String, String> myParams = new HashMap<>();
	private FacetOrder myOrder;

	public FacetCustomDefinitionSupplier(final String name) {
		myName = name;
	}

	public FacetCustomDefinitionSupplier withDtDefinition(final String dtDefinitionName) {
		myDtDefinitionName = dtDefinitionName;
		return this;
	}

	public FacetCustomDefinitionSupplier withFieldName(final String fieldName) {
		myFieldName = fieldName;
		return this;
	}

	public FacetCustomDefinitionSupplier withLabel(final String label) {
		myLabel = label;
		return this;
	}

	public FacetCustomDefinitionSupplier withParams(final String name, final String value) {
		Assertion.check()
				.isNotBlank(name)
				.isNotBlank(value);
		//----
		myParams.put(name, value);
		return this;
	}

	public FacetCustomDefinitionSupplier withOrder(final FacetOrder order) {
		myOrder = order;
		return this;
	}

	@Override
	public FacetDefinition get(final DefinitionSpace definitionSpace) {
		Assertion.check().isFalse(myParams.isEmpty(), "At least one params is mandatory for customFacet (in {0})", myName);
		final DtDefinition indexDtDefinition = definitionSpace.resolve(myDtDefinitionName, DtDefinition.class);
		return FacetDefinition.createCustomFacetDefinition(
				myName,
				indexDtDefinition.getField(myFieldName),
				MessageText.of(myLabel),
				myParams,
				myOrder);
	}
}
