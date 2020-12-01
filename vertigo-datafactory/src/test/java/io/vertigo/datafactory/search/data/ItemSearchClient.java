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
package io.vertigo.datafactory.search.data;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.node.definition.DefinitionProvider;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.DefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetCustomDefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetDefinition.FacetOrder;
import io.vertigo.datafactory.collections.definitions.FacetRangeDefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetTermDefinitionSupplier;
import io.vertigo.datafactory.collections.definitions.FacetedQueryDefinitionSupplier;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datafactory.search.SearchManager;
import io.vertigo.datafactory.search.data.domain.Item;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinitionSupplier;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datafactory.search.model.SearchQueryBuilder;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.UID;

public class ItemSearchClient implements Component, DefinitionProvider {

	private final SearchManager searchManager;
	private final VTransactionManager transactionManager;

	/**
	 * Contructeur.
	 * @param searchManager Search Manager
	 * @param transactionManager Transaction Manager
	 */
	@Inject
	public ItemSearchClient(
			final SearchManager searchManager,
			final VTransactionManager transactionManager) {
		this.searchManager = searchManager;
		this.transactionManager = transactionManager;
	}

	public SearchQueryBuilder createSearchQueryBuilderItemFacet(final String criteria, final SelectedFacetValues selectedFacetValues) {
		return SearchQuery.builder("QryItemFacet")
				.withCriteria(criteria)
				.withFacet(selectedFacetValues);
	}

	public SearchQueryBuilder createSearchQueryBuilderItemOptionalFacet(final java.lang.String criteria, final SelectedFacetValues selectedFacetValues) {
		return SearchQuery.builder("QryItemOptionalFacet")
				.withCriteria(criteria)
				.withFacet(selectedFacetValues);
	}

	public SearchQueryBuilder createSearchQueryBuilderItemFacetMulti(final String criteria, final SelectedFacetValues selectedFacetValues) {
		return SearchQuery.builder("QryItemFacetMulti")
				.withCriteria(criteria)
				.withFacet(selectedFacetValues);
	}

	public SearchQueryBuilder createSearchQueryBuilderItemFacetGeo(final Item criteria, final SelectedFacetValues selectedFacetValues) {
		return SearchQuery.builder("QryItemFacetGeo")
				.withCriteria(criteria)
				.withFacet(selectedFacetValues);

	}

	/**
	 * Récupération du résultat issu d'une requête.
	 * @param searchQuery critères initiaux
	 * @param listState Etat de la liste (tri et pagination)
	 * @return Résultat correspondant à la requête (de type BaseIndex)
	 */
	public FacetedQueryResult<Item, SearchQuery> loadList(final SearchQuery searchQuery, final DtListState listState) {
		final SearchIndexDefinition indexDefinition = searchManager.findFirstIndexDefinitionByKeyConcept(Item.class);
		return searchManager.loadList(indexDefinition, searchQuery, listState);
	}

	/**
	 * Mark an entity as dirty. Index of these elements will be reindexed if Tx commited.
	 * Reindexation isn't synchrone, strategy is dependant of plugin's parameters.
	 *
	 * @param entityUID Key concept's UID
	 */
	public void markAsDirty(final UID<Item> entityUID) {
		transactionManager.getCurrentTransaction().addAfterCompletion((final boolean txCommitted) -> {
			if (txCommitted) {// reindex only is tx successful
				searchManager.markAsDirty(Arrays.asList(entityUID));
			}
		});
	}

	/**
	 * Mark an entity as dirty. Index of these elements will be reindexed if Tx commited.
	 * Reindexation isn't synchrone, strategy is dependant of plugin's parameters.
	 *
	 * @param entity Key concept
	 */
	public void markAsDirty(final Item entity) {
		markAsDirty(UID.of(entity));
	}

