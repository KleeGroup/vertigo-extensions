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
package io.vertigo.orchestra;

import io.vertigo.account.AccountFeatures;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.connectors.javalin.JavalinFeatures;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.NodeConfigBuilder;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.core.plugins.resource.url.URLResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.orchestra.boot.DataBaseInitializer;
import io.vertigo.orchestra.services.execution.LocalExecutionProcessInitializer;
import io.vertigo.orchestra.util.monitoring.MonitoringServices;
import io.vertigo.orchestra.util.monitoring.MonitoringServicesImpl;
import io.vertigo.orchestra.webservices.WsDefinition;
import io.vertigo.orchestra.webservices.WsExecution;
import io.vertigo.orchestra.webservices.WsExecutionControl;
import io.vertigo.orchestra.webservices.data.user.TestUserSession;
import io.vertigo.orchestra.webservices.data.user.WsTestLogin;
import io.vertigo.vega.VegaFeatures;

public final class MyNodeConfig {
	public static final int WS_PORT = 8088;

	public static NodeConfigBuilder createNodeConfigBuilder() {
		return NodeConfig.builder()
				.withNodeId("NODE_TEST_1")
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.addPlugin(URLResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures()
						.withScript()
						.withJaninoScript()
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.withC3p0(
								Param.of("name", "orchestra"),
								Param.of("dataBaseClass", H2DataBase.class.getName()),
								Param.of("jdbcDriver", org.h2.Driver.class.getName()),
								Param.of("jdbcUrl", "jdbc:h2:~/vertigo/orchestra;AUTO_SERVER=TRUE"))
						//Param.of("jdbcUrl", "jdbc:h2:mem:orchestra;MVCC=FALSE"))
						.build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withMemoryCache()
						.withKVStore()
						.withDelayedMemoryKV(
								Param.of("collections", "tokens"),
								Param.of("timeToLiveSeconds", "120"))
						.withEntityStore()
						.withSqlEntityStore(
								Param.of("dataSpace", "orchestra"),
								Param.of("connectionName", "orchestra"),
								Param.of("sequencePrefix", "SEQ_"))
						.build())
				// we build h2 mem
				.addModule(ModuleConfig.builder("databaseInitializer").addComponent(DataBaseInitializer.class).build())
				//
				.addModule(new OrchestraFeatures()
						.withDataBase(Param.of("nodeName", "NODE_TEST_1"), Param.of("daemonPeriodSeconds", "1"), Param.of("workersCount", "3"), Param.of("forecastDurationSeconds", "60"))
						.withMemory(Param.of("workersCount", "1"))
						.build())
				.addModule(ModuleConfig.builder("orchestra-test")
						//---Services
						.addComponent(MonitoringServices.class, MonitoringServicesImpl.class)
						.build())
				.addInitializer(LocalExecutionProcessInitializer.class);
	}

	public static void addVegaEmbeded(final NodeConfigBuilder nodeConfigBuilder) {
		nodeConfigBuilder
				.addModule(new JavalinFeatures().withEmbeddedServer(Param.of("port", WS_PORT)).build())
				.addModule(new AccountFeatures()
						.withSecurity(Param.of("userSessionClassName", TestUserSession.class.getName()))
						.build())
				.addModule(new VegaFeatures()
						.withWebServices()
						.withJavalinWebServerPlugin()
						.withWebServicesTokens(Param.of("tokens", "tokens"))
						.withWebServicesSecurity()
						.withWebServicesRateLimiting()
						.build());
	}

	public static void addWebServices(final NodeConfigBuilder nodeConfigBuilder) {
		nodeConfigBuilder
				.addModule(ModuleConfig.builder("orchestra-ws")
						.addComponent(WsDefinition.class)
						.addComponent(WsExecution.class)
						.addComponent(WsExecutionControl.class)
						.addComponent(WsTestLogin.class)
						.build());
	}

	public static NodeConfig config() {
		// @formatter:off
		return createNodeConfigBuilder().build();
	}

	public static NodeConfig configWithVega() {
		// @formatter:off
		final NodeConfigBuilder builder = createNodeConfigBuilder();
		addVegaEmbeded(builder);
		addWebServices(builder);
		return builder.build();
	}



}
