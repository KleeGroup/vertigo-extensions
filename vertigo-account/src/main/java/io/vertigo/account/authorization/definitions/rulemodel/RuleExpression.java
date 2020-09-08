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

import io.vertigo.core.lang.Assertion;

/**
 * Single Expression Definition.
 * (field)(operator)(value)
 * @author npiedeloup
 */
public final class RuleExpression {
	/**
	 * All authorized operators.
	 * Should be ordered for parsing.
	 */
	public enum ValueOperator implements RuleOperator {
		/** Lesser Than or Equals. */
		LTE("<="),
		/** Greater Than or Equals. */
		GTE(">="),
		/** Not Equals. */
		NEQ("!="),
		/** Equals. */
		EQ("="),
		/** Lesser Than. */
		LT("<"),
		/** Greater Than. */
		GT(">");

		private final String[] asString;

		ValueOperator(final String... asString) {
			this.asString = asString;
		}

		/** {@inheritDoc} */
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

	private final String fieldName;
	private final ValueOperator operator;
	private final RuleValue value;

	/**
	 * @param fieldName FieldName
	 * @param operator OperatorDefinition
	 * @param value QueryDefinition
	 */
	public RuleExpression(final String fieldName, final ValueOperator operator, final RuleValue value) {
		Assertion.check()
				.isNotBlank(fieldName)
				.isNotNull(operator)
				.isNotNull(value);
		//-----
		this.fieldName = fieldName;
		this.operator = operator;
		this.value = value;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return fieldName + operator + value;
	}

	/**
	 * @return FieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @return operator
	 */
	public ValueOperator getOperator() {
		return operator;
	}

	/**
	 * @return value
	 */
	public RuleValue getValue() {
		return value;
	}
}
