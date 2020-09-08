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
package io.vertigo.stella.impl.workers.coordinator;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.vertigo.core.lang.Assertion;
import io.vertigo.stella.impl.work.Coordinator;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.master.WorkResultHandler;

/**
 * Implémentation d'un pool local de {@link Coordinator}.
 *
 * @author pchretien
 */
public final class WorkersCoordinator implements Coordinator, Closeable {
	/** Pool de workers qui wrappent sur l'implémentation générique.*/
	private final ExecutorService workers;

	/**
	 * Constructeur.
	 *
	 * @param workerCount paramètres d'initialisation du pool
	 */
	public WorkersCoordinator(final int workerCount) {
		Assertion.check().isTrue(workerCount >= 1, "At least one thread must be allowed to process asynchronous works.");
		//-----
		workers = Executors.newFixedThreadPool(workerCount);
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		//Shutdown in two phases (see doc)
		workers.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!workers.awaitTermination(60, TimeUnit.SECONDS)) {
				workers.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!workers.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (final InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			workers.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Work devant être exécuté
	 * WorkItem contient à la fois le Work et le callback.
	 * @param workItem WorkItem
	 * @param<R> result
	 */
	@Override
	public <W, R> Future<R> submit(final WorkItem<W, R> workItem, final WorkResultHandler<R> workResultHandler) {
		Assertion.check().isNotNull(workItem);
		//-----
		return workers.submit(new Worker<>(workItem, workResultHandler));
	}
}
