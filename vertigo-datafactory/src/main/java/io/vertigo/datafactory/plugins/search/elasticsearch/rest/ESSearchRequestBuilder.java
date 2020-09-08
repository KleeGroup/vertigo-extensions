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
package io.vertigo.datafactory.plugins.search.elasticsearch.rest;

import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.datafactory.plugins.search.elasticsearch.AsbtractESSearchRequestBuilder;
import io.vertigo.datafactory.plugins.search.elasticsearch.ESDocumentCodec;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datamodel.structure.model.DtListState;

//vérifier
/**
 * ElasticSearch request builder from searchManager api.
 * @author pchretien, npiedeloup
 */
final class ESSearchRequestBuilder extends AsbtractESSearchRequestBuilder<SearchRequest, SearchSourceBuilder, ESSearchRequestBuilder> {

	private final SearchRequest searchRequest;
	private final SearchSourceBuilder searchSourceBuilder;

	/**
	 * @param indexName Index name (env name)
	 * @param esClient ElasticSearch client
	 */
	ESSearchRequestBuilder(final String indexName, final RestHighLevelClient esClient, final Map<Class, BasicTypeAdapter> typeAdapters) {
		super(typeAdapters);
		//-----
		searchSourceBuilder = new SearchSourceBuilder()
				.fetchSource(new String[] { ESDocumentCodec.FULL_RESULT }, null);

		searchRequest = new SearchRequest(indexName)
				.searchType(SearchType.QUERY_THEN_FETCH)
				.source(searchSourceBuilder);
	}

	@Override
	protected void appendListState(final SearchQuery searchQuery, final DtListState listState, final int defaultMaxRows, final SearchIndexDefinition indexDefinition) {
		searchSourceBuilder.from(listState.getSkipRows())
				//If we send a clustering query, we don't retrieve result with hits response but with buckets
				.size(searchQuery.isClusteringFacet() ? 0 : listState.getMaxRows().orElse(defaultMaxRows));
		if (listState.getSortFieldName().isPresent()) {
			searchSourceBuilder.sort(getFieldSortBuilder(indexDefinition, listState));
		}
	}

	@Override
	protected SearchSourceBuilder getSearchSourceBuilder() {
		return searchSourceBuilder;
	}

	@Override
	protected SearchRequest getSearchRequest() {
		return searchRequest;
	}

	@Override
	protected void setQueryAndPostFilter(final QueryBuilder requestQueryBuilder, final BoolQueryBuilder postFilterBoolQueryBuilder) {
		searchSourceBuilder
				.query(requestQueryBuilder)
				.postFilter(postFilterBoolQueryBuilder);
	}

	@Override
	protected void setHighlighter(final HighlightBuilder highlightBuilder) {
		searchSourceBuilder.highlighter(highlightBuilder);
	}

	@Override
	protected void addAggregation(final SearchSourceBuilder searchRequestBuilder, final AggregationBuilder aggregationBuilder) {
		searchRequestBuilder.aggregation(aggregationBuilder);
	}

}
