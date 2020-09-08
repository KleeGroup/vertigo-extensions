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
package io.vertigo.datafactory.search.model;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.DefinitionReference;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.collections.definitions.FacetDefinition;
import io.vertigo.datafactory.collections.definitions.ListFilterBuilder;
import io.vertigo.datafactory.collections.model.FacetedQuery;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoExpression;
import io.vertigo.datafactory.impl.search.dsl.model.DslMultiExpression;
import io.vertigo.datafactory.impl.search.dsl.rules.DslParserUtil;
import io.vertigo.datamodel.structure.definitions.DtField;

/**
 * Critères de recherche.
 * @author npiedeloup
 */
public final class SearchQuery implements Serializable {
	private static final long serialVersionUID = -3215786603726103410L;

	private final Object queryCriteria;
	private final ListFilter queryListFilter;
	private final Optional<DslGeoExpression> dslGeoExpression;
	private final Optional<ListFilter> securityListFilter;

	//Informations optionnelles pour booster la pertinence des documents plus récent (null si inutilisé)
	private final String boostedDocumentDateFieldName;
	private final Integer numDaysOfBoostRefDocument;
	private final Integer mostRecentBoost;
	private final Optional<FacetedQuery> facetedQuery;
	private final DefinitionReference<FacetDefinition> clusteringFacetDefinitionRef;

	/**
	 * Constructor.
	 * @param facetedQuery facetedQuery
	 * @param facetedQueryDefinition Definition Filtre principal correspondant aux critères de la recherche
	 * @param queryCriteria Critères de recherche
	 * @param securityListFilter Filtre de sécurité
	 * @param clusteringFacetDefinition Facet utilisée pour cluster des resultats (null si non utilisé)
	 * @param boostedDocumentDateField Nom du champ portant la date du document (null si non utilisé)
	 * @param numDaysOfBoostRefDocument Age des documents servant de référence pour le boost des plus récents par rapport à eux (null si non utilisé)
	 * @param mostRecentBoost Boost relatif maximum entre les plus récents et ceux ayant l'age de référence (doit être > 1) (null si non utilisé)
	 */
	SearchQuery(
			final String listFilterBuilderQuery,
			final Class<? extends ListFilterBuilder> listFilterBuilderClass,
			final Optional<String> geoSearchQuery,
			final Object queryCriteria,
			final Optional<ListFilter> securityListFilter,
			final Optional<FacetedQuery> facetedQuery,
			final FacetDefinition clusteringFacetDefinition,
			final DtField boostedDocumentDateField,
			final Integer numDaysOfBoostRefDocument,
			final Integer mostRecentBoost) {
		Assertion.check()
				.isNotNull(facetedQuery)
				.isNotNull(queryCriteria)
				.isNotNull(securityListFilter)
				.when(boostedDocumentDateField != null, () -> Assertion.check()
						.isTrue(numDaysOfBoostRefDocument != null && mostRecentBoost != null, "Lorsque le boost des documents récents est activé, numDaysOfBoostRefDocument et mostRecentBoost sont obligatoires."))
				.when(boostedDocumentDateField == null, () -> Assertion.check()
						.isTrue(numDaysOfBoostRefDocument == null && mostRecentBoost == null, "Lorsque le boost des documents récents est désactivé, numDaysOfBoostRefDocument et mostRecentBoost doivent être null."))
				.when(numDaysOfBoostRefDocument != null, () -> Assertion.check()
						.isTrue(numDaysOfBoostRefDocument.longValue() > 1, "numDaysOfBoostRefDocument et mostRecentBoost doivent être strictement supérieur à 1."))
				.when(mostRecentBoost != null, () -> Assertion.check()
						.isTrue(mostRecentBoost.longValue() > 1, "numDaysOfBoostRefDocument et mostRecentBoost doivent être strictement supérieur à 1."));
		//-----
		this.facetedQuery = facetedQuery;
		this.queryCriteria = queryCriteria;

		if (listFilterBuilderQuery.isEmpty() || "*:*".equals(listFilterBuilderQuery)) {
			queryListFilter = ListFilter.of("*:*");
		} else {
			final List<DslMultiExpression> dslQuery = DslParserUtil.parseMultiExpression(listFilterBuilderQuery);
			final ListFilterBuilder<Object> listFilterBuilder = InjectorUtil.newInstance(listFilterBuilderClass);
			final ListFilter listFilter = listFilterBuilder
					.withDslQuery(dslQuery)
					.withCriteria(queryCriteria)
					.build();
			queryListFilter = listFilter.getFilterValue().isEmpty() ? ListFilter.of("*:*") : listFilter;
		}

		dslGeoExpression = geoSearchQuery.map(DslParserUtil::parseGeoExpression);

		this.securityListFilter = securityListFilter;
		boostedDocumentDateFieldName = boostedDocumentDateField != null ? boostedDocumentDateField.getName() : null;
		this.numDaysOfBoostRefDocument = numDaysOfBoostRefDocument;
		this.mostRecentBoost = mostRecentBoost;
		clusteringFacetDefinitionRef = clusteringFacetDefinition != null ? new DefinitionReference<>(clusteringFacetDefinition) : null;
	}

