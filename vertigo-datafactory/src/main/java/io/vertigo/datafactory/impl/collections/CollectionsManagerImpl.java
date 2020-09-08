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
package io.vertigo.datafactory.impl.collections;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datafactory.collections.CollectionsManager;
import io.vertigo.datafactory.collections.IndexDtListFunctionBuilder;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.collections.definitions.FacetDefinition;
import io.vertigo.datafactory.collections.model.Facet;
import io.vertigo.datafactory.collections.model.FacetValue;
import io.vertigo.datafactory.collections.model.FacetedQuery;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datafactory.impl.collections.facet.model.FacetFactory;
import io.vertigo.datafactory.impl.collections.functions.filter.DtListPatternFilter;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.util.VCollectors;

/**
 * Implémentation du gestionnaire de la manipulation des collections.
 *
 * @author  pchretien
 */
public final class CollectionsManagerImpl implements CollectionsManager {
	private final Optional<IndexPlugin> indexPluginOpt;

	private final FacetFactory facetFactory;

	/**
	 * Constructor.
	 * @param indexPluginOpt Plugin optionnel d'index
	 */
	@Inject
	public CollectionsManagerImpl(
			final SmartTypeManager smartTypeManager,
			final Optional<IndexPlugin> indexPluginOpt) {
		Assertion.check().isNotNull(indexPluginOpt);
		//-----
		this.indexPluginOpt = indexPluginOpt;
		facetFactory = new FacetFactory(this, smartTypeManager);
	}

	/** {@inheritDoc} */
	@Override
	public <R extends DtObject> FacetedQueryResult<R, DtList<R>> facetList(final DtList<R> dtList, final FacetedQuery facetedQuery, final Optional<FacetDefinition> clusterFacetDefinition) {
		Assertion.check()
				.isNotNull(dtList)
				.isNotNull(facetedQuery);
		//-----
		//1- on applique les filtres
		final DtList<R> resultDtList;
		final DtList<R> filteredDtList = dtList.stream()
				.filter(filter(facetedQuery))
				.collect(VCollectors.toDtList(dtList.getDefinition()));

		//2- on facette
		final List<Facet> facets = facetFactory.createFacets(facetedQuery.getDefinition(), filteredDtList);

		//2a- cluster definition
		//2b- cluster result
		final Map<FacetValue, DtList<R>> resultCluster;
		if (clusterFacetDefinition.isPresent()) {
			resultCluster = facetFactory.createCluster(clusterFacetDefinition.get(), filteredDtList);
			resultDtList = new DtList<>(dtList.getDefinition());
		} else {
			resultCluster = Collections.emptyMap();
			resultDtList = filteredDtList;
		}

		//TODO 2c- mise en valeur vide
		final Map<R, Map<DtField, String>> highlights = Collections.emptyMap();

		//3- on construit le résultat
		return new FacetedQueryResult<>(
				Optional.of(facetedQuery),
				filteredDtList.size(),
				resultDtList, //empty if clustering
				facets,
				clusterFacetDefinition,
				resultCluster,
				highlights,
				dtList);
	}

	//=========================================================================
	//=======================Filtrage==========================================
	//=========================================================================
	private <D extends DtObject> Predicate<D> filter(final FacetedQuery facetedQuery) {
		final SelectedFacetValues selectedFacetValues = facetedQuery.getSelectedFacetValues();
		Predicate<D> predicate = list -> true;
		for (final FacetDefinition facetDefinition : facetedQuery.getDefinition().getFacetDefinitions()) {
			if (!selectedFacetValues.getFacetValues(facetDefinition.getName()).isEmpty()) {
				Predicate<D> predicateValue = list -> false;
				for (final FacetValue facetValue : selectedFacetValues.getFacetValues(facetDefinition.getName())) {
					predicateValue = predicateValue.or(this.filter(facetValue.getListFilter()));
				}
				predicate = predicate.and(predicateValue);
			}
		}
		return predicate;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> IndexDtListFunctionBuilder<D> createIndexDtListFunctionBuilder() {
		Assertion.check().isTrue(indexPluginOpt.isPresent(), "An IndexPlugin is required to use this function");
		//-----
		return new IndexDtListFunctionBuilderImpl<>(indexPluginOpt.get());
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> Predicate<D> filter(final ListFilter listFilter) {
		return new DtListPatternFilter<>(listFilter.getFilterValue());
	}
}
