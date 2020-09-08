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
package io.vertigo.datafactory.search;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.connectors.elasticsearch.ElasticSearchFeatures;
import io.vertigo.connectors.elasticsearch.EmbeddedElasticSearchServer;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.NodeConfigBuilder;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.datafactory.DataFactoryFeatures;
import io.vertigo.datafactory.search.data.ItemSearchClient;
import io.vertigo.datafactory.search.data.TestSearchSmartTypes;
import io.vertigo.datafactory.search.data.domain.ItemSearchLoader;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datastore.DataStoreFeatures;

public final class MyNodeConfig {

	public static NodeConfig config(final boolean esHL, final boolean withDb) {
		final DataFactoryFeatures dataFactoryFeatures = new DataFactoryFeatures();
		dataFactoryFeatures.withSearch();

		final ElasticSearchFeatures elasticSearchFeatures = new ElasticSearchFeatures()
				.withEmbeddedServer(
						Param.of("home", "io/vertigo/datafactory/search/indexconfig"));
		if (esHL) {
			elasticSearchFeatures.withRestHL(
					Param.of("servers.names", "localhost:9200"));

			dataFactoryFeatures.withESHL(
					Param.of("config.file", "io/vertigo/datafactory/search/indexconfig/elasticsearch.yml"),
					Param.of("envIndexPrefix", "TuTest"),
					Param.of("rowsPerQuery", "50"));
		} else {
			elasticSearchFeatures.withTransport(
					Param.of("servers.names", "localhost:9300"),
					Param.of("cluster.name", EmbeddedElasticSearchServer.DEFAULT_VERTIGO_ES_CLUSTER_NAME));

			dataFactoryFeatures.withESClient(
					Param.of("config.file", "io/vertigo/datafactory/search/indexconfig/elasticsearch.yml"),
					Param.of("envIndexPrefix", "TuTest"),
					Param.of("rowsPerQuery", "50"));
		}
		final NodeConfigBuilder nodeConfigBuilder = NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures()
						.withScript()
						.withJaninoScript()
						.build());
		if (withDb) {
			nodeConfigBuilder.addModule(new DatabaseFeatures()
					.withSqlDataBase()
					.withC3p0(
							Param.of("dataBaseClass", H2DataBase.class.getName()),
							Param.of("jdbcDriver", "org.h2.Driver"),
							Param.of("jdbcUrl", "jdbc:h2:mem:database"))
					.build());
		}
		nodeConfigBuilder.addModule(new DataModelFeatures().build());
		if (withDb) {
			nodeConfigBuilder.addModule(new DataStoreFeatures()
					.withCache()
					.withMemoryCache()
					.withEntityStore()
					.withSqlEntityStore()
					.build());
		}
		nodeConfigBuilder.addModule(elasticSearchFeatures.build())
				.addModule(dataFactoryFeatures.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(ItemSearchClient.class)
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSearchSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", "io.vertigo.datafactory.search.data.DtDefinitions")
								.build())
						.addComponent(withDb ? io.vertigo.datafactory.search.withstore.ItemSearchLoader.class : ItemSearchLoader.class)
						.addDefinitionProvider(StoreCacheDefinitionProvider.class)
						.build());
		return nodeConfigBuilder.build();
	}
}
