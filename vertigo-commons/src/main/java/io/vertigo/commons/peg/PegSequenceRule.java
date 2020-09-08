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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;

/**
 * As wikipedia says
 * The sequence operator e1 e2 first invokes e1,
 * and if e1 succeeds, subsequently invokes e2 on the remainder of the input string left unconsumed by e1,
 * and returns the result.
 * If either e1 or e2 fails, then the sequence expression e1 e2 fails.
 *
 * @author pchretien
 */
final class PegSequenceRule implements PegRule<List<Object>> {
	private final List<PegRule<?>> rules;
	private final String expression;

	/**
	 * Constructor.
	 */
	PegSequenceRule(final List<PegRule<?>> rules) {
		Assertion.check()
				.isNotNull(rules)
				.isTrue(rules.size() > 1, "A sequence must contain at least 2 rules");
		//-----
		this.rules = Collections.unmodifiableList(rules);
		//---
		//A sequence of rules/expressions is like that : (e1 e2 e3)
		expression = "("
				+ this.rules.stream()
						.map(PegRule::getExpression)
						.collect(Collectors.joining(" "))
				+ ")";
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	List<PegRule<?>> getRules() {
		return rules;
	}

	/** {@inheritDoc} */
	@Override
	public PegResult<List<Object>> parse(final String text, final int start) throws PegNoMatchFoundException {
		final List<Object> results = new ArrayList<>();
		int index = start;
		PegNoMatchFoundException best = null;
		try {
			for (final PegRule<?> rule : rules) {
				final PegResult<?> cursor = rule
						.parse(text, index);
				index = cursor.getIndex();
				results.add(cursor.getValue());
				if (cursor.getBestUncompleteRule().isPresent()) {
					best = keepBestUncompleteRule(cursor.getBestUncompleteRule().get(), best);
				}
			}
		} catch (final PegNoMatchFoundException e) {
			throw keepBestUncompleteRule(new PegNoMatchFoundException(text, e.getIndex(), e, getExpression()), best);
		}
		return new PegResult<>(index, results);
	}

	private static PegNoMatchFoundException keepBestUncompleteRule(final PegNoMatchFoundException first, final PegNoMatchFoundException otherNullable) {
		Assertion.check().isNotNull(first);
		//----
		if (otherNullable == null || otherNullable.getIndex() < first.getIndex()) {
			return first;
		}
		return otherNullable;
	}
}
