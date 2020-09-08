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
package io.vertigo.datafactory.impl.search.dsl.model;

import java.util.List;

import io.vertigo.core.lang.Assertion;

/**
 * Multi expressions definition.
 * (preBody)\(?(expression|multiExpression)+\)?(postBody)
 * @author npiedeloup
 */
public final class DslMultiExpression {

	private final String preBody; //Spaces like
	private final boolean block;
	private final List<DslExpression> expressions;
	private final List<DslMultiExpression> multiExpressions;
	private final String postBody; //Spaces like

	/**
	 * @param preBody String before body
	 * @param block Is mode block
	 * @param expressions List of simple expression
	 * @param multiExpressions List of multi-expression
	 * @param postBody String after body
	 */
	public DslMultiExpression(
			final String preBody,
			final boolean block,
			final List<DslExpression> expressions,
			final List<DslMultiExpression> multiExpressions,
			final String postBody) {
		Assertion.check()
				.isNotNull(preBody)
				.isNotNull(expressions)
				.isNotNull(multiExpressions)
				.isNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.block = block;
		this.expressions = expressions;
		this.multiExpressions = multiExpressions;
		this.postBody = postBody;
	}

	/**
	 * @return preBody
	 */
	public String getPreBody() {
		return preBody;
	}

	/**
	 * @return block
	 */
	public boolean isBlock() {
		return block;
	}

	/**
	 * @return expressions
	 */
	public List<DslExpression> getExpressions() {
		return expressions;
	}

	/**
	 * @return multiExpressions
	 */
	public List<DslMultiExpression> getMultiExpressions() {
		return multiExpressions;
	}

	/**
	 * @return postBody
	 */
	public String getPostBody() {
		return postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder()
				.append(preBody).append(block ? "(" : "");
		for (final DslExpression expression : expressions) {
			sb.append(expression);
		}
		for (final DslMultiExpression multiExpression : multiExpressions) {
			sb.append(multiExpression);
		}
		sb.append(block ? ")" : "").append(postBody);
		return sb.toString();
	}
}
