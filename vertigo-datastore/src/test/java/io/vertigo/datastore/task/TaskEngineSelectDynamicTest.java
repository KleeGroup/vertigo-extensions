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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.task.data.TestSmartTypes;
import io.vertigo.datastore.task.data.domain.SuperHero;
import io.vertigo.datastore.task.data.domain.SuperHeroDataBase;

/**
 *
 * @author npiedeloup
 */
public final class TaskEngineSelectDynamicTest {
	private static final String DTC_SUPER_HERO_IN = "dtcSuperHeroIn";
	private static final String SUPER_HERO_ID_LIST = "superHeroIdList";
	private static final String STY_INTEGER = "STyInteger";
	private static final String STY_ID = "STyId";
	private static final String STY_DT_SUPER_HERO = "STyDtSuperHero";
	private static final String DTO_SUPER_HERO = "dtoSuperHero";
	@Inject
	private TaskManager taskManager;
	@Inject
	private EntityStoreManager entityStoreManager;
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
		superHeroDataBase.populateSuperHero(entityStoreManager, 10);
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
	 * Test de double exécution d'une tache.
	 */
	@Test
	public void testScript() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TkScriptTest",
					"select * from SUPER_HERO <%if(false) {%>where id = #" + DTO_SUPER_HERO + ".id#<%}%>");

			final SuperHero superHero = createSuperHero(10001L + 1);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTO_SUPER_HERO, superHero)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test des scripts.
	 */
	@Test
	public void testScriptVar() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TkScriptTest",
					"select * from SUPER_HERO <%if(dtoSuperHero.getId() == 10002L) {%>where id = #" + DTO_SUPER_HERO + ".id#<%}%>");

			final SuperHero superHero = new SuperHero();
			superHero.setId(10001L + 1);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTO_SUPER_HERO, superHero)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(1, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
		}
	}

	/**
	 * Test des nullable.
	 */
	@Test
	public void testNullable() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskWithNullableIn("TkNullableTest",
					"select * from SUPER_HERO where id = #param1#<%if(param2!=null) {%> OR id = #param2#+2 <%}%><%if(param3!=null) {%> OR id = #param3#+3<%}%>");

			final Task task = Task.builder(taskDefinition)
					.addValue("param1", 10002)
					.addValue("param2", null)
					.addValue("param3", 10002)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10002L, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10002L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test des scripts.
	 */
	@Test
	public void testScriptVarList() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkScriptTest",
					"select * from SUPER_HERO <%if(!dtcSuperHeroIn.isEmpty()) {%>where id in (#" + DTC_SUPER_HERO_IN + ".rownum.id#)<%}%>");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test du preprocessor trim.
	 * Note: nous n'avons pas accès à la chaine trimée, on check juste que la requete est valide.
	 */
	@Test
	public void testTrim() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TkScriptTest",
					"select * from SUPER_HERO  \n<%if(false) {%>\nwhere id = #" + DTO_SUPER_HERO + ".id#\n<%}%>\n");

			final SuperHero superHero = new SuperHero();
			superHero.setId(10001L + 1);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTO_SUPER_HERO, superHero)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();
			Assertions.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereIn() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkWhereIdTest",
					"select * from SUPER_HERO  where id in (#" + DTC_SUPER_HERO_IN + ".rownum.id#)");

			final DtList<SuperHero> ids = DtList.of(createSuperHero(10001L + 1), createSuperHero(10001L + 3));

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInPrimitive() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskListPrimitive("TkWhereInPrimitiveTest",
					"select * from SUPER_HERO  where id in (#" + SUPER_HERO_ID_LIST + ".rownum#)");

			final List<Long> ids = Arrays.asList(10001L + 1, 10001L + 3);

			final Task task = Task.builder(taskDefinition)
					.addValue(SUPER_HERO_ID_LIST, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInTab() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkWhereIdTest",
					"select * from SUPER_HERO  where\tID in\t(#" + DTC_SUPER_HERO_IN + ".rownum.id#)");

			final DtList<SuperHero> ids = DtList.of(createSuperHero(10001L + 1), createSuperHero(10001L + 3));

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInParenthesis() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkWhereIdTest",
					"select * from SUPER_HERO  where\t(id in\t(#" + DTC_SUPER_HERO_IN + ".rownum.id#))");

			final DtList<SuperHero> ids = DtList.of(createSuperHero(10001L + 1), createSuperHero(10001L + 3));

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInEmpty() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkWhereIdTest",
					"select * from SUPER_HERO where id in (#" + DTC_SUPER_HERO_IN + ".rownum.id#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(0, resultList.size());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereNotIn() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkWhereIdTest",
					"select * from SUPER_HERO where id not in (#" + DTC_SUPER_HERO_IN + ".rownum.id#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids).build();

			ids.add(createSuperHero(10001L + 1));
			ids.add(createSuperHero(10001L + 3));
			ids.add(createSuperHero(10001L + 5));
			ids.add(createSuperHero(10001L + 6));
			ids.add(createSuperHero(10001L + 7));
			ids.add(createSuperHero(10001L + 8));

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(4, resultList.size());
			Assertions.assertEquals(10001L + 0, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 2, resultList.get(1).getId().longValue());
			Assertions.assertEquals(10001L + 4, resultList.get(2).getId().longValue());
			Assertions.assertEquals(10001L + 9, resultList.get(3).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereNotInEmpty() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkWhereIdTest",
					"select * from SUPER_HERO where id not in (#" + DTC_SUPER_HERO_IN + ".rownum.id#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test where in avec 2200 Id a inclure.
	 */
	@Test
	public void testWhereIn2200() {
		superHeroDataBase.populateSuperHero(entityStoreManager, 4500);
		//---

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkWhereIdTest",
					"select * from SUPER_HERO  where id in (#" + DTC_SUPER_HERO_IN + ".rownum.id#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			for (int i = 0; i < 2200; i++) {
				ids.add(createSuperHero(10001L + 2 * i));
			}

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2200, resultList.size());
		}
	}

	/**
	 * Test where in avec 2200 Id a exclure.
	 */
	@Test
	public void testWhereNotIn2200() {
		superHeroDataBase.populateSuperHero(entityStoreManager, 4500);
		//---

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TkWhereIdTest",
					"select * from SUPER_HERO  where id not in (#" + DTC_SUPER_HERO_IN + ".rownum.id#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			for (int i = 0; i < 2200; i++) {
				ids.add(createSuperHero(10001L + 2 * i));
			}

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(10 + 4500 - 2200, resultList.size());
		}
	}

	private static SuperHero createSuperHero(final long id) {
		final SuperHero superHero = new SuperHero();
		superHero.setId(id);
		return superHero;
	}

	private TaskDefinition registerTaskWithNullableIn(final String taskDefinitionName, final String params) {
		final SmartTypeDefinition smartTypeInteger = node.getDefinitionSpace().resolve(STY_INTEGER, SmartTypeDefinition.class);
		final SmartTypeDefinition smartTypeSuperHeroe = node.getDefinitionSpace().resolve(STY_DT_SUPER_HERO, SmartTypeDefinition.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.addInAttribute("param1", smartTypeInteger, Cardinality.OPTIONAL_OR_NULLABLE)
				.addInAttribute("param2", smartTypeInteger, Cardinality.OPTIONAL_OR_NULLABLE)
				.addInAttribute("param3", smartTypeInteger, Cardinality.OPTIONAL_OR_NULLABLE)
				.withOutAttribute("dtc", smartTypeSuperHeroe, Cardinality.MANY)
				.build();
	}

	private TaskDefinition registerTaskObject(final String taskDefinitionName, final String params) {
		final SmartTypeDefinition smartTypeSupeHero = node.getDefinitionSpace().resolve(STY_DT_SUPER_HERO, SmartTypeDefinition.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.addInAttribute(DTO_SUPER_HERO, smartTypeSupeHero, Cardinality.ONE)
				.withOutAttribute("dtc", smartTypeSupeHero, Cardinality.MANY)
				.build();
	}

	private TaskDefinition registerTaskList(final String taskDefinitionName, final String params) {
		final SmartTypeDefinition smartTypeSupeHeroe = node.getDefinitionSpace().resolve(STY_DT_SUPER_HERO, SmartTypeDefinition.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.addInAttribute(DTC_SUPER_HERO_IN, smartTypeSupeHeroe, Cardinality.MANY)
				.withOutAttribute("dtc", smartTypeSupeHeroe, Cardinality.MANY)
				.build();
	}

	private TaskDefinition registerTaskListPrimitive(final String taskDefinitionName, final String params) {
		final SmartTypeDefinition smartTypeLong = node.getDefinitionSpace().resolve(STY_ID, SmartTypeDefinition.class);
		final SmartTypeDefinition smartTypeSupeHeroe = node.getDefinitionSpace().resolve(STY_DT_SUPER_HERO, SmartTypeDefinition.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.addInAttribute(SUPER_HERO_ID_LIST, smartTypeLong, Cardinality.MANY)
				.withOutAttribute("dtc", smartTypeSupeHeroe, Cardinality.MANY)
				.build();
	}

}