	/**
	 * Static method factory for SearchQueryBuilder
	 * @param listFilter ListFilter
	 * @return SearchQueryBuilder
	 */
	public static SearchQueryBuilder builder(final String facetedQueryDefinitionName) {
		return new SearchQueryBuilder(facetedQueryDefinitionName);
	}

	/**
	 * Static method factory for manual SearchQueryBuilder (no definition)
	 * @param listFilterBuilderQuery listFilterBuilderQuery
	 * @param listFilterBuilderClass listFilterBuilderClass
	 * @return SearchQueryBuilder
	 */
	public static SearchQueryBuilder builder(final String listFilterBuilderQuery, final Class<? extends ListFilterBuilder> listFilterBuilderClass) {
		return new SearchQueryBuilder(listFilterBuilderQuery, listFilterBuilderClass);
	}

	/**
	 * Facets informations.
	 * @return facetedQuery.
	 */
	public Optional<FacetedQuery> getFacetedQuery() {
		return facetedQuery;
	}

	/**
	 * Filtre principal correspondant aux critères de la recherche.
	 * @return Valeur du filtre
	 */
	public ListFilter getListFilter() {
		return queryListFilter;
	}

	/**
	 * Filtre geo correspondant aux critères geo de la recherche.
	 * @return Valeur du filtre
	 */
	public Optional<DslGeoExpression> getGeoExpression() {
		return dslGeoExpression;
	}

	/**
	 * Filtre correspondant aux critères de sécurité.
	 * @return Valeur du filtre
	 */
	public Optional<ListFilter> getSecurityListFilter() {
		return securityListFilter;
	}

	/**
	 * Indique que la recherche propose un clustering des documents par une facette.
	 * Le nombre de document par valeur des facette est limité
	 * @return si le clustering est activé
	 */
	public boolean isClusteringFacet() {
		return clusteringFacetDefinitionRef != null;
	}

	/**
	 * @return Facette utilisé pour le clustering
	 */
	public FacetDefinition getClusteringFacetDefinition() {
		Assertion.check().isTrue(isClusteringFacet(), "Le clustering des documents par facette n'est pas activé sur cette recherche");
		//-----
		return clusteringFacetDefinitionRef.get();
	}

	/**
	 * Indique que la recherche boost les documents les plus récents.
	 * C'est une formule de type 1/x qui est utilisée.
	 * La formule de boost est 1 / ((documentAgeDay/NumDaysOfBoostRefDocument) + (1/(MostRecentBoost-1)))
	 * @return si le boost est activé
	 */
	public boolean isBoostMostRecent() {
		return boostedDocumentDateFieldName != null;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Nom du champ portant la date du document
	 */
	public String getBoostedDocumentDateField() {
		Assertion.check().isTrue(isBoostMostRecent(), "Le boost des documents les plus récent n'est pas activé sur cette recherche");
		//-----
		return boostedDocumentDateFieldName;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Age des documents servant de référence pour le boost des plus récents par rapport à eux
	 */
	public int getNumDaysOfBoostRefDocument() {
		Assertion.check().isTrue(isBoostMostRecent(), "Le boost des documents les plus récent, n'est pas activé sur cette recherche");
		//-----
		return numDaysOfBoostRefDocument;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Boost relatif maximum entre les plus récents et ceux ayant l'age de référence (doit être > 1).
	 */
	public int getMostRecentBoost() {
		Assertion.check().isTrue(isBoostMostRecent(), "Le boost des documents les plus récent, n'est pas activé sur cette recherche");
		//-----
		return mostRecentBoost;
	}

	/**
	 * Query criteria
	 * @return Query criteria
	 */
	public Object getCriteria() {
		return queryCriteria;
	}

}
