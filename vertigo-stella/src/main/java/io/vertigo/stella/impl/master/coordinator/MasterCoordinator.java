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
package io.vertigo.stella.impl.master.coordinator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.stella.impl.master.MasterPlugin;
import io.vertigo.stella.impl.master.WorkResult;
import io.vertigo.stella.impl.work.Coordinator;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.master.WorkResultHandler;

/**
 * @author pchretien
 */
public final class MasterCoordinator implements Coordinator, Activeable {
	private final MasterPlugin masterPlugin;
	private final Thread watcher;
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

	public MasterCoordinator(final MasterPlugin masterPlugin) {
		Assertion.check().isNotNull(masterPlugin);
		//-----
		this.masterPlugin = masterPlugin;
		watcher = createWatcher();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		watcher.start();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		watcher.interrupt();
		try {
			watcher.join();
		} catch (final InterruptedException e) {
			//On ne fait rien
		}
	}

	/** {@inheritDoc} */
	@Override
	public <W, R> Future<R> submit(final WorkItem<W, R> workItem, final WorkResultHandler<R> workResultHandler) {
		//2. On attend les notifs sur un thread séparé, la main est rendue de suite
		final WFuture<R> future = new WFuture(workResultHandler);
		putWorkItem(workItem, future);
		return future;
	}

	private <R> void setResult(final String workId, final R result, final Throwable error) {
		Assertion.check()
				.isNotBlank(workId)
				.isTrue(result == null ^ error == null, "result xor error is null");
		//-----
		final WorkResultHandler workResultHandler = workResultHandlers.remove(workId);
		if (workResultHandler != null) {
			//Que faire sinon
			workResultHandler.onDone(result, error);
		}
	}

	private Thread createWatcher() {
		return new Thread("DistributedWorkResultWatcher") {
			/** {@inheritDoc} */
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					//On attend le résultat (par tranches de 1s)
					final int waitTimeSeconds = 1;
					final WorkResult result = masterPlugin.pollResult(waitTimeSeconds);
					if (result != null) {
						setResult(result.workId, result.result, result.error);
					}
				}
			}
		};
	}

	private <W, R> void putWorkItem(final WorkItem<W, R> workItem, final WorkResultHandler<R> workResultHandler) {
		workResultHandlers.put(workItem.getId(), workResultHandler);
		masterPlugin.putWorkItem(workItem);
	}
}
