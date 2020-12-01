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
package io.vertigo.datafactory.search.withstore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Collections;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.database.sql.SqlManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.impl.search.dsl.DslListFilterBuilder;
import io.vertigo.datafactory.search.SearchManager;
import io.vertigo.datafactory.search.data.domain.Item;
import io.vertigo.datafactory.search.data.domain.ItemDataBase;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.entitystore.EntityStoreManager;

/**
 * Test de l'implémentation standard couplé au store.
 *
 * @author npiedeloup
 */
abstract class AbstractSearchManagerStoreTest {
	@Inject
	private SqlManager dataBaseManager;
	@Inject
	private EntityStoreManager entityStoreManager;
	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private SearchManager searchManager;
	//Index
	private static final String IDX_ITEM = "IdxItem";

	private SearchIndexDefinition itemIndexDefinition;

	private long initialDbItemSize = 0;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() throws SQLException {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		final DefinitionSpace definitionSpace = node.getDefinitionSpace();
		itemIndexDefinition = definitionSpace.resolve(IDX_ITEM, SearchIndexDefinition.class);

		//A chaque test on recrée la table famille
		try (final SqlConnectionCloseable connectionCloseable = new SqlConnectionCloseable(dataBaseManager)) {
			execCallableStatement(connectionCloseable.getConnection(), "create table item(ID BIGINT, MANUFACTURER varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), FAM_ID BIGINT, YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MOTOR_TYPE varchar(50), OPTIONAL_NUMBER BIGINT, OPTIONAL_STRING varchar(50), LOCALISATION varchar(50), LAST_MODIFIED timestamp, PRECISION INT );");
			execCallableStatement(connectionCloseable.getConnection(), "create sequence SEQ_ITEM start with 10001 increment by 1");
		}

		//On supprime tout
		removeAll();

		final ItemDataBase itemDataBase = new ItemDataBase();
		initialDbItemSize = itemDataBase.size();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Item item : itemDataBase.getAllItems()) {
				item.setId(null);
				entityStoreManager.create(item);
			}
			transaction.commit();
		}
		waitAndExpectIndexation(initialDbItemSize);
	}

	@AfterEach
	public final void tearDown() throws SQLException {
		if (node != null) {
			//A chaque fin de test on arréte la base.
			try (final SqlConnectionCloseable connectionCloseable = new SqlConnectionCloseable(dataBaseManager)) {
				execCallableStatement(connectionCloseable.getConnection(), "shutdown;");
			} finally {
				node.close();
			}
		}
	}

	protected abstract NodeConfig buildNodeConfig();

	private void execCallableStatement(final SqlConnection connection, final String sql) throws SQLException {
		dataBaseManager.executeUpdate(
				SqlStatement.builder(sql).build(),
				Collections.emptyMap(),
				connection);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexAllQuery() {
		final long size = query("*:*");
		Assertions.assertEquals(initialDbItemSize, size);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexNewData() {
		testIndexAllQuery();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Item item = createNewItem();
			entityStoreManager.create(item);
			transaction.commit();
		}
		waitAndExpectIndexation(initialDbItemSize + 1);
		Assertions.assertEquals(1, query("description:légende"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexDeleteData() {
		testIndexAllQuery();
		Assertions.assertEquals(1, query("id:10001"));

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			entityStoreManager.delete(createURI(10001L));
			transaction.commit();
		}
		waitAndExpectIndexation(initialDbItemSize - 1);
		Assertions.assertEquals(0, query("id:10001"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexUpdateData() {
		testIndexAllQuery();
		final Item item = createNewItem();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			entityStoreManager.create(item);
			transaction.commit();
		}

		waitAndExpectIndexation(initialDbItemSize + 1);
		Assertions.assertEquals(1, query("description:légende"));

		item.setDescription("Vendue");
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			entityStoreManager.update(item);
			transaction.commit();
		}

		waitAndExpectIndexation(0, "description:légende");
		Assertions.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assertions.assertEquals(1, query("description:vendue"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexDoubleUpdateData() {
		testIndexAllQuery();
		final Item item = createNewItem();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			entityStoreManager.create(item);
			transaction.commit();
		}

		waitAndExpectIndexation(initialDbItemSize + 1);
		Assertions.assertEquals(1, query("description:légende"));

		item.setDescription("Vendue");
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			entityStoreManager.update(item);
			entityStoreManager.update(item);
			entityStoreManager.update(item);
			entityStoreManager.update(item);
			transaction.commit();
		}

		waitAndExpectIndexation(0, "description:légende");
		Assertions.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assertions.assertEquals(1, query("description:vendue"));
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveAll() {
		//On supprime tout
		removeAll();
		final long resize = query("*:*");
		Assertions.assertEquals(0L, resize);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testReIndexAll() {
		testIndexAllQuery();
		removeAll();
		long resize = query("*:*");
		Assertions.assertEquals(0L, resize);

		doReindexAll();
		waitAndExpectIndexation(initialDbItemSize);

		resize = query("*:*");
		Assertions.assertEquals(initialDbItemSize, resize);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 * @throws SQLException if some error in test
	 */
	@Test
	public void testReIndexAllAfterDirectDelete() throws SQLException {
		testIndexAllQuery();

		try (final SqlConnectionCloseable connectionCloseable = new SqlConnectionCloseable(dataBaseManager)) {
			execCallableStatement(connectionCloseable.getConnection(), "delete from item where id = 10001");
			connectionCloseable.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(initialDbItemSize - 1, entityStoreManager.count(itemIndexDefinition.getKeyConceptDtDefinition()));
		}
		doReindexAll();
		waitAndExpectIndexation(initialDbItemSize - 1);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 * @throws SQLException if some error in test
	 */
	@Test
	public void testReIndexAllAfterDirectDeleteAll() throws SQLException {
		testIndexAllQuery();

		try (final SqlConnectionCloseable connectionCloseable = new SqlConnectionCloseable(dataBaseManager)) {
			execCallableStatement(connectionCloseable.getConnection(), "delete from item ");
			connectionCloseable.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(0, entityStoreManager.count(itemIndexDefinition.getKeyConceptDtDefinition()));
		}
		doReindexAll();
		waitAndExpectIndexation(0);
	}

	private static Item createNewItem() {
		final Item item = new Item();
		item.setId(null);
		item.setPrice(12000);
		item.setManufacturer("Acme");
		item.setModel("Martin");
		item.setYear(1978);
		item.setKilo(1500);
		final BigDecimal conso = new BigDecimal(7.6);
		conso.setScale(2, RoundingMode.HALF_UP);
		item.setConsommation(conso);
		item.setMotorType("essence");
		item.setDescription("Voiture de légende assurant une reindexation dès son insertion");
		return item;
	}

	private long query(final String query) {
		//recherche
		final SearchQuery searchQuery = SearchQuery.builder(query, DslListFilterBuilder.class)
				.withCriteria("")
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private FacetedQueryResult<Item, SearchQuery> doQuery(final SearchQuery searchQuery, final DtListState listState) {
		return searchManager.loadList(itemIndexDefinition, searchQuery, listState);
	}

	private static UID createURI(final long itemId) {
		return UID.of(Item.class, itemId);
	}

	private void doRemove(final String query) {
		final ListFilter removeQuery = ListFilter.of(query);
		searchManager.removeAll(itemIndexDefinition, removeQuery);
	}

	private void doReindexAll() {
		searchManager.reindexAll(itemIndexDefinition);
	}

	private void removeAll() {
		doRemove("*:*");
		waitAndExpectIndexation(0);
	}

	private void waitAndExpectIndexation(final long expectedCount) {
		waitAndExpectIndexation(expectedCount, "*:*");
	}

	private void waitAndExpectIndexation(final long expectedCount, final String queryStr) {
		final long time = System.currentTimeMillis();
		long size = -1;
		try {
			do {
				Thread.sleep(250); //wait index was done

				size = query(queryStr);
				if (size == expectedCount) {
					break; //si le nombre est atteint on sort.
				}

			} while (System.currentTimeMillis() - time < 15000);//timeout 15s (batch 10s + reindex 5s)
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt(); //si interrupt on relance
		}
		Assertions.assertEquals(expectedCount, size);
	}

	private class SqlConnectionCloseable implements AutoCloseable {
		private final SqlConnection connection;

		SqlConnectionCloseable(final SqlManager dataBaseManager) {
			connection = dataBaseManager.getConnectionProvider(SqlManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
		}

		SqlConnection getConnection() {
			return connection;
		}

		public void commit() throws SQLException {
			connection.commit();
		}

		@Override
		public void close() throws SQLException {
			connection.close();
		}
	}

}
