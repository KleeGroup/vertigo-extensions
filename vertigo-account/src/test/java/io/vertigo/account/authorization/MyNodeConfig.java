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
package io.vertigo.account.authorization;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.authorization.model.FullSecuredServices;
import io.vertigo.account.authorization.model.PartialSecuredServices;
import io.vertigo.account.data.TestSmartTypes;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.data.model.DtDefinitions;
import io.vertigo.account.plugins.authorization.loaders.JsonSecurityDefinitionProvider;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;

public final class MyNodeConfig {

	public static NodeConfig config() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new AccountFeatures()
						.withSecurity(
								Param.of("userSessionClassName", TestUserSession.class.getName()))
						.withAuthorization()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(
								DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
										.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
										.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
										.build())
						.addDefinitionProvider(DefinitionProviderConfig.builder(JsonSecurityDefinitionProvider.class)
								.addDefinitionResource("security", "io/vertigo/account/authorization/advanced-auth-config-v2.json")
								.build())
						.addDefinitionProvider(TestSecurityDefinitionProvider.class)
						.addComponent(FullSecuredServices.class)
						.addComponent(PartialSecuredServices.class)
						.build())

				.build();
	}
}
