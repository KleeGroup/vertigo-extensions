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
package io.vertigo.datastore.entitystore.sql.vendor.h2;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.h2.Driver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.entitystore.data.domain.car.Car;
import io.vertigo.datastore.entitystore.data.domain.car.CarDataBase;
import io.vertigo.datastore.entitystore.data.domain.famille.Famille.CarFields;
import io.vertigo.datastore.entitystore.sql.SqlDataStoreNodeConfig;
import io.vertigo.datastore.entitystore.sql.SqlUtil;

/**
 *
 */
public final class SqlCriteriaTest {

	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private EntityStoreManager entityStoreManager;
	@Inject
	private TaskManager taskManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		//A chaque test on recrée la table famille
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateCarRequests(),
				"TkInitMain",
				Optional.empty());
		final CarDataBase carDataBase = new CarDataBase();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Car car : carDataBase.getAllCars()) {
				car.setId(null);
				entityStoreManager.create(car);
			}
			transaction.commit();
		}
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return SqlDataStoreNodeConfig.build(
				H2DataBase.class.getCanonicalName(),
				Driver.class.getCanonicalName(),
				"jdbc:h2:mem:database");
	}

	protected List<String> getCreateCarRequests() {
		return List.of(
				" create table motor_type(MTY_CD varchar(50) , LABEL varchar(255))",
				"insert into motor_type(MTY_CD, LABEL) values ('ESSENCE', 'Essence')",
				"insert into motor_type(MTY_CD, LABEL) values ('DIESEL', 'Diesel')",
				" create table car(ID BIGINT, FAM_ID BIGINT, MANUFACTURER varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MTY_CD varchar(50), GEO_POINT TEXT )",
				" create sequence SEQ_CAR start with 10001 increment by 1");
	}

	@Test
	public void assertCriteria() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final long count = entityStoreManager.find(dtDefinitionCar, Criterions.isEqualTo(CarFields.model, "passat"), DtListState.of(null)).size();
			Assertions.assertEquals(1, count);
		}
	}

}
