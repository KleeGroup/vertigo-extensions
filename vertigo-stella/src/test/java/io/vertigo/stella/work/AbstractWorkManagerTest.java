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
package io.vertigo.stella.work;

import java.util.function.Function;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.stella.master.MasterManager;
import io.vertigo.stella.master.WorkResultHandler;
import io.vertigo.stella.work.mock.DivideWork;
import io.vertigo.stella.work.mock.DivideWorkEngine;
import io.vertigo.stella.work.mock.SlowWork;
import io.vertigo.stella.work.mock.SlowWorkEngine;
import io.vertigo.stella.work.mock.ThreadLocalWork;
import io.vertigo.stella.work.mock.ThreadLocalWorkEngine;

/**
 * @author pchretien
 */
public abstract class AbstractWorkManagerTest {
	private final long warmupTime = 10000; //en fonction du mode de distribution la prise en compte d'une tache est plus ou moins longue. Pour les TU on estime à 2s
	private static final int WORKER_COUNT = 5; //Doit correspondre au workerCount déclaré dans managers.xlm

	@Inject
	private MasterManager workManager;
	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() throws Exception {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		doSetUp();
	}

	protected void doSetUp() throws Exception {
		//
	}

	@AfterEach
	public final void tearDown() throws Exception {
		if (node != null) {
			try {
				doTearDown();
			} finally {
				node.close();
			}
		}
	}

	protected void doTearDown() throws Exception {
		//
	}

	protected abstract NodeConfig buildNodeConfig();

	protected final void nop(final Object o) {
		//nada
	}

	//=========================================================================
	//===========================PROCESS=======================================
	//=========================================================================
	@Test
	public void testProcess() {
		final DivideWork work = new DivideWork(10, 5);
		final long div = workManager.process(work, DivideWorkEngine.class).join();
		Assertions.assertEquals(10L / 5L, div);
	}

	@Test
	public void testProcessor() {
		//	final DivideWork work = new DivideWork(10, 5);
		final Function<Long, Long> square = work -> workManager.process(work, SquareWorkEngine.class).join();

		final Function<String, Long> length = work -> workManager.process(work, LengthWorkEngine.class).join();

		final long result = length
				.andThen(square)
				.andThen(square)
				.apply("aa");
		Assertions.assertEquals(2 * 2 * 2 * 2L, result);
	}

	public static final class LengthWorkEngine implements WorkEngine<String, Long> {
		/** {@inheritDoc} */
		@Override
		public Long process(final String work) {
			return work.length() * 1L;
		}
	}

	public static final class SquareWorkEngine implements WorkEngine<Long, Long> {
		/** {@inheritDoc} */
		@Override
		public Long process(final Long work) {
			return work.longValue() * work.longValue();
		}
	}

