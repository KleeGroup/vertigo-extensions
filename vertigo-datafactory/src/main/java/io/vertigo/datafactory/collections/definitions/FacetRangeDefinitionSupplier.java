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

import java.util.ArrayList;
import java.util.List;

import io.vertigo.core.locale.MessageText;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.DefinitionSupplier;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.collections.definitions.FacetDefinition.FacetOrder;
import io.vertigo.datafactory.collections.model.FacetValue;
import io.vertigo.datamodel.structure.definitions.DtDefinition;

public final class FacetRangeDefinitionSupplier implements DefinitionSupplier {

	private final String myName;
	private String myDtDefinitionName;
	private String myFieldName;
	private String myLabel;
	private boolean myMultiSelectable;
	private final List<FacetValue> facetValues = new ArrayList<>();
	private FacetOrder myOrder;

	public FacetRangeDefinitionSupplier(final String name) {
		this.myName = name;
	}

	public FacetRangeDefinitionSupplier withDtDefinition(final String dtDefinitionName) {
		this.myDtDefinitionName = dtDefinitionName;
		return this;
	}

	public FacetRangeDefinitionSupplier withFieldName(final String fieldName) {
		this.myFieldName = fieldName;
		return this;
	}

	public FacetRangeDefinitionSupplier withLabel(final String label) {
		this.myLabel = label;
		return this;
	}

	public FacetRangeDefinitionSupplier withMultiSelectable() {
		myMultiSelectable = true;
		return this;
	}

	public FacetRangeDefinitionSupplier withRange(final String code, final String filter, final String label) {
		facetValues.add(new FacetValue(code, ListFilter.of(filter), MessageText.of(label)));
		return this;
	}

	public FacetRangeDefinitionSupplier withOrder(final FacetOrder order) {
		this.myOrder = order;
		return this;
	}

	@Override
	public FacetDefinition get(final DefinitionSpace definitionSpace) {
		final DtDefinition indexDtDefinition = definitionSpace.resolve(myDtDefinitionName, DtDefinition.class);
		return FacetDefinition.createFacetDefinitionByRange(
				myName,
				indexDtDefinition.getField(myFieldName),
				MessageText.of(myLabel),
				facetValues,
				myMultiSelectable,
				myOrder);
	}
}
