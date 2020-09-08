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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.stella.master.WorkPromise;

/**
 *
 * @author pchretien
 */
final class WorkPromiseImpl implements WorkPromise {
	private final Future internal;

	WorkPromiseImpl(final Future future) {
		Assertion.check().isNotNull(future);
		//---
		this.internal = future;
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		return internal.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return internal.isCancelled();
	}

	@Override
	public boolean isDone() {
		return internal.isDone();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return internal.get();
	}

	@Override
	public Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return internal.get(timeout, unit);
	}

	@Override
	public Object join() {
		try {
			return internal.get();
		} catch (final ExecutionException | InterruptedException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			throw WrappedException.wrap(e.getCause());
		}
	}

}
