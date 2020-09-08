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
package io.vertigo.stella.impl.master;

import java.util.UUID;
import java.util.concurrent.Future;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.stella.impl.master.coordinator.MasterCoordinator;
import io.vertigo.stella.impl.master.listener.WorkListener;
import io.vertigo.stella.impl.master.listener.WorkListenerImpl;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.master.MasterManager;
import io.vertigo.stella.master.WorkPromise;
import io.vertigo.stella.master.WorkResultHandler;
import io.vertigo.stella.work.WorkEngine;

/**
 * Implémentation de workManager.
 *
 * @author pchretien, npiedeloup
 */
public final class MasterManagerImpl implements MasterManager, Activeable {
	private final WorkListener workListener;
	private final MasterCoordinator masterCoordinator;

	/**
	 * Constructeur.
	 * @param masterPlugin Optional plugin for work's distribution
	 */
	@Inject
	public MasterManagerImpl(final MasterPlugin masterPlugin) {
		Assertion.check().isNotNull(masterPlugin);
		//-----
		workListener = new WorkListenerImpl(/*analyticsManager*/);
		masterCoordinator = new MasterCoordinator(masterPlugin);
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//coordinator n'étant pas un plugin
		//il faut le démarrer et l'arréter explicitement.
		masterCoordinator.start();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		masterCoordinator.stop();
	}

	private static String createWorkId() {
		return UUID.randomUUID().toString();
	}

	/** {@inheritDoc} */
	@Override
	public <W, R> WorkPromise<R> process(final W work, final Class<? extends WorkEngine<W, R>> workEngineClass) {
		Assertion.check()
				.isNotNull(work)
				.isNotNull(workEngineClass);
		//-----
		final WorkItem<W, R> workItem = new WorkItem<>(createWorkId(), work, workEngineClass);
		final WorkResultHandler<R> emptyWorkResultHandler = new WorkResultHandler<>() {
			@Override
			public void onStart() {
				//nothing
			}

			@Override
			public void onDone(final R result, final Throwable error) {
				//nothing
			}
		};

		final Future<R> future = submit(workItem, emptyWorkResultHandler);
		return new WorkPromiseImpl(future);
	}

	@Override
	public <W, R> void schedule(final W work, final Class<? extends WorkEngine<W, R>> workEngineClass, final WorkResultHandler<R> workResultHandler) {
		Assertion.check()
				.isNotNull(work)
				.isNotNull(workEngineClass)
				.isNotNull(workResultHandler);
		//-----
		final WorkItem<W, R> workItem = new WorkItem<>(createWorkId(), work, workEngineClass);
		submit(workItem, workResultHandler);
	}

	private <W, R> Future<R> submit(final WorkItem<W, R> workItem, final WorkResultHandler<R> workResultHandler) {
		workListener.onStart(workItem.getWorkEngineClass().getName());
		boolean executed = false;
		final long start = System.currentTimeMillis();
		try {
			final Future<R> future = masterCoordinator.submit(workItem, workResultHandler);
			executed = true;
			return future;
		} finally {
			workListener.onFinish(workItem.getWorkEngineClass().getName(), System.currentTimeMillis() - start, executed);
		}
	}
}
