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
package io.vertigo.datamodel.smarttype;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.smarttype.data.FormatterTest;
import io.vertigo.datamodel.smarttype.data.TestOverride;
import io.vertigo.datamodel.smarttype.data.TestSmartTypes;
import io.vertigo.datamodel.smarttype.data.domain.Base;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;

public class EdgeCasesSmartTypeDefinitionProviderTest {

	@Test
	public void testObjectSmartTypeOverride() {
		//Overide infered smartType
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(new DataModelFeatures().build())
				.addModule(ModuleConfig.builder("myModule")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getCanonicalName())
								.addDefinitionResource("smarttypes", TestOverride.class.getCanonicalName())
								.addDefinitionResource("dtobjects", Base.class.getPackage().getName() + "*")
								.build())
						.build())
				.build();
		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			final SmartTypeDefinition dtBaseSmartType = node.getDefinitionSpace().resolve("STyDtBase", SmartTypeDefinition.class);
			Assertions.assertEquals(FormatterTest.class, dtBaseSmartType.getFormatterConfig().getFormatterClass());
		}
	}

	@Test
	public void testDuplicateSmartType() {
		//Overide infered smartType
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(new DataModelFeatures().build())
				.addModule(ModuleConfig.builder("myModule")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getCanonicalName())
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getCanonicalName())
								.addDefinitionResource("smarttypes", TestOverride.class.getCanonicalName())
								.addDefinitionResource("dtobjects", Base.class.getPackage().getName() + "*")
								.build())
						.build())
				.build();

		Assertions.assertThrows(IllegalStateException.class, () -> {
			try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
				final SmartTypeDefinition dtBaseSmartType = node.getDefinitionSpace().resolve("STyDtBase", SmartTypeDefinition.class);
				Assertions.assertEquals(FormatterTest.class, dtBaseSmartType.getFormatterConfig().getFormatterClass());
			}
		});
	}

}
