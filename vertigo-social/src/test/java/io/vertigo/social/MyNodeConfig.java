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
package io.vertigo.social;

import io.vertigo.account.AccountFeatures;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.connectors.javalin.JavalinFeatures;
import io.vertigo.connectors.redis.RedisFeatures;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.NodeConfigBuilder;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.social.data.MockIdentities;
import io.vertigo.social.notification.data.TestUserSession;
import io.vertigo.social.notification.webservices.TestLoginWebServices;
import io.vertigo.social.webservices.account.AccountWebServices;
import io.vertigo.social.webservices.comment.CommentWebServices;
import io.vertigo.social.webservices.notification.NotificationWebServices;
import io.vertigo.vega.VegaFeatures;

public final class MyNodeConfig {
	public static final int WS_PORT = 8088;

	private static NodeConfigBuilder createNodeConfigBuilder(final boolean redis) {
		final String redisHost = "redis-pic.part.klee.lan.net";
		final String redisPort = "6379";
		final String redisDatabase = "15";

		final NodeConfigBuilder nodeConfigBuilder = NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build());

		final CommonsFeatures commonsFeatures = new CommonsFeatures();
		if (redis) {
			nodeConfigBuilder.addModule(new RedisFeatures()
					.withJedis(
							Param.of("host", redisHost),
							Param.of("port", redisPort),
							Param.of("database", redisDatabase))
					.build());
		}

		nodeConfigBuilder
				.addModule(commonsFeatures.build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures().build())
				.addModule(ModuleConfig.builder("identities")
						.addComponent(MockIdentities.class)
						.build());

		final AccountFeatures accountFeatures = new AccountFeatures()
				.withSecurity(Param.of("userSessionClassName", TestUserSession.class.getName()))
				.withAuthentication()
				.withMockAuthentication()
				.withAccount()
				.withLoaderAccount(
						Param.of("accountLoaderName", "MockIdentities"),
						Param.of("groupLoaderName", "MockIdentities"));

		final SocialFeatures socialFeatures = new SocialFeatures()
				.withComments()
				.withNotifications();

		if (redis) {
			return nodeConfigBuilder
					.addModule(accountFeatures
							.withRedisAccountCache()
							.build())
					.addModule(socialFeatures
							.withRedisNotifications()
							.withRedisComments()
							.build());
		}
		//else we use memory
		return nodeConfigBuilder
				.addModule(accountFeatures
						.withMemoryAccountCache()
						.build())
				.addModule(socialFeatures
						.withMemoryNotifications()
						.withMemoryComments()
						.build());
	}

	public static NodeConfig config(final boolean redis) {
		return createNodeConfigBuilder(redis).build();
	}

	public static NodeConfig vegaConfig() {
		return createNodeConfigBuilder(true)
				.addModule(new JavalinFeatures()
						.withEmbeddedServer(Param.of("port", WS_PORT))
						.build())
				.addModule(new VegaFeatures()
						.withWebServices()
						.withJavalinWebServerPlugin()
						.withWebServicesSecurity()
						.build())
				.addModule(ModuleConfig.builder("ws-account")
						.addComponent(AccountWebServices.class)
						.addComponent(TestLoginWebServices.class)
						.build())
				.addModule(ModuleConfig.builder("ws-notifications")
						.addComponent(NotificationWebServices.class)
						.build())
				.addModule(ModuleConfig.builder("ws-comment")
						.addComponent(CommentWebServices.class)
						.build())
				.build();
		// @formatter:on
	}
}
