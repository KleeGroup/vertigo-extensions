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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.basics.task.TaskEngineProc;
import io.vertigo.basics.task.TaskEngineSelect;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.util.ListBuilder;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.model.Task;
import io.vertigo.datamodel.task.model.TaskResult;
import io.vertigo.datastore.entitystore.data.domain.car.Car;
import io.vertigo.datastore.entitystore.data.domain.car.CarDataBase;
import io.vertigo.datastore.entitystore.data.domain.car.MotorTypeEnum;
import io.vertigo.datastore.entitystore.data.domain.famille.Famille;
import io.vertigo.datastore.entitystore.data.domain.famille.Famille.CarFields;
import io.vertigo.datastore.entitystore.sql.SqlUtil;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public abstract class AbstractStoreManagerTest {
	@Inject
	protected EntityStoreManager entityStoreManager;
	@Inject
	protected VTransactionManager transactionManager;
	@Inject
	protected TaskManager taskManager;

	protected DtDefinition dtDefinitionFamille;
	private DtDefinition dtDefinitionCar;

	private CarDataBase carDataBase;

	private AutoCloseableNode node;

	protected Node getApp() {
		return node;
	}

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		doSetUp();
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			try {
				doTearDown();
			} finally {
				node.close();
			}
		}

	}

	protected abstract NodeConfig buildNodeConfig();

	protected void doSetUp() {
		carDataBase = new CarDataBase();
		dtDefinitionFamille = DtObjectUtil.findDtDefinition(Famille.class);

		dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);

		initMainStore();
	}

	protected void initMainStore() {
		//A chaque test on recrée la table famille
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateMainStoreRequests(),
				"TkInitMain",
				Optional.empty());

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Car car : carDataBase.getAllCars()) {
				car.setId(null);
				entityStoreManager.create(car);
			}
			transaction.commit();
		}
	}

	protected List<String> getCreateMainStoreRequests() {
		return new ListBuilder<String>()
				.addAll(getCreateFamilleRequests())
				.addAll(getCreateCarRequests())
				.addAll(getCreateFileInfoRequests())
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
				" create table car(ID BIGINT, FAM_ID BIGINT, MANUFACTURER varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MTY_CD varchar(50), GEO_POINT TEXT )",
				" create sequence SEQ_CAR start with 10001 increment by 1");
	}

	protected List<String> getCreateFileInfoRequests() {
		return List.of(
				" create table VX_FILE_INFO(FIL_ID BIGINT , FILE_NAME varchar(255), MIME_TYPE varchar(255), LENGTH BIGINT, LAST_MODIFIED date, FILE_PATH varchar(255), FILE_DATA BLOB)",
				" create sequence SEQ_VX_FILE_INFO start with 10001 increment by 1");
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

	protected void doTearDown() {
		cleanDb();
	}

	protected void cleanDb() {
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getDropRequests(),
				"TkShutDown",
				Optional.empty());
	}

	@Test
	public void testSelectCarCachedRowMax() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final DtList<Car> dtc1 = entityStoreManager.find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3));
			Assertions.assertEquals(3, dtc1.size());
			//-----
			final DtList<Car> dtc = entityStoreManager.find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(null));
			Assertions.assertEquals(9, dtc.size());
		}
	}

	@Test
	public void testSelectCarDtListState() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final DtList<Car> dtc1 = entityStoreManager.find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3));
			Assertions.assertEquals(3, dtc1.size());
			//-----
			final DtList<Car> dtc2 = entityStoreManager.find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 2, null, null));
			Assertions.assertEquals(3, dtc2.size());
			Assertions.assertEquals(dtc1.get(2).getId(), dtc2.get(0).getId());
			//-----
			final DtList<Car> dtc3 = entityStoreManager.find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 0, CarFields.manufacturer.name(), false));
			Assertions.assertEquals(3, dtc3.size());
			Assertions.assertEquals("Audi", dtc3.get(0).getManufacturer());
			//-----
			final DtList<Car> dtc4 = entityStoreManager.find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 0, CarFields.manufacturer.name(), true));
			Assertions.assertEquals(3, dtc4.size());
			Assertions.assertEquals("Volkswagen", dtc4.get(0).getManufacturer());
			//-----
			final DtList<Car> dtc5 = entityStoreManager.find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 0, CarFields.price.name(), false));
			Assertions.assertEquals(3, dtc5.size());
			Assertions.assertEquals(2500, dtc5.get(0).getPrice().intValue());
			//-----
			final DtList<Car> dtc6 = entityStoreManager.find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 0, CarFields.price.name(), true));
			Assertions.assertEquals(3, dtc6.size());
			Assertions.assertEquals(109000, dtc6.get(0).getPrice().intValue());
		}
	}

	@Test
	public void testSelectCarAndTestMasterDataEnum() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final DtList<Car> dtcEssence = entityStoreManager.find(
					DtObjectUtil.findDtDefinition(Car.class),
					Criterions.isEqualTo(CarFields.mtyCd, MotorTypeEnum.essence.getEntityUID().getId()),
					DtListState.of(2));
			//---
			Assertions.assertEquals(1, dtcEssence.size());
			Assertions.assertTrue(dtcEssence.get(0).motorType().getEnumValue() == MotorTypeEnum.essence);
		}
	}

	@Test
	public void testSelectCountCars() {
		final TaskDefinition taskDefinition = TaskDefinition.builder("TkCountCars")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select count(*) from CAR")
				.withOutAttribute("count", SmartTypeDefinition.builder("STyCount", BasicType.Long).build(), Cardinality.ONE)
				.build();

		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final Task task = Task.builder(taskDefinition).build();
			final long count = taskManager
					.execute(task)
					.getResult();
			//-----
			Assertions.assertEquals(9, count);
		}
	}

	protected void nativeInsertCar(final Car car) {
		Assertion.check().isNull(car.getId(), "L'id n'est pas null {0}", car.getId());
		//-----
		final DefinitionSpace definitionSpace = node.getDefinitionSpace();
		final SmartTypeDefinition smartTypeCar = definitionSpace.resolve("STyDtCar", SmartTypeDefinition.class);

		final TaskDefinition taskDefinition = TaskDefinition.builder("TkInsertCar")
				.withEngine(TaskEngineProc.class)
				.withRequest("insert into CAR (ID, FAM_ID,MANUFACTURER, MODEL, DESCRIPTION, YEAR, KILO, PRICE, MTY_CD) values "
						+ "(NEXT VALUE FOR SEQ_CAR, #dtoCar.famId#, #dtoCar.manufacturer#, #dtoCar.model#, #dtoCar.description#, #dtoCar.year#, #dtoCar.kilo#, #dtoCar.price#, #dtoCar.mtyCd#)")
				.addInAttribute("dtoCar", smartTypeCar, Cardinality.ONE)
				.build();

		final Task task = Task.builder(taskDefinition)
				.addValue("dtoCar", car)
				.build();
		final TaskResult taskResult = taskManager
				.execute(task);
		nop(taskResult);
	}

	protected void nop(final TaskResult taskResult) {
		//nop
	}

	protected final DtList<Car> nativeLoadCarList() {
		final DefinitionSpace definitionSpace = node.getDefinitionSpace();
		final SmartTypeDefinition smartTypeCar = definitionSpace.resolve("STyDtCar", SmartTypeDefinition.class);

		final TaskDefinition taskDefinition = TaskDefinition.builder("TkLoadAllCars")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select * from CAR")
				.withOutAttribute("dtc", smartTypeCar, Cardinality.MANY)
				.build();

		final Task task = Task.builder(taskDefinition)
				.build();
		return taskManager
				.execute(task)
				.getResult();
	}

	/**
	 * On vérifie que la liste est vide.
	 */
	@Test
	public void testGetFamille() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtList<Famille> dtc = entityStoreManager.find(dtDefinitionFamille, null, DtListState.of(null));
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
			DtList<Famille> dtc = entityStoreManager.find(dtDefinitionFamille, null, DtListState.of(null));
			Assertions.assertEquals(0, dtc.size());
			//-----
			final Famille famille = new Famille();
			famille.setLibelle("encore un");
			final Famille createdFamille = entityStoreManager.create(famille);
			// on attend un objet avec un id non null ?
			Assertions.assertNotNull(createdFamille.getFamId());
			//-----
			dtc = entityStoreManager.find(dtDefinitionFamille, null, DtListState.of(null));
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

	/**
	 * Test que les listes NN ne reste pas en cache après une mise à jour.
	 * Ici l'entité en cache est la destination de la navigation : Car
	 */
	@Test
	public void testGetFamilleLocationCars() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on crée une famille
			final Famille famille = new Famille();
			famille.setLibelle("Ma famille");
			final Famille createdFamille = entityStoreManager.create(famille);

			//on récupère la liste des voitures
			final DtList<Car> cars = entityStoreManager.find(dtDefinitionCar, null, DtListState.of(null));
			Assertions.assertNotNull(cars);
			Assertions.assertFalse(cars.isEmpty(), "La liste des cars est vide");

			//on associe la liste de voiture à la famille en NN
			final List<UID> carUriList = new ArrayList<>();
			for (final Car car : cars) {
				carUriList.add(car.getUID());
			}
			entityStoreManager.getBrokerNN().updateNN(createdFamille.getVoituresLocationDtListURI(), carUriList);

			//On garde le résultat de l'association NN
			final DtList<Car> firstResult = createdFamille.getVoituresLocationList();
			Assertions.assertEquals(cars.size(), firstResult.size(), "Test tailles du nombre de voiture dans une NN");

			//On met à jour l'association en retirant le premier élément
			carUriList.remove(0);
			entityStoreManager.getBrokerNN().updateNN(createdFamille.getVoituresLocationDtListURI(), carUriList);

			//on garde le résultat en lazy : il doit avoir le meme nombre de voiture qu'au début
			final DtList<Car> lazyResult = createdFamille.getVoituresLocationList();
			Assertions.assertEquals(firstResult.size(), lazyResult.size(), "Test tailles du nombre de voiture pour une NN");

			//on recharge la famille et on recharge la liste issus de l'association NN : il doit avoir une voiture de moins qu'au début
			final Famille famille2 = entityStoreManager.readOne(UID.of(Famille.class, createdFamille.getFamId()));
			final DtList<Car> secondResult = famille2.getVoituresLocationList();
			Assertions.assertEquals(firstResult.size() - 1, secondResult.size(), "Test tailles du nombre de voiture dans une NN");
			transaction.commit();
		}
	}

	/**
	 * Test que les listes 1N ne reste pas en cache après une mise à jour.
	 * Ici l'entité en cache est la destination de la navigation : Car
	 */
	@Test
	public void testGetFamilliesCars() {
		//on crée une famille
		final Famille famille = new Famille();
		famille.setLibelle("Ma famille");

		final DtList<Car> firstResult;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Famille createdFamille = entityStoreManager.create(famille);

			//on récupère la liste des voitures
			final DtList<Car> cars = entityStoreManager.find(dtDefinitionCar, null, DtListState.of(null));
			Assertions.assertNotNull(cars);
			Assertions.assertFalse(cars.isEmpty(), "La liste des cars est vide");

			//on associe la liste de voiture à la famille en 1N
			for (final Car car : cars) {
				car.setFamId(createdFamille.getFamId());
				entityStoreManager.update(car);
			}

			//On garde le résultat de l'association 1N
			firstResult = createdFamille.getVoituresFamilleList();

			//On met à jour l'association en retirant le premier élément
			final Car firstCar = cars.get(0);
			firstCar.setFamId(null);
			entityStoreManager.update(firstCar);
			transaction.commit(); //sans commit le cache n'est pas rafraichit
		}

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {

			//on garde le résultat en lazy : il doit avoir le meme nombre de voiture qu'au début
			final DtList<Car> lazyResult = famille.getVoituresFamilleList();
			Assertions.assertEquals(firstResult.size(), lazyResult.size(), "Test tailles du nombre de voiture pour une 1-N");

			//on recharge la famille et on recharge la liste issus de l'association 1N : il doit avoir une voiture de moins qu'au début
			final Famille famille2 = entityStoreManager.readOne(famille.getUID());
			final DtList<Car> secondResult = famille2.getVoituresFamilleList();
			Assertions.assertEquals(firstResult.size() - 1, secondResult.size(), "Test tailles du nombre de voiture pour une 1-N");
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testSelectSmartType() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car peugeotCar = entityStoreManager.readOne(UID.of(Car.class, 10001L));
			Assertions.assertEquals(1, peugeotCar.getGeoPoint().getX(), "Test geopoint smartType adapter");
			Assertions.assertEquals(2, peugeotCar.getGeoPoint().getY(), "Test geopoint smartType adapter");
		}
	}

	@Test
	public void testTxCrudInsertCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			entityStoreManager.create(car);

			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);

			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertNativeSelectRollback() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			entityStoreManager.create(car);

			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertNativeSelectRollback() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);

			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertRollbackCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			entityStoreManager.create(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertRollbackCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxCrudInsertRollbackNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			entityStoreManager.create(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertRollbackNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertCrudInsertCommit() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			final Car car2 = createNewCar();
			nativeInsertCar(car2);
			entityStoreManager.create(car);
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudInsertTwoCommit() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				final Car car = createNewCar();
				entityStoreManager.create(car);
				transaction.commit();
				transaction.commit();
			}
		});
	}

	@Test
	public void testTxCrudInsertCommitCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			entityStoreManager.create(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCommitCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertCommitNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			entityStoreManager.create(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCommitNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testCrudInsertNoTx() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Car car = createNewCar();
			entityStoreManager.create(car);
		});
	}

	@Test
	public void testNativeInsertNoTx() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Car car = createNewCar();
			nativeInsertCar(car);
		});
	}

	@Test
	public void testCrudSelectNoTx() {
		Assertions.assertThrows(NullPointerException.class, () -> checkCrudCarsCount(0));
	}

	@Test
	public void testNativeSelectNoTx() {
		Assertions.assertThrows(NullPointerException.class, () -> checkNativeCarsCount(0));
	}

	@Test
	public void testPerfCrudInsertCrudSelectRollback() {
		final long start = System.currentTimeMillis();
		int execCount = 0;
		while (System.currentTimeMillis() - start < 1000) {
			testTxCrudInsertCrudSelectRollback();
			execCount++;
		}
		final long time = System.currentTimeMillis() - start;
		System.out.println(execCount + " exec en 1s. moy=" + time * 1000 / execCount / 1000d + "ms");
	}

	@Test
	public void testPerfNativeInsertNativeSelectRollback() {
		final long start = System.currentTimeMillis();
		int execCount = 0;
		while (System.currentTimeMillis() - start < 1000) {
			testTxNativeInsertNativeSelectRollback();
			execCount++;
		}
		final long time = System.currentTimeMillis() - start;
		System.out.println(execCount + " exec en 1s. moy=" + time * 1000 / execCount / 1000d + "ms");
	}

	@Test
	public void testCrudCountCars() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final long count = entityStoreManager.count(dtDefinitionCar);
			//-----
			Assertions.assertEquals(9, count);
		}
	}

	@Test
	public void testTxCrudInsertDeleteCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car createdCar = entityStoreManager.create(car);
			//Check cars count
			checkCrudCarsCount(1);
			entityStoreManager.delete(createdCar.getUID());
			checkCrudCarsCount(1); //car is cacheable : list was'nt flush here
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkCrudCarsCount(0); //car is cacheable : must wait commit to see delete
		}
	}

	@Test
	public void testTxCrudInsertCommitCrudDeleteCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			entityStoreManager.create(car);
			checkCrudCarsCount(1);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			entityStoreManager.delete(car.getUID());
			checkCrudCarsCount(0);
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudLockCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car createdCar = entityStoreManager.create(car);
			//Check cars count
			checkCrudCarsCount(1);
			entityStoreManager.readOneForUpdate(createdCar.getUID());
			checkCrudCarsCount(1);
			transaction.commit();
		}
	}

	private void checkNativeCarsCount(final int deltaCount) {
		final DtList<Car> cars = nativeLoadCarList();
		Assertions.assertNotNull(cars);
		Assertions.assertEquals(carDataBase.size() + deltaCount, cars.size(), "Test du nombre de voiture");
	}

	private void checkCrudCarsCount(final int deltaCount) {
		final DtList<Car> cars = entityStoreManager.find(dtDefinitionCar, null, DtListState.of(null));
		Assertions.assertNotNull(cars);
		Assertions.assertEquals(carDataBase.size() + deltaCount, cars.size(), "Test du nombre de voiture");
	}

	private static Car createNewCar() {
		final Car car = new Car();
		car.setId(null);
		car.setPrice(5600);
		car.setManufacturer("Peugeot");
		car.setModel("407");
		car.setYear(2014);
		car.setKilo(20000);
		car.motorType().setEnumValue(MotorTypeEnum.essence);
		car.setDescription("Vds 407 de test, 2014, 20000 kms, rouge, TBEG");
		return car;
	}
}
