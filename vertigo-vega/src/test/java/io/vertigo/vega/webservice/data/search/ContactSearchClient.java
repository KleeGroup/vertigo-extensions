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
package io.vertigo.vega.webservice.data.search;

import java.util.List;

import io.vertigo.core.node.component.Component;
import io.vertigo.core.node.definition.DefinitionProvider;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.DefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetRangeDefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetTermDefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetedQueryDefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetDefinition.FacetOrder;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinitionSupplier;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datafactory.search.model.SearchQueryBuilder;

public class ContactSearchClient implements Component, DefinitionProvider {

	/**
	 * Création d'une SearchQuery de type : Base.
	 * @param criteria Critères de recherche
	 * @param selectedFacetValues Liste des facettes sélectionnées à appliquer
	 * @return SearchQueryBuilder pour ce type de recherche
	 */
	public SearchQueryBuilder createSearchQueryBuilderBase(final java.lang.String criteria, final SelectedFacetValues selectedFacetValues) {
		return SearchQuery.builder("QryContactFacet")
				.withCriteria(criteria)
				.withFacet(selectedFacetValues);
	}

	/** {@inheritDoc} */
	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return List.of(
				//---
				// SearchIndexDefinition
				//-----
				new SearchIndexDefinitionSupplier("IdxContact")
						.withKeyConcept("DtContact")
						.withIndexDtDefinition("DtContact")
						.withLoaderId("ContactSearchLoader")
						.withCopyToFields("allText", "conId", "honorificCode", "name", "firstName", "birthday", "email"),
				//---
				// FacetTermDefinition
				//-----
				new FacetTermDefinitionSupplier("FctHonorificCode")
						.withDtDefinition("DtContact")
						.withFieldName("honorificCode")
						.withLabel("Par code honorific")
						.withOrder(FacetOrder.count)
						.withMultiSelectable(),
				new FacetRangeDefinitionSupplier("FctBirthday")
						.withDtDefinition("DtContact")
						.withFieldName("birthday")
						.withLabel("Par date")
						.withRange("r1", "birthday:[* TO 01/01/1980]", "avant 1980")
						.withRange("r2", "birthday:[01/01/1980 TO 01/01/1990]", "1980-1990")
						.withRange("r3", "birthday:[01/01/1990 TO 01/01/2000]", "1990-2000")
						.withRange("r4", "birthday:[01/01/2000 TO 01/01/2010]", "2000-2010")
						.withRange("r5", "birthday:[01/01/2010 TO *]", "apres 2010")
						.withOrder(FacetOrder.definition),
				//---
				// FacetedQueryDefinition
				//-----
				new FacetedQueryDefinitionSupplier("QryContactFacet")
						.withListFilterBuilderClass(io.vertigo.datafactory.impl.search.dsl.DslListFilterBuilder.class)
						.withListFilterBuilderQuery("#criteria#")
						.withCriteriaSmartType("STyTexte50")
						.withFacet("FctHonorificCode")
						.withFacet("FctBirthday"));
	}
}
