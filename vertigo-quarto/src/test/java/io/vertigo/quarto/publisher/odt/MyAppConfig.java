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
package io.vertigo.quarto.publisher.odt;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.quarto.impl.publisher.PublisherManagerImpl;
import io.vertigo.quarto.plugins.publisher.odt.OpenOfficeMergerPlugin;
import io.vertigo.quarto.publisher.PublisherManager;
import io.vertigo.quarto.publisher.TestPublisherDefinitionProvider;
import io.vertigo.quarto.publisher.data.TestPublisherSmartTypes;

class MyNodeConfig {

	public static NodeConfig config() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures()
						.withScript()
						.withJaninoScript()
						.build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(PublisherManager.class, PublisherManagerImpl.class)
						.addPlugin(OpenOfficeMergerPlugin.class)
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestPublisherSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", "io.vertigo.quarto.publisher.data.domain*")
								.build())
						.addDefinitionProvider(TestPublisherDefinitionProvider.class)
						.build())
				.build();

	}

}
