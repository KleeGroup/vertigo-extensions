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
package io.vertigo.datastore.task;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.basics.task.TaskEngineProcBatch;
import io.vertigo.basics.task.TaskEngineSelect;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
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
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.model.Task;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.task.data.TestSmartTypes;
import io.vertigo.datastore.task.data.domain.SuperHero;
import io.vertigo.datastore.task.data.domain.SuperHeroDataBase;

/**
 *	TaskEngineProcBatch tests
 *
 * @author dszniten
 */
public final class TaskEngineProcBatchTest {
	private static final String DTC_SUPER_HERO_IN = "dtcSuperHeroIn";
	private static final String SUPER_HERO_ID_LIST_IN = "superHeroIdListIn";
	private static final String DTC_SUPER_HERO_OUT = "dtcSuperHeroOut";
	private static final String OTHER_PARAM_IN = "otherParam";
	private static final String STY_DT_SUPER_HERO = "STyDtSuperHero";
	private static final String STY_ID = "STyId";
	private static final String STY_STRING = "STyString";

	@Inject
	private TaskManager taskManager;
	@Inject
	private VTransactionManager transactionManager;

	private SuperHeroDataBase superHeroDataBase;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() throws Exception {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		superHeroDataBase = new SuperHeroDataBase(transactionManager, taskManager);
		superHeroDataBase.createDataBase();
	}

	@AfterEach
	public final void tearDown() throws Exception {
		if (node != null) {
			node.close();
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
								.addDefinitionResource("dtobjects", "io.vertigo.datastore.task.data.DtDefinitions")
								.build())
						.build())
				.build();
	}

	/**
	 * Tests batch insertion with a task
	 */
	@Test
	public void testInsertBatch() {
		final String request = new StringBuilder("insert into SUPER_HERO(id, NAME) values (")
				.append("#").append(DTC_SUPER_HERO_IN + ".id").append("# , ")
				.append("#").append(DTC_SUPER_HERO_IN + ".name").append("# ) ")
				.toString();
		final TaskDefinition taskDefinition = TaskDefinition.builder("TkTestInsertBatch")
				.withEngine(TaskEngineProcBatch.class)
				.addInAttribute(DTC_SUPER_HERO_IN, node.getDefinitionSpace().resolve(STY_DT_SUPER_HERO, SmartTypeDefinition.class), Cardinality.MANY)
				.withRequest(request)
				.build();

		final DtList<SuperHero> superHeroes = SuperHeroDataBase.getSuperHeroes();

		final Task task = Task.builder(taskDefinition)
				.addValue(DTC_SUPER_HERO_IN, superHeroes)
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			taskManager.execute(task);
			transaction.commit();
		}

		Assertions.assertEquals(superHeroes.size(), selectHeroes().size());

	}

	/**
	 * Tests batch insertion with a task
	 */
	@Test
	public void testInsertBatchWithAdditionalParam() {
		final String request = new StringBuilder("insert into SUPER_HERO(id, NAME) values (")
				.append("#").append(DTC_SUPER_HERO_IN + ".id").append("# , ")
				.append("#").append(OTHER_PARAM_IN).append("# ) ")
				.toString();
		final TaskDefinition taskDefinition = TaskDefinition.builder("TkTestInsertBatch")
				.withEngine(TaskEngineProcBatch.class)
				.addInAttribute(DTC_SUPER_HERO_IN, node.getDefinitionSpace().resolve(STY_DT_SUPER_HERO, SmartTypeDefinition.class), Cardinality.MANY)
				.addInAttribute(OTHER_PARAM_IN, node.getDefinitionSpace().resolve(STY_STRING, SmartTypeDefinition.class), Cardinality.ONE)
				.withRequest(request)
				.build();

		final DtList<SuperHero> superHeroes = SuperHeroDataBase.getSuperHeroes();

		final Task task = Task.builder(taskDefinition)
				.addValue(DTC_SUPER_HERO_IN, superHeroes)
				.addValue(OTHER_PARAM_IN, "test")
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			taskManager.execute(task);
			transaction.commit();
		}

		Assertions.assertEquals(superHeroes.size(), selectHeroes().size());

	}

	/**
	 * Tests batch insertion with a task
	 */
	@Test
	public void testInsertBatchPrimitive() {
		final String request = new StringBuilder("insert into SUPER_HERO(id, NAME) values (")
				.append("#").append(SUPER_HERO_ID_LIST_IN).append("# , 'test' ").append(" )")
				.toString();
		final TaskDefinition taskDefinition = TaskDefinition.builder("TkTestInsertBatch")
				.withEngine(TaskEngineProcBatch.class)
				.addInAttribute(SUPER_HERO_ID_LIST_IN, node.getDefinitionSpace().resolve(STY_ID, SmartTypeDefinition.class), Cardinality.MANY)
				.withRequest(request)
				.build();

		final List<Long> superHeroesIds = SuperHeroDataBase.getSuperHeroes().stream().map(superHero -> superHero.getId()).collect(Collectors.toList());

		final Task task = Task.builder(taskDefinition)
				.addValue(SUPER_HERO_ID_LIST_IN, superHeroesIds)
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			taskManager.execute(task);
			transaction.commit();
		}

		Assertions.assertEquals(superHeroesIds.size(), selectHeroes().size());

	}

	private DtList<SuperHero> selectHeroes() {
		final TaskDefinition taskDefinition = TaskDefinition.builder("TkSelectHeroes")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select * from SUPER_HERO")
				.withOutAttribute(DTC_SUPER_HERO_OUT, node.getDefinitionSpace().resolve(STY_DT_SUPER_HERO, SmartTypeDefinition.class), Cardinality.MANY)
				.build();
		final Task task = Task.builder(taskDefinition).build();
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			return taskManager.execute(task).getResult();
		}
	}

}
