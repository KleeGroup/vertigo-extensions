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

import io.vertigo.core.locale.MessageText;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.DefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetDefinition.FacetOrder;
import io.vertigo.datamodel.structure.definitions.DtDefinition;

public final class FacetTermDefinitionSupplier implements DefinitionSupplier {

	private final String myName;
	private String myDtDefinitionName;
	private String myFieldName;
	private String myLabel;
	private boolean myMultiSelectable;
	private FacetOrder myOrder;

	public FacetTermDefinitionSupplier(final String name) {
		this.myName = name;
	}

	public FacetTermDefinitionSupplier withDtDefinition(final String dtDefinitionName) {
		this.myDtDefinitionName = dtDefinitionName;
		return this;
	}

	public FacetTermDefinitionSupplier withFieldName(final String fieldName) {
		this.myFieldName = fieldName;
		return this;
	}

	public FacetTermDefinitionSupplier withLabel(final String label) {
		this.myLabel = label;
		return this;
	}

	public FacetTermDefinitionSupplier withMultiSelectable() {
		myMultiSelectable = true;
		return this;
	}

	public FacetTermDefinitionSupplier withOrder(final FacetOrder order) {
		this.myOrder = order;
		return this;
	}

	@Override
	public FacetDefinition get(final DefinitionSpace definitionSpace) {
		final DtDefinition indexDtDefinition = definitionSpace.resolve(myDtDefinitionName, DtDefinition.class);
		return FacetDefinition.createFacetDefinitionByTerm(
				myName,
				indexDtDefinition.getField(myFieldName),
				MessageText.of(myLabel),
				myMultiSelectable,
				myOrder);
	}
}