	/** {@inheritDoc} */
	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return List.of(
				//---
				// SearchIndexDefinition
				//-----
				new SearchIndexDefinitionSupplier("IdxItem")
						.withKeyConcept("DtItem")
						.withIndexDtDefinition("DtItem")
						.withLoaderId("ItemSearchLoader")
						.withCopyToFields("allText", "manufacturer", "model", "description", "year", "kilo", "price", "motorType"),
				//---
				// FacetTermDefinition
				//-----
				new FacetTermDefinitionSupplier("FctDescriptionItem")
						.withDtDefinition("DtItem")
						.withFieldName("description")
						.withLabel("Description")
						.withOrder(FacetOrder.count),
				new FacetTermDefinitionSupplier("FctOptionalStringItem")
						.withDtDefinition("DtItem")
						.withFieldName("optionalString")
						.withLabel("optionalString")
						.withOrder(FacetOrder.count),
				new FacetTermDefinitionSupplier("FctManufacturerItem")
						.withDtDefinition("DtItem")
						.withFieldName("manufacturer")
						.withLabel("Par constructeur")
						.withOrder(FacetOrder.count),
				new FacetTermDefinitionSupplier("FctManufacturerItemAlpha")
						.withDtDefinition("DtItem")
						.withFieldName("manufacturer")
						.withLabel("Par constructeur")
						.withOrder(FacetOrder.alpha),
				new FacetTermDefinitionSupplier("FctManufacturerItemMulti")
						.withDtDefinition("DtItem")
						.withFieldName("manufacturer")
						.withLabel("Par constructeur")
						.withMultiSelectable()
						.withOrder(FacetOrder.alpha),
				new FacetRangeDefinitionSupplier("FctYearItem")
						.withDtDefinition("DtItem")
						.withFieldName("year")
						.withLabel("Par date")
						.withRange("R1", "year:[* TO 2000]", "avant 2000")
						.withRange("R2", "year:[2000 TO 2005]", "2000-2005")
						.withRange("R3", "year:[2005 TO *]", "apres 2005")
						.withOrder(FacetOrder.definition),
				new FacetRangeDefinitionSupplier("FctLocalisationItem")
						.withDtDefinition("DtItem")
						.withLabel("Par distance")
						.withFieldName("localisation") //fieldname in index
						//.withOrigin("#loc1#")
						.withRange("R1", "localisation:#localisation#~5km", "< 5km") //#localisation# ref a field of criteria not index
						.withRange("R2", "localisation:#localisation#~7km", "< 7km")
						.withRange("R3", "localisation:#localisation#~8500m", "< 8.5km")
						.withRange("R4", "localisation:#localisation#~10km", "< 10km")
						.withRange("R5", "localisation:#localisation#~20km", "< 20km")
						.withOrder(FacetOrder.definition),
				new FacetRangeDefinitionSupplier("FctLocalisationCircleItem")
						.withDtDefinition("DtItem")
						.withLabel("Par distance")
						.withFieldName("localisation") //fieldname in index
						.withRange("R1", "localisation:[#localisation#~0km to #localisation#~5km]", "< 5km") //#localisation# ref a field of criteria not index
						.withRange("R2", "localisation:[#localisation#~5km to #localisation#~7km]", "5 à 7km")
						.withRange("R3", "localisation:[#localisation#~7km to #localisation#~8500m]", "7 à 8.5km")
						.withRange("R4", "localisation:[#localisation#~8500m to #localisation#~10km]", "8.5 à 10km")
						.withRange("R5", "localisation:[#localisation#~10km to #localisation#~20km]", "10 à 20km")
						.withRange("R6", "localisation:[#localisation#~20km to #localisation#~0km]", "> 20km")
						.withOrder(FacetOrder.definition),
				new FacetCustomDefinitionSupplier("FctLocalisationHashItem")
						.withDtDefinition("DtItem")
						.withLabel("Par geohash")
						.withFieldName("localisation") //fieldname in index
						.withParams("geohash_grid", "{\"field\" : \"localisation\",\"precision\" : #precision# }")
						.withParams("innerWriteTo", "writeVInt(#precision#);writeVInt(1000);writeVInt(-1);writeGeoPoint();writeGeoPoint();") //same as GeoGridAggregationBuilder.innerWriteTo
						.withOrder(FacetOrder.count),
				//---
				// FacetedQueryDefinition
				//-----
				new FacetedQueryDefinitionSupplier("QryItemFacet")
						.withListFilterBuilderClass(io.vertigo.datafactory.impl.search.dsl.DslListFilterBuilder.class)
						.withListFilterBuilderQuery("description:#query# manufacturer:#query#")
						.withCriteriaSmartType("STyString")
						.withFacet("FctDescriptionItem")
						.withFacet("FctManufacturerItem")
						.withFacet("FctManufacturerItemAlpha")
						.withFacet("FctYearItem"),
				new FacetedQueryDefinitionSupplier("QryItemOptionalFacet")
						.withListFilterBuilderClass(io.vertigo.datafactory.impl.search.dsl.DslListFilterBuilder.class)
						.withListFilterBuilderQuery("description:#query# manufacturer:#query#")
						.withCriteriaSmartType("STyString")
						.withFacet("FctOptionalStringItem"),
				new FacetedQueryDefinitionSupplier("QryItemFacetMulti")
						.withListFilterBuilderClass(io.vertigo.datafactory.impl.search.dsl.DslListFilterBuilder.class)
						.withListFilterBuilderQuery("description:#query# manufacturer:#query#")
						.withCriteriaSmartType("STyString")
						.withFacet("FctDescriptionItem")
						.withFacet("FctManufacturerItemMulti")
						.withFacet("FctYearItem"),
				new FacetedQueryDefinitionSupplier("QryItemFacetGeo")
						.withListFilterBuilderClass(io.vertigo.datafactory.impl.search.dsl.DslListFilterBuilder.class)
						.withListFilterBuilderQuery("description:#+description*#")
						.withGeoSearchQuery("localisation:#localisation#~50km") // distance
						.withCriteriaSmartType("STyDtItem")
						.withFacet("FctLocalisationItem")
						.withFacet("FctLocalisationCircleItem"),
				new FacetedQueryDefinitionSupplier("QryItemFacetGeo2")
						.withListFilterBuilderClass(io.vertigo.datafactory.impl.search.dsl.DslListFilterBuilder.class)
						.withListFilterBuilderQuery("description:#+description*#")
						.withGeoSearchQuery("localisation:[#localisation#~5km to #localisation#~50km]") // distance circle
						.withCriteriaSmartType("STyDtItem")
						.withFacet("FctLocalisationCircleItem")
						.withFacet("FctLocalisationHashItem"));
	}

}
