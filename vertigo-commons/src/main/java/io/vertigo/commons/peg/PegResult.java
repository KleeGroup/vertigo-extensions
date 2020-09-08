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
package io.vertigo.commons.peg;

import java.util.Optional;

import io.vertigo.core.lang.Assertion;

/**
 * 	Contains the result of a parsing operation.
 *   - the new index position
 *   - the object created
 * @author pchretien
 *
 * @param <R> the type of the Result Object
 */
public final class PegResult<R> {
	private final int index;
	private final R value;
	private final Optional<PegNoMatchFoundException> bestUncompleteRule;

	/**
	 * Constructor.
	 * @param index
	 * @param result
	 */
	PegResult(final int index, final R result) {
		Assertion.check().isNotNull(result);
		//---
		this.index = index;
		this.value = result;
		this.bestUncompleteRule = Optional.empty();
	}

	/**
	 * Constructor.
	 * @param index
	 * @param result
	 * @param bestUncompleteRule
	 */
	PegResult(final int index, final R result, final PegNoMatchFoundException bestUncompleteRule) {
		Assertion.check().isNotNull(result);
		//---
		this.index = index;
		this.value = result;
		this.bestUncompleteRule = Optional.ofNullable(bestUncompleteRule);
	}

	/**
	 * @return Index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the result value
	 */
	public R getValue() {
		return value;
	}

	/**
	 * @return the best Uncomplete Rule
	 */
	public Optional<PegNoMatchFoundException> getBestUncompleteRule() {
		return bestUncompleteRule;
	}

}
