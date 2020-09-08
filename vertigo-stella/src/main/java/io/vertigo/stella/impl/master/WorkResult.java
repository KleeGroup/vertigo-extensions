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

import io.vertigo.core.lang.Assertion;

/**
 * @author pchretien
 * @param<R> result
 */
public final class WorkResult<R> {
	public final String workId;
	public final Throwable error;
	public final R result;

	/**
	 * Constructor.
	 * Must set Result or Error, other must be null.
	 * @param workId Work id
	 * @param result Work result (null if error)
	 * @param error Work error (null if result)
	 */
	public WorkResult(final String workId, final R result, final Throwable error) {
		Assertion.check()
				.isNotBlank(workId)
				.isTrue(result == null ^ error == null, "result xor error is null");
		//-----
		this.workId = workId;
		this.error = error;
		this.result = result;
	}
}
