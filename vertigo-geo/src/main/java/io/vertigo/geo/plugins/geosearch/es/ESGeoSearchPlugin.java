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
package io.vertigo.geo.plugins.geosearch.es;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.connectors.elasticsearch.RestHighLevelElasticSearchConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.util.VCollectors;
import io.vertigo.geo.geocoder.GeoLocation;
import io.vertigo.geo.impl.geosearch.GeoSearchPlugin;

public final class ESGeoSearchPlugin implements GeoSearchPlugin, Activeable {

	private final RestHighLevelElasticSearchConnector elasticSearchConnector;

	private RestHighLevelClient esClient;
	private final String envIndexPrefix;
	private final CodecManager codecManager;

	@Inject
	public ESGeoSearchPlugin(
			@ParamValue("envIndexPrefix") final String envIndexPrefix,
			@ParamValue("connectorName") final Optional<String> connectorNameOpt,
			final List<RestHighLevelElasticSearchConnector> elasticSearchConnectors,
			final CodecManager codecManager,
			final SmartTypeManager smartTypeManager,
			final ResourceManager resourceManager) {
		Assertion.check()
				.isNotBlank(envIndexPrefix)
				.isNotNull(elasticSearchConnectors)
				.isFalse(elasticSearchConnectors.isEmpty(), "At least one ElasticSearchConnector espected");
		//-----
		//------
		this.envIndexPrefix = envIndexPrefix;
		final String connectorName = connectorNameOpt.orElse("main");
		elasticSearchConnector = elasticSearchConnectors.stream()
				.filter(connector -> connectorName.equals(connector.getName()))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("Can't found ElasticSearchConnector named '" + connectorName + "' in " + elasticSearchConnectors));

		this.codecManager = codecManager;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//Init ElasticSearch Client
		esClient = elasticSearchConnector.getClient();
		//must wait yellow status to be sure prepareExists works fine (instead of returning false on a already exist index)
		waitForYellowStatus();

	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		// nothing
	}

	private String obtainIndexName(final String indexName) {
		return StringUtil.camelToConstCase(envIndexPrefix + indexName).toLowerCase(Locale.ROOT);
	}

	@Override
	public <D extends DtObject> DtList<D> searchInBoundingBox(
			final GeoLocation topLeft,
			final GeoLocation bottomRight,
			final String indexName,
			final Class<D> dtIndexClass,
			final DtFieldName<D> fieldName,
			final Integer maxRows) {
		final GeoBoundingBoxQueryBuilder geoBoundingBoxQueryBuilder = QueryBuilders.geoBoundingBoxQuery(fieldName.name())
				.setCorners(new GeoPoint(topLeft.getLatitude(), topLeft.getLongitude()), new GeoPoint(bottomRight.getLatitude(), bottomRight.getLongitude()));
		try {
			final SearchHits searchHits = esClient
					.search(new SearchRequest(obtainIndexName(indexName))
							.searchType(SearchType.QUERY_THEN_FETCH)
							.source(new SearchSourceBuilder()
									.query(geoBoundingBoxQueryBuilder)
									.fetchSource(new String[] { "fullResult" }, null)
									.size(maxRows)),
							RequestOptions.DEFAULT)
					.getHits();
			return Stream.of(searchHits.getHits())
					.map(hit -> {
						return (D) codecManager.getCompressedSerializationCodec().decode(codecManager.getBase64Codec().decode((String) hit.getSourceAsMap().get("fullResult")));
					})
					.collect(VCollectors.toDtList(dtIndexClass));
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}

	}

	private void waitForYellowStatus() {
		try {
			final ClusterHealthRequest request = new ClusterHealthRequest();
			request.timeout(TimeValue.timeValueSeconds(30));
			request.waitForYellowStatus();

			final ClusterHealthResponse response = esClient.cluster().health(request, RequestOptions.DEFAULT);
			//-----
			Assertion.check().isFalse(response.isTimedOut(), "ElasticSearch cluster waiting yellow status Timedout");
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error on waitForYellowStatus");
		}
	}

}
