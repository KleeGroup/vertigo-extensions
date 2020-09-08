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
package io.vertigo.datastore.entitystore.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.entitystore.AbstractStoreManagerTest;
import io.vertigo.datastore.entitystore.data.DtDefinitions;
import io.vertigo.datastore.entitystore.data.TestSmartTypes;
import io.vertigo.datastore.entitystore.data.domain.famille.Famille;

/**
 * Test de l'implémentation avec cache.
 *
 * @author pchretien
 */
public final class CachedStoreManagerTest extends AbstractStoreManagerTest {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures()
						.withScript()
						.withJaninoScript()
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.withC3p0(
								Param.of("dataBaseClass", H2DataBase.class.getName()),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:database"))
						.build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withMemoryCache()
						.withEntityStore()
						.withSqlEntityStore()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
								.build())
						.addDefinitionProvider(TestCacheStoreManagerDefinitionProvider.class)
						.build())
				.build();
	}

	/**
	 * On charge une liste, ajoute un element et recharge la liste pour verifier l'ajout.
	 */
	@Override
	@Test
	public void testAddFamille() {
		//ce test est modifier car le cache n'est pas transactionnel : la liste n'est pas accessible sans commit
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtList<Famille> dtc = entityStoreManager.find(dtDefinitionFamille, null, DtListState.of(null));
			Assertions.assertEquals(0, dtc.size());
			//-----
			final Famille famille = new Famille();
			famille.setLibelle("encore un");
			final Famille createdFamille = entityStoreManager.create(famille);
			// on attend un objet avec un ID non null ?
			Assertions.assertNotNull(createdFamille.getFamId());
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtList<Famille> dtc = entityStoreManager.find(dtDefinitionFamille, null, DtListState.of(null));
			Assertions.assertEquals(1, dtc.size());

		}
	}
}
