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
import java.util.List;

import io.vertigo.core.lang.Assertion;

/**
 * The manyRule.
 * If the pattern AB is searched and
 * the text is
 *  - empty : an empty list is returned only if emptyAccepted.
 *  - ABAB : a list with a size of 2 is returned
 *  - ABABC : a list with a size of 2 is returned only if untilEnd is false.
 *
 *  if untilEnd is true then all the text must be consumed during the evaluation.
 *
 * @author pchretien
 * @param <R> Type of the product text parsing
 */
final class PegManyRule<R> implements PegRule<List<R>> {
	private final PegRule<R> rule;
	private final String expression;
	private final boolean zeroAccepted;
	private final boolean untilEnd;

	/**
	 * Constructor.
	 * @param rule the rule that's will be evaluated
	 * @param zeroAccepted zeroOrMore else oneOrMore
	 * @param untilEnd if the evaluation must parse all text
	 */
	PegManyRule(final PegRule<R> rule, final boolean zeroAccepted, final boolean untilEnd) {
		Assertion.check().isNotNull(rule);
		//-----
		this.rule = rule;
		this.zeroAccepted = zeroAccepted;
		this.untilEnd = untilEnd;
		expression = rule.getExpression() + (zeroAccepted ? "*" : "+");
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	boolean isEmptyAccepted() {
		return zeroAccepted;
	}

	PegRule<R> getRule() {
		return rule;
	}

	/** {@inheritDoc} */
	@Override
	public PegResult<List<R>> parse(final String text, final int start) throws PegNoMatchFoundException {
		int index = start;
		int i = 0;
		//-----
		final List<R> results = new ArrayList<>();
		PegNoMatchFoundException best = null;
		try {
			int prevIndex = -1;
			while (index < text.length() && index > prevIndex) {
				prevIndex = index;
				PegLogger.look("MANY", "m" + i++, prevIndex, getRule());

				final PegResult<R> parserCursor = getRule()
						.parse(text, index);
				index = parserCursor.getIndex();
				PegLogger.found("MANY", "m" + i, prevIndex, index, text, getRule());

				if (index > prevIndex) {
					//cela signifie que l'index n a pas avancé, on sort
					results.add(parserCursor.getValue());
				}
			}
		} catch (final PegNoMatchFoundException e) {
			best = e;
			PegLogger.miss("MANY", "m" + i, index, getRule());
		}
		if (!isEmptyAccepted() && results.isEmpty()) {
			throw new PegNoMatchFoundException(text, best != null ? best.getIndex() : start, best, "Aucun élément de la liste trouvé : {0}", getExpression());
		}
		if (untilEnd && text.length() > index) {
			throw new PegNoMatchFoundException(text, best != null ? best.getIndex() : start, best, "{0} élément(s) trouvé(s), éléments suivants non parsés selon la règle :{1}", results.size(), getExpression());
		}
		return new PegResult<>(index, results, best);
	}
}