	@Test
	public void testProcessWithNull() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final DivideWork work = null;
			final long div = workManager.process(work, DivideWorkEngine.class).join();
			nop(div);
		});
	}

	@Test
	public void testProcessWithError() {
		Assertions.assertThrows(ArithmeticException.class, () -> {
			final DivideWork work = new DivideWork(10, 0);
			final long div = workManager.process(work, DivideWorkEngine.class).join();
			nop(div);
		});
	}

	//=========================================================================
	//===========================SCHEDULE======================================
	//=========================================================================

	/**
	 * test of 2 async executions
	 */
	@Test
	public void testSchedule() {
		final DivideWork work = new DivideWork(10, 5);
		final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
		workManager.schedule(work, DivideWorkEngine.class, workResultHanlder);
		workManager.schedule(work, DivideWorkEngine.class, workResultHanlder);
		//---
		final boolean finished = workResultHanlder.waitFinish(2, warmupTime);
		Assertions.assertTrue(finished, "Can't finished in time : " + workResultHanlder);
		Assertions.assertEquals(2, workResultHanlder.getLastResult().intValue());
		Assertions.assertEquals(null, workResultHanlder.getLastThrowable());
	}

	@Test
	public void testScheduleWithNull() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final DivideWork work = null;
			final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
			workManager.schedule(work, DivideWorkEngine.class, workResultHanlder);
		});
	}

	/**
	 * Teste l'exécution asynchrone d'une tache avec erreur. (Division par 0)
	 */
	@Test
	public void testScheduleError() {
		final DivideWork work = new DivideWork(10, 0);
		final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
		workManager.schedule(work, DivideWorkEngine.class, workResultHanlder);

		final boolean finished = workResultHanlder.waitFinish(1, warmupTime);
		//On vérifie plusieurs  choses
		// -que l'erreur remontée est bien une ArithmeticException
		//- que l'exception est contenue dans le handler
		Assertions.assertTrue(finished, "Can't finished in time : " + workResultHanlder);
		Assertions.assertEquals(null, workResultHanlder.getLastResult());
		Assertions.assertEquals(ArithmeticException.class, workResultHanlder.getLastThrowable().getClass());
	}

	/**
	 * Teste l'exécution asynchrone d'une tache avec une durée de timeOut trop courte.
	 */
	@Test
	public void testScheduleWithTimeOut() {
		final int workTime = 5 * 1000; //5s temps d'exécution d'un work
		final MyWorkResultHanlder<Boolean> workResultHanlder = new MyWorkResultHanlder<>();
		final SlowWork work = new SlowWork(workTime);
		workManager.schedule(work, SlowWorkEngine.class, workResultHanlder);
		final boolean finished = workResultHanlder.waitFinish(1, workTime - 1000);
		//-----
		//We are expecting a time out if we are waiting less than execution's time.
		Assertions.assertFalse(finished);
	}

	//=========================================================================
	//=========================================================================
	//=========================================================================

	/**
	 * Teste l'exécution asynchrone de plusieurs taches.
	 * - On démarre 10 (2 fois le nombre de workerCount) Travaux qui ne font rien qu'attendre 5s.
	 * On vérifie que si on attend 10s + marge alors toutes les taches sont exécutées.
	 */
	@Test
	public void testWaitForAll() {
		final int workTime = 2 * 1000; //2s temps d'exécution d'un work
		final MyWorkResultHanlder<Boolean> workResultHanlder = new MyWorkResultHanlder<>(1);

		createWorkItems(WORKER_COUNT * 2, workTime, workResultHanlder);
		final boolean finished = workResultHanlder.waitFinish(WORKER_COUNT * 2, 2 * workTime + warmupTime);

		//On estime que la durée max n'excéde pas le workTime + 1000ms
		//-----
		Assertions.assertTrue(finished, "Can't finished in time : " + workResultHanlder);

	}

	/**
	 * Teste l'exécution asynchrone de très nombreuses taches.
	 * - On démarre 50 (10 fois le nombre de workerCount) Travaux qui ne font rien qu'attendre 2s.
	 * On vérifie que les taches sont toutes prise en charge avant la fin.
	 * On vérifie que si on attend 10*2s + marge alors toutes les taches sont exécutées.
	 */
	@Test
	public void testWaitForAllMassWork() {
		final int workToCreate = 10 * WORKER_COUNT;
		final int workTime = 2 * 1000; //2s temps d'exécution d'un work
		final long start = System.currentTimeMillis();
		final MyWorkResultHanlder<Boolean> workResultHanlder = new MyWorkResultHanlder<>();
		createWorkItems(workToCreate, workTime, workResultHanlder);
		final long timeout = Math.round(10 * 1.2 * workTime + warmupTime); //*1.2 because some lag on our PIC
		Assertions.assertTrue(System.currentTimeMillis() - start < timeout, "Schedule de " + workToCreate + " work trop long : " + (System.currentTimeMillis() - start) + "ms");

		final boolean finished = workResultHanlder.waitFinish(workToCreate, timeout);
		//On estime que la durée max n'excéde pas le workTime + 1000ms
		//-----
		Assertions.assertTrue(finished, "Les " + workToCreate + " works n'ont pas terminés dans les temps, le timeout à " + timeout + "ms s'est déclenché: " + workResultHanlder);

	}

	/**
	 * Teste les Work qui vident correctement leur threadLocal.
	 */
	@Test
	public void testThreadLocalWorkReset() {
		final int workToCreate = 20 * WORKER_COUNT;
		final int workTime = 500; //200ms temps d'exécution d'un work
		final MyWorkResultHanlder<Integer> workResultHanlder = new MyWorkResultHanlder<>();
		createThreadLocalWorkItems(workToCreate, workTime, true, workResultHanlder);

		final boolean finished = workResultHanlder.waitFinish(workToCreate, workTime * workToCreate);
		//-----
		Assertions.assertTrue(finished, "Les works n'ont pas terminés dans les temps, le timeout à " + workTime * workToCreate + "ms s'est déclenché");
		Assertions.assertEquals(Integer.valueOf(1), workResultHanlder.getLastResult(), "ThreadLocal conservé entre deux exécutions ");
	}

	/**
	 * Teste les Work qui NE vident PAS leur threadLocal.
	 */
	@Test
	public void testThreadLocalWorkNotReset() {
		final int workToCreate = 20 * WORKER_COUNT;
		final int workTime = 200; //200ms temps d'exécution d'un work
		final MyWorkResultHanlder<Integer> workResultHanlder = new MyWorkResultHanlder<>();
		createThreadLocalWorkItems(workToCreate, workTime, false, workResultHanlder);

		final boolean finished = workResultHanlder.waitFinish(workToCreate, 50 * workTime * workToCreate);
		//-----
		Assertions.assertTrue(finished, "Les works n'ont pas terminés dans les temps, le timeout à " + workTime * workToCreate + "ms s'est déclanché");
		Assertions.assertEquals(Integer.valueOf(1), workResultHanlder.getLastResult(), "ThreadLocal conservé entre deux exécutions ");
	}

	private void createWorkItems(final int workToCreate, final int workTime, final WorkResultHandler<Boolean> workResultHanlder) {
		for (int i = 0; i < workToCreate; i++) {
			workManager.schedule(new SlowWork(workTime), SlowWorkEngine.class, workResultHanlder);
		}
	}

	private void createThreadLocalWorkItems(final int workToCreate, final int workTime, final boolean clearThreadLocal, final WorkResultHandler<Integer> workResultHanlder) {
		for (int i = 0; i < workToCreate; i++) {
			workManager.schedule(new ThreadLocalWork(workTime, clearThreadLocal), ThreadLocalWorkEngine.class, workResultHanlder);
		}
	}
}
