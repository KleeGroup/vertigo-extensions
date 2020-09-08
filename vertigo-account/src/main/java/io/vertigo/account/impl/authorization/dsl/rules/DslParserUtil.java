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
package io.vertigo.account.impl.authorization.dsl.rules;

import io.vertigo.account.authorization.definitions.rulemodel.RuleMultiExpression;
import io.vertigo.commons.peg.PegNoMatchFoundException;

/**
 * Util for parsing security rules.
 * @author npiedeloup
 */
public final class DslParserUtil {

	private DslParserUtil() {
		//nothing
	}

	/**
	 * @param buildQuery Builder pattern
	 * @return Parsed pattern
	 * @throws PegNoMatchFoundException If pattern doesn't match grammar
	 */
	public static RuleMultiExpression parseMultiExpression(final String buildQuery) throws PegNoMatchFoundException {
		if ("true".equals(buildQuery.trim())) {
			return new RuleMultiExpression(true);
		}
		return new DslMultiExpressionRule()
				.parse(buildQuery)
				.getValue();
	}
}
