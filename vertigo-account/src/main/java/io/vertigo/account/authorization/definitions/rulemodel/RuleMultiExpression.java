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
package io.vertigo.account.authorization.definitions.rulemodel;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.lang.Assertion;

/**
 * Multi expressions definition.
 * \(?(expression1|multiExpression1) ((logicalOperator) (expression2|multiExpression2))*\)?
 * @author npiedeloup
 */
public final class RuleMultiExpression {

	/**
	 * All authorized operators.
	 */
	public enum BoolOperator implements RuleOperator {
		/** OR. */
		OR("OR", "Or", "or", "||"),
		/** AND. */
		AND("AND", "And", "and", "&&");

		private final String[] asString;

		BoolOperator(final String... asString) {
			this.asString = asString;
		}

		/**
		 * @return List of authorized string for this operator
		 */
		@Override
		public String[] authorizedString() {
			return asString;
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return asString[0];
		}

	}

	private final boolean block;
	private final boolean alwaysTrue;
	private final BoolOperator boolOperator;
	private final List<RuleExpression> expressions;
	private final List<RuleMultiExpression> multiExpressions;

	/**
	 * @param alwaysTrue Is alwaysTrue
	 */
	public RuleMultiExpression(final boolean alwaysTrue) {
		Assertion.check().isTrue(alwaysTrue, "Always true constructor, must be use when rule mean alwaysTrue");
		//-----
		block = false;
		this.alwaysTrue = true;
		boolOperator = BoolOperator.AND;
		expressions = Collections.emptyList();
		multiExpressions = Collections.emptyList();
	}

	/**
	 * @param block Is mode block
	 * @param boolOperator logical Operator : must be the same for all not blocked expressions
	 * @param expressions List of simple expression
	 * @param multiExpressions List of multi-expression
	 */
	public RuleMultiExpression(
			final boolean block,
			final BoolOperator boolOperator,
			final List<RuleExpression> expressions,
			final List<RuleMultiExpression> multiExpressions) {
		Assertion.check()
				.isNotNull(boolOperator)
				.isNotNull(expressions)
				.isNotNull(multiExpressions);
		//-----
		this.block = block;
		alwaysTrue = false;
		this.boolOperator = boolOperator;
		this.expressions = expressions;
		this.multiExpressions = multiExpressions;
	}

	/**
	 * @return boolOperator
	 */
	public BoolOperator getBoolOperator() {
		return boolOperator;
	}

	/**
	 * @return block
	 */
	public boolean isBlock() {
		return block;
	}

	/**
	 * @return alwaysTrue
	 */
	public boolean isAlwaysTrue() {
		return alwaysTrue;
	}

	/**
	 * @return expressions
	 */
	public List<RuleExpression> getExpressions() {
		return expressions;
	}

	/**
	 * @return multiExpressions
	 */
	public List<RuleMultiExpression> getMultiExpressions() {
		return multiExpressions;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder()
				.append(alwaysTrue ? "true" : "")
				.append(block ? "(" : "");
		String sep = "";
		for (final RuleExpression expression : expressions) {
			sb.append(sep);
			sb.append(expression);
			sep = " " + boolOperator + " ";
		}
		for (final RuleMultiExpression multiExpression : multiExpressions) {
			sb.append(sep);
			sb.append(multiExpression);
			sep = " " + boolOperator + " ";
		}
		sb.append(block ? ")" : "");
		return sb.toString();
	}
}
