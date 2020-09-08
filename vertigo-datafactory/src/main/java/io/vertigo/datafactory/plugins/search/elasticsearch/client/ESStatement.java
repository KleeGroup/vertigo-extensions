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
package io.vertigo.datafactory.plugins.search.elasticsearch.client;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.lang.VUserException;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.impl.search.SearchResource;
import io.vertigo.datafactory.plugins.search.elasticsearch.AsbtractESSearchRequestBuilder;
import io.vertigo.datafactory.plugins.search.elasticsearch.ESDocumentCodec;
import io.vertigo.datafactory.plugins.search.elasticsearch.ESFacetedQueryResultBuilder;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.model.SearchIndex;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;

//vérifier
/**
 * Requête physique d'accès à ElasticSearch.
 * Le driver exécute les requêtes de façon synchrone dans le contexte transactionnelle de la ressource.
 * @author pchretien, npiedeloup
 * @param <I> Type de l'objet représentant l'index
 * @param <K> Type du keyConcept métier indexé
 */
final class ESStatement<K extends KeyConcept, I extends DtObject> {

	private static final RefreshPolicy DEFAULT_REFRESH = RefreshPolicy.NONE; //mettre a true pour TU uniquement
	private static final RefreshPolicy BULK_REFRESH = RefreshPolicy.NONE; //mettre a RefreshPolicy.IMMEDIATE pour TU uniquement
	private static final Logger LOGGER = LogManager.getLogger(ESStatement.class);

	private final String indexName;
	private final Client esClient;
	private final ESDocumentCodec esDocumentCodec;
	private final Map<Class, BasicTypeAdapter> typeAdapters;

	/**
	 * Constructor.
	 * @param esDocumentCodec Codec de traduction (bi-directionnelle) des objets métiers en document
	 * @param indexName Index name
	 * @param esClient Client ElasticSearch.
	 */
	ESStatement(final ESDocumentCodec esDocumentCodec, final String indexName, final Client esClient, final Map<Class, BasicTypeAdapter> typeAdapters) {
		Assertion.check().isNotBlank(indexName)
				.isNotNull(esDocumentCodec)
				.isNotNull(esClient)
				.isNotNull(typeAdapters);
		//-----
		this.indexName = indexName;
		this.esClient = esClient;
		this.esDocumentCodec = esDocumentCodec;
		this.typeAdapters = typeAdapters;
	}

	/**
	 * @param indexCollection Collection des indexes à insérer
	 */
	void putAll(final Collection<SearchIndex<K, I>> indexCollection) {
		//Injection spécifique au moteur d'indexation.
		try {
			final BulkRequestBuilder bulkRequest = esClient.prepareBulk().setRefreshPolicy(BULK_REFRESH);
			for (final SearchIndex<K, I> index : indexCollection) {
				try (final XContentBuilder xContentBuilder = esDocumentCodec.index2XContentBuilder(index)) {
					bulkRequest.add(esClient.prepareIndex()
							.setIndex(indexName)
							.setId(index.getUID().urn())
							.setSource(xContentBuilder));
				}
			}
			final BulkResponse bulkResponse = bulkRequest.execute().actionGet();
			if (bulkResponse.hasFailures()) {
				throw new VSystemException("Can't putAll into {1} index.\nCause by {2}", indexName, bulkResponse.buildFailureMessage());
			}
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	private static void handleIOException(final IOException e) {
		throw WrappedException.wrap(e, "Serveur ElasticSearch indisponible");
	}

	/**
	 * @param index index à insérer
	 */
	void put(final SearchIndex<K, I> index) {
		//Injection spécifique au moteur d'indexation.
		try (final XContentBuilder xContentBuilder = esDocumentCodec.index2XContentBuilder(index)) {
			esClient.prepareIndex().setRefreshPolicy(DEFAULT_REFRESH)
					.setIndex(indexName)
					.setId(index.getUID().urn())
					.setSource(xContentBuilder)
					.execute() //execute asynchrone
					.actionGet(); //get wait exec
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	/**
	 * Supprime des documents.
	 * @param query Requete de filtrage des documents à supprimer
	 */
	void remove(final ListFilter query) {
		Assertion.check().isNotNull(query);
		//-----
		try {
			final QueryBuilder queryBuilder = AsbtractESSearchRequestBuilder.translateToQueryBuilder(query);
			final DeleteByQueryRequestBuilder deleteByQueryAction = new DeleteByQueryRequestBuilder(esClient, DeleteByQueryAction.INSTANCE)
					.filter(queryBuilder);
			deleteByQueryAction
					.source()
					.setIndices(indexName);
			final BulkByScrollResponse response = deleteByQueryAction.get();
			final long deleted = response.getDeleted();
			LOGGER.debug("Removed {} elements", deleted);
		} catch (final SearchPhaseExecutionException e) {
			final VUserException vue = new VUserException(SearchResource.DYNAMO_SEARCH_QUERY_SYNTAX_ERROR);
			vue.initCause(e);
			throw vue;
		}
	}

	/**
	 * Supprime un document.
	 * @param uid UID du document à supprimer
	 */
	void remove(final UID uid) {
		Assertion.check().isNotNull(uid);
		//-----
		esClient.prepareDelete().setRefreshPolicy(DEFAULT_REFRESH)
				.setIndex(indexName)
				.setId(uid.urn())
				.execute()
				.actionGet();
	}

	/**
	 * @param indexDefinition Index de recherche
	 * @param searchQuery Mots clés de recherche
	 * @param listState Etat de la liste (tri et pagination)
	 * @param defaultMaxRows Nombre de ligne max par defaut
	 * @return Résultat de la recherche
	 */
	FacetedQueryResult<I, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState, final int defaultMaxRows) {
		Assertion.check().isNotNull(searchQuery);
		//-----
		final SearchRequestBuilder searchRequestBuilder = new ESSearchRequestBuilder(indexName, esClient, typeAdapters)
				.withSearchIndexDefinition(indexDefinition)
				.withSearchQuery(searchQuery)
				.withListState(listState, defaultMaxRows)
				.build();
		LOGGER.info("loadList {}", searchRequestBuilder);
		try {
			final SearchResponse queryResponse = searchRequestBuilder.execute().actionGet();
			return new ESFacetedQueryResultBuilder(esDocumentCodec, indexDefinition, queryResponse, searchQuery)
					.build();
		} catch (final SearchPhaseExecutionException e) {
			final VUserException vue = new VUserException(SearchResource.DYNAMO_SEARCH_QUERY_SYNTAX_ERROR);
			vue.initCause(e);
			throw vue;
		}
	}

	/**
	 * @return Nombre de document indexés
	 */
	public long count() {
		final SearchResponse response = esClient.prepareSearch(indexName)
				.setSize(0) //on cherche juste à compter
				.execute()
				.actionGet();
		return response.getHits().getTotalHits().value;
	}
}
