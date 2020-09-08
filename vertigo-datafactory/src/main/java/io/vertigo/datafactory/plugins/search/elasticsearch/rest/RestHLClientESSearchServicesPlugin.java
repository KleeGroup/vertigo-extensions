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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.connectors.elasticsearch.RestHighLevelElasticSearchConnector;
import io.vertigo.core.analytics.health.HealthChecked;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthMeasureBuilder;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.impl.search.SearchServicesPlugin;
import io.vertigo.datafactory.plugins.search.elasticsearch.ESDocumentCodec;
import io.vertigo.datafactory.plugins.search.elasticsearch.IndexType;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.model.SearchIndex;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;

/**
 * Gestion de la connexion au serveur Solr de manière transactionnel.
 * @author dchallas, npiedeloup
 */
public final class RestHLClientESSearchServicesPlugin implements SearchServicesPlugin, Activeable {
	private static final int DEFAULT_SCALING_FACTOR = 1000;
	private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy||strict_date_optional_time||epoch_second";
	private static final int OPTIMIZE_MAX_NUM_SEGMENT = 32;
	/** field suffix for keyword fields added by this plugin. */
	public static final String SUFFIX_SORT_FIELD = ".keyword";

	private static final Logger LOGGER = LogManager.getLogger(RestHLClientESSearchServicesPlugin.class);
	private final SmartTypeManager smartTypeManager;
	private final CodecManager codecManager;
	private final RestHighLevelElasticSearchConnector elasticSearchConnector;

	private Map<Class, BasicTypeAdapter> typeAdapters;
	private ESDocumentCodec elasticDocumentCodec;

	private RestHighLevelClient esClient;
	private final DtListState defaultListState;
	private final int defaultMaxRows;
	private final String envIndexPrefix;
	private final URL configFileUrl;
	private boolean indexSettingsValid;

