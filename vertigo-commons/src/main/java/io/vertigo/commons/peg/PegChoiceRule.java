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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;

/**
 * The first rule that matches is taken.
 * If no rule is found then an notFoundException is thrown.
 *
 * @author pchretien
 */
final class PegChoiceRule implements PegRule<PegChoice> {
	private final List<PegRule<?>> rules;
	private final String expression;

	/**
	 * Constructor.
	 * @param rules the list of rules to test
	 */
	PegChoiceRule(final List<PegRule<?>> rules) {
		Assertion.check().isNotNull(rules);
		//-----
		this.rules = Collections.unmodifiableList(rules);
		//---
		//A choice of rules/expressions is like that : (e1 | e2 | e3)
		expression = "("
				+ rules.stream()
						.map(PegRule::getExpression)
						.collect(Collectors.joining(" | "))
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

	@Override
	public PegResult<PegChoice> parse(final String text, final int start) throws PegNoMatchFoundException {
		PegNoMatchFoundException best = null;
		for (int choiceIndex = 0; choiceIndex < getRules().size(); choiceIndex++) {
			try {
				PegLogger.look("CHOICE", "c" + choiceIndex, start, getRules().get(choiceIndex));
				final PegResult<?> parserCursor = getRules().get(choiceIndex).parse(text, start);
				final int end = parserCursor.getIndex();
				if (parserCursor.getBestUncompleteRule().isPresent()) {
					best = keepBestUncompleteRule(parserCursor.getBestUncompleteRule().get(), best);
				}
				if (best != null && end < best.getIndex()) {
					Assertion.check().isNotNull(best, "best exception should be set at same time of bestIndex");
					//Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
					PegLogger.log("Reject CHOICE pos" + start + " : " + choiceIndex + " at " + end);
					throw best;
				}
				PegLogger.found("CHOICE", "c" + choiceIndex, start, end, text, getRules().get(choiceIndex));
				final PegChoice value = new PegChoice(choiceIndex, parserCursor.getValue());
				return new PegResult<>(end, value, best);
			} catch (final PegNoMatchFoundException e) {
				//Tant que l'on a des erreurs sur l'évaluation des règles
				//on recommence jusqu'à trouver la première qui fonctionne.
				PegLogger.miss("CHOICE", "c" + choiceIndex, start, getRules().get(choiceIndex));
				best = keepBestUncompleteRule(new PegNoMatchFoundException(text, e.getIndex(), e, getExpression()), best);
			}
		}
		//Nothing has been found
		throw keepBestUncompleteRule(new PegNoMatchFoundException(text, start, null, "No rule found when evalutating  FirstOf : '{0}'", getExpression()), best);
	}

	private PegNoMatchFoundException keepBestUncompleteRule(final PegNoMatchFoundException first, final PegNoMatchFoundException otherNullable) {
		Assertion.check().isNotNull(first);
		//----
		if (otherNullable == null || otherNullable.getIndex() < first.getIndex()) {
			return first;
		}
		return otherNullable;
	}
}
