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
package io.vertigo.datastore.entitystore;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.core.util.ListBuilder;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.entitystore.data.OtherStoreDtDefinitions;
import io.vertigo.datastore.entitystore.data.TestSmartTypes;
import io.vertigo.datastore.entitystore.data.domain.otherstore.Famille;
import io.vertigo.datastore.entitystore.sql.SqlUtil;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public final class MultiStoreManagerTest {

	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private TaskManager taskManager;
	@Inject
	private EntityStoreManager entityStoreManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		//A chaque test on recrée la table famille dans l'autre base
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateMainStoreRequests(),
				"TkInit",
				Optional.empty());
		//A chaque test on recrée la table famille dans l'autre base
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateFamilleRequests(),
				"TkInitOther",
				Optional.of("otherStore"));

	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			try {
				SqlUtil.execRequests(
						transactionManager,
						taskManager,
						getDropRequests(),
						"TkShutDown",
						Optional.empty());
			} finally {
				node.close();
			}
		}
	}

	private NodeConfig buildNodeConfig() {
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
						.withC3p0(
								Param.of("name", "otherBase"),
								Param.of("dataBaseClass", H2DataBase.class.getName()),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:database2"))
						.build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withMemoryCache()
						.withEntityStore()
						.withSqlEntityStore()
						.withSqlEntityStore(
								Param.of("dataSpace", "otherStore"),
								Param.of("connectionName", "otherBase"))
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", OtherStoreDtDefinitions.class.getName())
								.build())
						.addDefinitionProvider(StoreCacheDefinitionProvider.class)
						.build())
				.build();
	}

	protected List<String> getCreateMainStoreRequests() {
		return new ListBuilder<String>()
				.addAll(getCreateFamilleRequests())
				.addAll(getCreateCarRequests())
				.build();
	}

	protected List<String> getCreateFamilleRequests() {
		return List.of(
				" create table famille(FAM_ID BIGINT , LIBELLE varchar(255))",
				" create sequence SEQ_FAMILLE start with 10001 increment by 1");
	}

	protected List<String> getCreateCarRequests() {
		return List.of(
				" create table fam_car_location(FAM_ID BIGINT, ID BIGINT)",
				" create table motor_type(MTY_CD varchar(50) , LABEL varchar(255))",
				"insert into motor_type(MTY_CD, LABEL) values ('ESSENCE', 'Essence')",
				"insert into motor_type(MTY_CD, LABEL) values ('DIESEL', 'Diesel')",
				" create table car(ID BIGINT, FAM_ID BIGINT, MANUFACTURER varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MTY_CD varchar(50) )",
				" create sequence SEQ_CAR start with 10001 increment by 1");
	}

	protected List<String> getDropRequests() {
		return List.of(
				" drop table if exists VX_FILE_INFO ",
				" drop sequence if exists SEQ_VX_FILE_INFO",
				" drop table if exists fam_car_location",
				" drop table if exists car",
				" drop table if exists motor_type",
				" drop sequence if exists SEQ_CAR",
				" drop table if exists famille",
				" drop sequence if exists SEQ_FAMILLE");
	}

	/**
	 * On vérifie que la liste est vide.
	 */
	@Test
	public void testGetFamille() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtList<Famille> dtc = entityStoreManager.find(DtObjectUtil.findDtDefinition(Famille.class), null, DtListState.of(null));
			Assertions.assertNotNull(dtc);
			Assertions.assertTrue(dtc.isEmpty(), "La liste des famille est vide");
			transaction.commit();
		}
	}

	/**
	 * On charge une liste, ajoute un element et recharge la liste pour verifier l'ajout.
	 */
	@Test
	public void testAddFamille() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			DtList<Famille> dtc = entityStoreManager.find(DtObjectUtil.findDtDefinition(Famille.class), null, DtListState.of(null));
			Assertions.assertEquals(0, dtc.size());
			//-----
			final Famille famille = new Famille();
			famille.setLibelle("encore un");
			final Famille createdFamille = entityStoreManager.create(famille);
			// on attend un objet avec un id non null ?
			Assertions.assertNotNull(createdFamille.getFamId());
			//-----
			dtc = entityStoreManager.find(DtObjectUtil.findDtDefinition(Famille.class), null, DtListState.of(null));
			Assertions.assertEquals(1, dtc.size());
			transaction.commit();
		}
	}

	/**
	 * on vérifier l'exception levée si une contrainte bdd n'est pas respecté.
	 */
	@Test
	public void testCreateFamilleFail() {
		Assertions.assertThrows(Exception.class, () -> {
			try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				final DecimalFormat df = new DecimalFormat("000000000:");
				//-----
				final Famille famille = new Famille();
				final StringBuilder sb = new StringBuilder();
				for (int i = 0; i < 4000; i++) {
					sb.append(df.format(i));
				}
				// libelle
				famille.setLibelle(sb.toString());
				//On doit échouer car le libellé est trop long
				entityStoreManager.create(famille);
				Assertions.fail();
			}
		});
	}

}
