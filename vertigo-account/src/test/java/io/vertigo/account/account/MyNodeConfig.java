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
package io.vertigo.account.account;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.data.TestSmartTypes;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.data.model.DtDefinitions;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.connectors.redis.RedisFeatures;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.NodeConfigBuilder;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datastore.DataStoreFeatures;

public final class MyNodeConfig {
	private static final String REDIS_HOST = "redis-pic.part.klee.lan.net";
	private static final int REDIS_PORT = 6379;
	private static final int REDIS_DATABASE = 15;

	public static NodeConfig config(final boolean redis, final boolean database) {
		final NodeConfigBuilder nodeConfigBuilder = NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build());

		if (redis) {
			nodeConfigBuilder.addModule(new RedisFeatures()
					.withJedis(
							Param.of("host", REDIS_HOST),
							Param.of("port", REDIS_PORT),
							Param.of("database", REDIS_DATABASE))
					.build());
		}

		final DatabaseFeatures databaseFeatures = new DatabaseFeatures();
		final DataStoreFeatures datastoreFeatures = new DataStoreFeatures()
				.withFileStore();
		final AccountFeatures accountFeatures = new AccountFeatures()
				.withSecurity(Param.of("userSessionClassName", TestUserSession.class.getName()))
				.withAccount();

		if (database) {
			databaseFeatures
					.withSqlDataBase()
					.withC3p0(
							Param.of("dataBaseClass", H2DataBase.class.getName()),
							Param.of("jdbcDriver", "org.h2.Driver"),
							Param.of("jdbcUrl", "jdbc:h2:mem:database"));

			datastoreFeatures
					.withEntityStore()
					.withSqlEntityStore();

			accountFeatures.withStoreAccount(
					Param.of("userIdentityEntity", "DtUser"),
					Param.of("groupIdentityEntity", "DtUserGroup"),
					Param.of("userAuthField", "email"),
					Param.of("userToAccountMapping", "id:usrId, displayName:fullName, email:email, authToken:email"),
					Param.of("groupToGroupAccountMapping", "id:grpId, displayName:name"));
		} else {
			accountFeatures.withTextAccount(
					Param.of("accountFilePath", "io/vertigo/account/data/identities.txt"),
					Param.of("accountFilePattern", "^(?<id>[^;]+);(?<displayName>[^;]+);(?<email>(?<authToken>[^;@]+)@[^;]+);(?<photoUrl>.*)$"),
					Param.of("groupFilePath", "io/vertigo/account/data/groups.txt"),
					Param.of("groupFilePattern", "^(?<id>[^;]+);(?<displayName>[^;]+);(?<accountIds>.*)$"));
		}

		if (redis) {
			accountFeatures.withRedisAccountCache();
		} else {
			//else we use memory
			accountFeatures.withMemoryAccountCache();
		}
		return nodeConfigBuilder
				.addModule(new CommonsFeatures()
						.withScript()
						.withJaninoScript()
						.build())
				.addModule(databaseFeatures.build())
				.addModule(new DataModelFeatures().build())
				.addModule(datastoreFeatures
						.withCache()
						.withMemoryCache()
						.build())
				.addModule(accountFeatures.build())
				.addModule(ModuleConfig.builder("app")
						.addDefinitionProvider(
								DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
										.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
										.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
										.build())
						.build())
				.build();
	}

}
