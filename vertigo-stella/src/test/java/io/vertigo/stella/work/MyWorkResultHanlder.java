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

import io.vertigo.core.lang.Assertion;
import io.vertigo.stella.master.WorkResultHandler;

/**
 * Handler unique permettant de collecter les infos relatives à l'exécution des tests.
 *
 * @author pchretien
 * @param<R> result
 */
public final class MyWorkResultHanlder<R> implements WorkResultHandler<R> {
	private R lastResult;
	private Throwable lastError;
	//compteurs
	private final int logCountEvery;
	private int succeededCount;
	private int failedCount;
	private final long start = System.currentTimeMillis();

	public MyWorkResultHanlder() {
		this.logCountEvery = 10;
	}

	public MyWorkResultHanlder(final int logCountEvery) {
		this.logCountEvery = logCountEvery;
	}

	@Override
	public synchronized void onStart() {
		//System.out.println("onStart");
	}

	public synchronized R getLastResult() {
		return lastResult;
	}

	public synchronized Throwable getLastThrowable() {
		return lastError;
	}

	@Override
	public synchronized void onDone(final R result, final Throwable error) {
		Assertion.check().isTrue(result == null ^ error == null, "result xor error is null");
		//-----
		lastResult = result;
		lastError = error;
		if (error == null) {
			//System.out.println("onSuccess");
			succeededCount++;
		} else {
			failedCount++;
		}
		if (failedCount + succeededCount > 0 && (failedCount + succeededCount) % logCountEvery == 0) {
			final long elapsed = System.currentTimeMillis() - start;
			System.out.println(">executed> " + toString() + " in avg:" + elapsed / (failedCount + succeededCount) + " ms/" + logCountEvery + " exec");
		}
	}

	private synchronized boolean isFinished(final int expected, final long timeoutMs) {
		return failedCount + succeededCount < expected && System.currentTimeMillis() - start < timeoutMs;
	}

	public boolean waitFinish(final int expected, final long timeoutMs) {
		while (isFinished(expected, timeoutMs)) {
			try {
				Thread.sleep(100); //On attend 100ms
			} catch (final InterruptedException e) {
				break;//on quitte
			}
		}
		return failedCount + succeededCount == expected;
	}

	@Override
	public synchronized String toString() {
		return "{success : " + succeededCount + " , fail : " + failedCount + " }";

	}
}
