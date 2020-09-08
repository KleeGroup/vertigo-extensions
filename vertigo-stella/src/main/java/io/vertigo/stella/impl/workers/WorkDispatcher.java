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
package io.vertigo.stella.impl.workers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.vertigo.core.lang.Assertion;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.impl.workers.coordinator.WorkersCoordinator;
import io.vertigo.stella.master.WorkResultHandler;

final class WorkDispatcher implements Runnable {
	private final WorkersCoordinator localWorker;
	private final String workType;
	private final WorkersPlugin workerPlugin;
	private final String nodeId;

	WorkDispatcher(final String nodeId, final String workType, final WorkersCoordinator localWorker, final WorkersPlugin nodePlugin) {
		Assertion.check()
				.isNotBlank(nodeId)
				.isNotBlank(workType)
				.isTrue(workType.indexOf('^') == -1, "Number of dispatcher per WorkType must be managed by NodeManager {0}", workType)
				.isNotNull(localWorker)
				.isNotNull(nodePlugin);
		//-----
		this.nodeId = nodeId;
		this.workType = workType;
		this.localWorker = localWorker;
		workerPlugin = nodePlugin;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				doRun();
			} catch (final InterruptedException e) {
				break; //stop on Interrupt
			}
		}
	}

	private <W, R> void doRun() throws InterruptedException {
		final WorkItem<W, R> workItem = workerPlugin.pollWorkItem(nodeId, workType);
		if (workItem != null) {
			final WorkResultHandler<R> workResultHandler = new WorkResultHandler<>() {
				@Override
				public void onStart() {
					workerPlugin.putStart(workItem.getId());
				}

				@Override
				public void onDone(final R result, final Throwable error) {
					//nothing here, should be done by waiting the future result
				}
			};
			//---Et on fait executer par le workerLocal
			final Future<R> futureResult = localWorker.submit(workItem, workResultHandler);
			R result;
			try {
				result = futureResult.get();
				workerPlugin.putResult(workItem.getId(), result, null);
			} catch (final ExecutionException e) {
				workerPlugin.putResult(workItem.getId(), null, e.getCause());
			}
		}
		//if workitem is null, that's mean there is no workitem available;
	}

}