	/**
	 * Constructor.
	 * @param envIndexPrefix ES index name
	 * @param indexNameIsPrefix indexName use as prefix
	 * @param defaultMaxRows Nombre de lignes
	 * @param codecManager Manager de codec
	 * @param configFile Fichier de configuration des indexs
	 * @param resourceManager Manager des resources
	 */
	@Inject
	public RestHLClientESSearchServicesPlugin(
			@ParamValue("envIndexPrefix") final String envIndexPrefix,
			@ParamValue("rowsPerQuery") final int defaultMaxRows,
			@ParamValue("config.file") final String configFile,
			@ParamValue("connectorName") final Optional<String> connectorNameOpt,
			final List<RestHighLevelElasticSearchConnector> elasticSearchConnectors,
			final CodecManager codecManager,
			final SmartTypeManager smartTypeManager,
			final ResourceManager resourceManager) {
		Assertion.check()
				.isNotBlank(envIndexPrefix)
				.isNotNull(elasticSearchConnectors)
				.isFalse(elasticSearchConnectors.isEmpty(), "At least one ElasticSearchConnector espected");
		//Assertion.when(indexNameIsPrefix).check(() -> indexNameOrPrefix.endsWith("_"), "When envIndex is use as prefix, it must ends with _ (current : {0})", indexNameOrPrefix);
		//Assertion.when(!indexNameIsPrefix).check(() -> !indexNameOrPrefix.endsWith("_"), "When envIndex isn't declared as prefix, it can't ends with _ (current : {0})", indexNameOrPrefix);
		//-----
		this.smartTypeManager = smartTypeManager;
		this.codecManager = codecManager;
		this.defaultMaxRows = defaultMaxRows;
		defaultListState = DtListState.of(defaultMaxRows);
		//------
		this.envIndexPrefix = envIndexPrefix;
		configFileUrl = resourceManager.resolve(configFile);
		final String connectorName = connectorNameOpt.orElse("main");
		elasticSearchConnector = elasticSearchConnectors.stream()
				.filter(connector -> connectorName.equals(connector.getName()))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("Can't found ElasticSearchConnector named '" + connectorName + "' in " + elasticSearchConnectors));
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		typeAdapters = smartTypeManager.getTypeAdapters("search");
		elasticDocumentCodec = new ESDocumentCodec(codecManager, typeAdapters);
		//Init ElasticSearch Client
		esClient = elasticSearchConnector.getClient();
		indexSettingsValid = true;
		//must wait yellow status to be sure prepareExists works fine (instead of returning false on a already exist index)
		waitForYellowStatus();
		//Init typeMapping IndexDefinition <-> Conf ElasticSearch
		for (final SearchIndexDefinition indexDefinition : Node.getNode().getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			final String myIndexName = obtainIndexName(indexDefinition);
			try {
				createIndex(myIndexName);
				updateTypeMapping(indexDefinition, hasSortableNormalizer(myIndexName));
				logMappings(myIndexName);
			} catch (final IOException e) {
				throw WrappedException.wrap(e, "Error on index {0}", myIndexName);
			}
		}
		waitForYellowStatus();

	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		// nothing
	}

	private boolean hasSortableNormalizer(final String myIndexName) {
		try {
			final GetSettingsRequest request = new GetSettingsRequest().indices(myIndexName);
			final GetSettingsResponse getIndexResponse = esClient.indices().getSettings(request, RequestOptions.DEFAULT);

			final Settings currentSettings = getIndexResponse.getIndexToSettings().get(myIndexName);
			return !currentSettings.getAsSettings("index.analysis.normalizer.sortable").isEmpty();
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error on index {0}", myIndexName);
		}
	}

	private String obtainIndexName(final SearchIndexDefinition indexDefinition) {
		return StringUtil.camelToConstCase(envIndexPrefix + indexDefinition.getName()).toLowerCase(Locale.ROOT);
	}

	private void createIndex(final String myIndexName) throws IOException {
		final GetIndexRequest getIndexRequest = new GetIndexRequest(myIndexName);
		if (!esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
			final CreateIndexRequest createIndexRequest = new CreateIndexRequest(myIndexName);
			if (configFileUrl != null) {
				try (InputStream is = configFileUrl.openStream()) {
					final Settings settings = Settings.builder().loadFromStream(configFileUrl.getFile(), is, false).build();
					createIndexRequest.settings(settings);
				}
			}
			final AcknowledgedResponse createIndexResponse = esClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
			Assertion.check().isTrue(createIndexResponse.isAcknowledged(), "Can't create index settings of {0}", myIndexName);
		} else if (configFileUrl != null) {
			// If we use local config file, we check config against ES server
			try (InputStream is = configFileUrl.openStream()) {
				final Settings settings = Settings.builder().loadFromStream(configFileUrl.getFile(), is, false).build();
				indexSettingsValid = indexSettingsValid && !isIndexSettingsDirty(myIndexName, settings);
			}
		}
	}

	private boolean isIndexSettingsDirty(final String myIndexName, final Settings settings) throws IOException {
		final GetSettingsRequest request = new GetSettingsRequest().indices(myIndexName);
		final GetSettingsResponse getIndexResponse = esClient.indices().getSettings(request, RequestOptions.DEFAULT);
		final Settings currentSettings = getIndexResponse.getIndexToSettings().get(myIndexName);

		boolean indexSettingsDirty = false;
		final Set<String> settingsNames = settings.keySet();
		for (final String settingsName : settingsNames) {
			final String currentValue = currentSettings.get(settingsName);
			if (currentValue == null) {
				indexSettingsDirty = true;
				break;
			}
			final Object expectedValue = settings.get(settingsName);
			if (!currentValue.equals(expectedValue)) {
				indexSettingsDirty = true;
				LOGGER.warn("[{}] {} :  current={}, expected= {}", myIndexName, settingsName, currentValue, expectedValue);
				break;
			}
		}
		return indexSettingsDirty;
	}

	private void logMappings(final String myIndexName) throws IOException {
		final GetMappingsRequest request = new GetMappingsRequest().indices(myIndexName);
		final GetMappingsResponse getMappingsResponse = esClient.indices().getMapping(request, RequestOptions.DEFAULT);

		final Map<String, MappingMetadata> indexMappings = getMappingsResponse.mappings();
		LOGGER.info("Index {} CurrentMapping:", myIndexName);
		for (final Entry<String, MappingMetadata> dtoMapping : indexMappings.entrySet()) {
			LOGGER.info(" {} -> {}", dtoMapping.getKey(), dtoMapping.getValue().source());
		}
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept, I extends DtObject> void putAll(final SearchIndexDefinition indexDefinition, final Collection<SearchIndex<S, I>> indexCollection) {
		Assertion.check().isNotNull(indexCollection);
		//-----
		final ESStatement<S, I> statement = createElasticStatement(indexDefinition);
		statement.putAll(indexCollection);
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept, I extends DtObject> void put(final SearchIndexDefinition indexDefinition, final SearchIndex<S, I> index) {
		//On vérifie la cohérence des données SO et SOD.
		Assertion.check()
				.isNotNull(indexDefinition)
				.isNotNull(index)
				.isTrue(indexDefinition.equals(index.getDefinition()), "les Définitions ne sont pas conformes");
		//-----
		final ESStatement<S, I> statement = createElasticStatement(indexDefinition);
		statement.put(index);
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept> void remove(final SearchIndexDefinition indexDefinition, final UID<S> uri) {
		Assertion.check()
				.isNotNull(uri)
				.isNotNull(indexDefinition);
		//-----
		createElasticStatement(indexDefinition).remove(uri);
		markToOptimize(obtainIndexName(indexDefinition));
	}

	/** {@inheritDoc} */
	@Override
	public <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState) {
		Assertion.check().isNotNull(searchQuery);
		//-----
		final ESStatement<KeyConcept, R> statement = createElasticStatement(indexDefinition);
		final DtListState usedListState = listState != null ? listState : defaultListState;
		return statement.loadList(indexDefinition, searchQuery, usedListState, defaultMaxRows);
	}

	/** {@inheritDoc} */
	@Override
	public long count(final SearchIndexDefinition indexDefinition) {
		Assertion.check().isNotNull(indexDefinition);
		//-----
		return createElasticStatement(indexDefinition).count();
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final SearchIndexDefinition indexDefinition, final ListFilter listFilter) {
		Assertion.check()
				.isNotNull(indexDefinition)
				.isNotNull(listFilter);
		//-----
		createElasticStatement(indexDefinition).remove(listFilter);
		markToOptimize(obtainIndexName(indexDefinition));
	}

	private <S extends KeyConcept, I extends DtObject> ESStatement<S, I> createElasticStatement(final SearchIndexDefinition indexDefinition) {
		Assertion.check()
				.isTrue(indexSettingsValid,
						"Index settings have changed and are no more compatible, you must recreate your index : stop server, delete your index data folder, restart server and launch indexation job.")
				.isNotNull(indexDefinition);
		//-----
		return new ESStatement<>(elasticDocumentCodec, obtainIndexName(indexDefinition), esClient, typeAdapters);
	}

	private static String obtainPkIndexDataType(final SmartTypeDefinition smartTypeDefinition) {
		// On peut préciser pour chaque smartType le type d'indexation
		// Calcul automatique  par default.
		Assertion.check().isTrue(smartTypeDefinition.getScope().isPrimitive(), "Type de donnée non pris en charge comme PK pour le keyconcept indexé [" + smartTypeDefinition + "].");
		switch (smartTypeDefinition.getBasicType()) {
			case Boolean:
			case Double:
			case Integer:
			case Long:
				return smartTypeDefinition.getBasicType().name().toLowerCase(Locale.ROOT);
			case String:
				return "keyword";
			case LocalDate:
			case Instant:
			case BigDecimal:
			case DataStream:
			default:
				throw new IllegalArgumentException("Type de donnée non pris en charge comme PK pour le keyconcept indexé [" + smartTypeDefinition + "].");
		}
	}

	/**
	 * Update template definition of this type.
	 * @param indexDefinition Index concerné
	 * @throws IOException
	 */
	private void updateTypeMapping(final SearchIndexDefinition indexDefinition, final boolean sortableNormalizer) throws IOException {
		Assertion.check().isNotNull(indexDefinition);
		//-----
		final String myIndexName = obtainIndexName(indexDefinition);

		try (final XContentBuilder typeMapping = XContentFactory.jsonBuilder()) {
			typeMapping.startObject()
					.startObject("properties")
					.startObject(ESDocumentCodec.FULL_RESULT)
					.field("type", "binary")
					.endObject();

			typeMapping.startObject(ESDocumentCodec.DOC_ID)
					.field("type", obtainPkIndexDataType(indexDefinition.getKeyConceptDtDefinition().getIdField().get().getSmartTypeDefinition()))
					.endObject();

			/* 3 : Les champs du dto index */
			final Set<DtField> copyFromFields = indexDefinition.getIndexCopyFromFields();
			final DtDefinition indexDtDefinition = indexDefinition.getIndexDtDefinition();
			for (final DtField dtField : indexDtDefinition.getFields()) {
				final IndexType indexType = IndexType.readIndexType(dtField.getSmartTypeDefinition());
				typeMapping.startObject(dtField.getName());
				appendIndexTypeMapping(typeMapping, indexType);
				if (copyFromFields.contains(dtField)) {
					appendIndexCopyToMapping(indexDefinition, typeMapping, dtField);
				}
				if (indexType.isIndexSubKeyword()) {
					typeMapping.startObject("fields");
					typeMapping.startObject("keyword");
					typeMapping.field("type", "keyword");
					if (sortableNormalizer) {
						typeMapping.field("normalizer", "sortable");
					}
					typeMapping.endObject();
					typeMapping.endObject();
				}
				if (indexType.isIndexFieldData()) {
					typeMapping.field("fielddata", true);
				}
				typeMapping.endObject();
			}
			typeMapping.endObject().endObject(); //end properties

			final PutMappingRequest putMappingRequest = new PutMappingRequest(myIndexName);
			putMappingRequest.source(typeMapping);
			//le Type est deprecated setType(indexDefinition.getName())

			LOGGER.info("set index mapping of {} as {}", myIndexName, typeMapping);
			final AcknowledgedResponse putMappingResponse = esClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
			Assertion.check().isTrue(putMappingResponse.isAcknowledged(), "Can't put index mapping of {0}", myIndexName);
		}
	}

	private static void appendIndexCopyToMapping(final SearchIndexDefinition indexDefinition, final XContentBuilder typeMapping, final DtField dtField) throws IOException {
		final List<DtField> copyToFields = indexDefinition.getIndexCopyToFields(dtField);
		if (copyToFields.size() == 1) {
			typeMapping.field("copy_to", copyToFields.get(0).getName());
		} else {
			final String[] copyToFieldNames = new String[copyToFields.size()];
			for (int i = 0; i < copyToFieldNames.length; i++) {
				copyToFieldNames[i] = copyToFields.get(i).getName();
			}
			typeMapping.field("copy_to", copyToFieldNames);
		}
	}

	private static void appendIndexTypeMapping(final XContentBuilder typeMapping, final IndexType indexType) throws IOException {
		typeMapping.field("type", indexType.getIndexDataType());
		if (indexType.getIndexAnalyzer().isPresent()) {
			typeMapping.field("keyword".equals(indexType.getIndexDataType()) ? "normalizer" : "analyzer", indexType.getIndexAnalyzer().get());
		}
		if ("scaled_float".equals(indexType.getIndexDataType())) {
			typeMapping.field("scaling_factor", DEFAULT_SCALING_FACTOR);
		}
		if ("date".equals(indexType.getIndexDataType())) {
			typeMapping.field("format", DEFAULT_DATE_FORMAT);
		}
	}

	private void markToOptimize(final String myIndexName) {
		final ForceMergeRequest request = new ForceMergeRequest(myIndexName)
				.maxNumSegments(OPTIMIZE_MAX_NUM_SEGMENT)//32 files : empirique
				.flush(true);
		esClient.indices().forcemergeAsync(request, RequestOptions.DEFAULT, null);
		/*try {
			final ForceMergeResponse forceMergeResponse = esClient.indices().forcemerge(request, RequestOptions.DEFAULT);
			Assertion.check().argument(forceMergeResponse.getStatus() == RestStatus.OK, "Can't forceMerge on {0}", myIndexName);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error on index {0}", myIndexName);
		}*/
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

	@HealthChecked(name = "clusterHealth", feature = "search")
	public HealthMeasure checkClusterHealth() {
		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();
		try {
			final ClusterHealthResponse clusterHealthResponse = esClient
					.cluster()
					.health(new ClusterHealthRequest(), RequestOptions.DEFAULT);
			switch (clusterHealthResponse.getStatus()) {
				case GREEN:
					healthMeasureBuilder.withGreenStatus();
					break;
				case YELLOW:
					healthMeasureBuilder.withYellowStatus(null, null);
					break;
				case RED:
					healthMeasureBuilder.withRedStatus(null, null);
					break;
				default:
					break;
			}
		} catch (final Exception e) {
			healthMeasureBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthMeasureBuilder.build();
	}

}
