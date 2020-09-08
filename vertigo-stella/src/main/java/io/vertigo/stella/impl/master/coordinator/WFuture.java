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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.stella.master.WorkResultHandler;

/**
 * @author pchretien
 * @param<R> result
 */
final class WFuture<R> implements Future<R>, WorkResultHandler<R> {
	private final AtomicBoolean done = new AtomicBoolean(false);
	private final CountDownLatch countDownLatch = new CountDownLatch(1);
	private Throwable myError;
	private R myResult;
	private final WorkResultHandler<R> redirect;

	WFuture(final WorkResultHandler<R> redirect) {
		Assertion.check().isNotNull(redirect);
		//---
		this.redirect = redirect;
	}

	/** {@inheritDoc} */
	@Override
	public void onDone(final R result, final Throwable error) {
		Assertion.check().isTrue(result == null ^ error == null, "result xor error is null");
		//-----
		if (done.compareAndSet(false, true)) {
			myResult = result;
			myError = error;
			countDownLatch.countDown();
		}
		redirect.onDone(result, error);
	}

	/** {@inheritDoc} */
	@Override
	public void onStart() {
		redirect.onStart();
	}

	/** {@inheritDoc} */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		if (done.compareAndSet(false, true)) {
			myResult = null;
			myError = new CancellationException();
			countDownLatch.countDown();
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCancelled() {
		if (done.get()) {
			try {
				countDownLatch.await();
			} catch (final InterruptedException e) {
				throw WrappedException.wrap(e);
			}
			return myError instanceof CancellationException;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDone() {
		return done.get() && countDownLatch.getCount() == 0;
	}

	@Override
	public R get() throws InterruptedException, ExecutionException {
		countDownLatch.await();
		if (myResult != null) {
			return myResult;
		}
		if (myError instanceof CancellationException) {
			throw (CancellationException) new CancellationException().initCause(myError);
		}
		throw new ExecutionException(myError);
	}

	@Override
	public R get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (!countDownLatch.await(timeout, unit)) {
			throw new TimeoutException();
		}
		if (myResult != null) {
			return myResult;
		}
		if (myError instanceof CancellationException) {
			throw (CancellationException) new CancellationException().initCause(myError);
		}
		throw new ExecutionException(myError);
	}

}
