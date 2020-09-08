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
package io.vertigo.datafactory.impl.search.dsl.rules;

import java.util.List;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoExpression;
import io.vertigo.datafactory.impl.search.dsl.model.DslMultiExpression;
import io.vertigo.datafactory.impl.search.dsl.model.DslUserCriteria;

/**
 * Util for parsing search patterns and queries.
 * @author npiedeloup
 */
public final class DslParserUtil {

	private DslParserUtil() {
		//nothing
	}

	/**
	 * @param buildQuery Builder pattern
	 * @return List<DslMultiExpression> Parsed pattern
	 */
	public static List<DslMultiExpression> parseMultiExpression(final String buildQuery) {
		try {
			return parseMultiExpressionWithError(buildQuery);
		} catch (final PegNoMatchFoundException e) {
			final String message = StringUtil.format("Can't parse listFilterPattern {0}\n{1}", buildQuery, e.getFullMessage());
			throw WrappedException.wrap(e, message);
		} catch (final Exception e) {
			final String message = StringUtil.format("Can't parse listFilterPattern {0}\n{1}", buildQuery, e.getMessage());
			throw WrappedException.wrap(e, message);
		}
	}

	/**
	 * @param buildQuery Builder pattern
	 * @return Parsed pattern
	 * @throws PegNoMatchFoundException If pattern doesn't match grammar
	 */
	private static List<DslMultiExpression> parseMultiExpressionWithError(final String buildQuery) throws PegNoMatchFoundException {
		return new DslSearchExpressionRule()
				.parse(buildQuery)
				.getValue();
	}

	/**
	 * @param userString User criteria
	 * @return Parsed User criteria
	 */
	public static List<DslUserCriteria> parseUserCriteria(final String userString) {
		return DslUserCriteriaRule.parse(userString);
	}

	/**
	 * @param geoString geo criteria
	 * @return Parsed GeoExpression
	 */
	public static DslGeoExpression parseGeoExpression(final String geoString) {
		return DslGeoExpressionRule.parse(geoString);
	}
}
