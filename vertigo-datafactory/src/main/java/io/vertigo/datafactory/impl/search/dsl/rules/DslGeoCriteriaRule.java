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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datafactory.impl.search.dsl.model.DslField;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoDistanceQuery;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoExpression;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoPointCriteria;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoPointFixed;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoRangeQuery;
import io.vertigo.datafactory.impl.search.dsl.model.DslQuery;

/**
 * One geo expression.
 * A dev geo query :
 *
 * Distance :
 * fieldName:"40.73, -74.1"~10km
 * fieldName:#loc1#~10km
 *
 * Range :
 * fieldName:[#locUL# to #locBR#]
 * fieldName:[#loc1#~5km to #loc1#~10km]
 * fieldName:["20.73, -54.1" to "40.73, -74.1"]
 *
 * @author npiedeloup
 */
final class DslGeoExpressionRule {

	private static final String GEOLOC_FIXED_PATTERN = "\"(-?[0-9\\.]+\\s*,\\s*-?[0-9\\.]+)\"";
	private static final String GEOLOC_CRITERIA_PATTERN = "#([^#]+)#";

	private static final String GEO_FIELD_PATTERN = "([^\\s\\:]+)(?:\\s)*:(?:\\s)*";
	private static final String GEO_DISTANCE_PATTERN = "(?:"
			+ GEOLOC_FIXED_PATTERN + "|" + GEOLOC_CRITERIA_PATTERN //group geoLocFix | geoLocCrit
			+ ")"
			+ "+\\~([0-9\\.]+)([a-z]+)"; //group distance distanceUnit

	private static final String GEO_RANGE_PATTERN = "\\[\\s*(?:"
			+ GEOLOC_FIXED_PATTERN + "|" + GEOLOC_CRITERIA_PATTERN //group geoLocFix | geoLocCrit
			+ ")+(?:\\~([0-9\\.]+)([a-z]+))?\\s*(?:(?:to)|(?:TO))\\s*(?:"
			+ GEOLOC_FIXED_PATTERN + "|" + GEOLOC_CRITERIA_PATTERN //group geoLocFix | geoLocCrit
			+ ")+(?:\\~([0-9\\.]+)([a-z]+))?\\s*\\]";

	private static final String GEO_EXPRESSION_PATTERN_STRING = "(?:\\s|^)"
			+ GEO_FIELD_PATTERN //group 1
			+ "(?:" + GEO_DISTANCE_PATTERN // group 2-5
			+ "|" + GEO_RANGE_PATTERN // group 6-13
			+ ")(?=\\s|$)"; // group 14
	private static final Pattern GEO_EXPRESSION_PATTERN = Pattern.compile(GEO_EXPRESSION_PATTERN_STRING);

	/**
	 * @param geoString geo string
	 * @return Parsed list of DslGeoExpressionRule
	 */
	static DslGeoExpression parse(final String geoString) {
		final DslGeoExpression dslGeoExpression;
		final Matcher geoValueMatcher = GEO_EXPRESSION_PATTERN.matcher(geoString);
		Assertion.check().isTrue(geoValueMatcher.find(), "Can't parse geoExpression ({0})", geoString);

		if (geoValueMatcher.group(4) != null) {
			//found geoDistance
			final DslField geoField = new DslField("", geoValueMatcher.group(1), "");
			final DslQuery geoLoc;
			if (geoValueMatcher.group(2) != null) {
				geoLoc = new DslGeoPointFixed(geoValueMatcher.group(2));
			} else {
				geoLoc = new DslGeoPointCriteria(geoValueMatcher.group(3));
			}
			final int distance = Integer.parseInt(geoValueMatcher.group(4));
			final String distanceUnit = geoValueMatcher.group(5);
			final DslGeoDistanceQuery geoDistance = new DslGeoDistanceQuery(geoLoc, distance, distanceUnit);
			dslGeoExpression = new DslGeoExpression(geoField, geoDistance);

		} else if (geoValueMatcher.group(6) != null || geoValueMatcher.group(7) != null) {
			//found geoBoundingBox
			final DslField geoField = new DslField("", geoValueMatcher.group(1), "");
			DslQuery geoLoc1;
			if (geoValueMatcher.group(6) != null) {
				geoLoc1 = new DslGeoPointFixed(geoValueMatcher.group(6));
			} else {
				geoLoc1 = new DslGeoPointCriteria(geoValueMatcher.group(7));
			}
			if (geoValueMatcher.group(8) != null) {
				final int distance = Integer.parseInt(geoValueMatcher.group(8));
				final String distanceUnit = geoValueMatcher.group(9);
				geoLoc1 = new DslGeoDistanceQuery(geoLoc1, distance, distanceUnit);
			}
			DslQuery geoLoc2;
			if (geoValueMatcher.group(10) != null) {
				geoLoc2 = new DslGeoPointFixed(geoValueMatcher.group(10));
			} else {
				geoLoc2 = new DslGeoPointCriteria(geoValueMatcher.group(11));
			}
			if (geoValueMatcher.group(12) != null) {
				final int distance = Integer.parseInt(geoValueMatcher.group(12));
				final String distanceUnit = geoValueMatcher.group(13);
				geoLoc2 = new DslGeoDistanceQuery(geoLoc2, distance, distanceUnit);
			}

			final DslGeoRangeQuery geoRange = new DslGeoRangeQuery("", geoLoc1, geoLoc2, "");
			dslGeoExpression = new DslGeoExpression(geoField, geoRange);
		} else {
			throw new IllegalArgumentException("Can't parse geoExpression (" + geoString + ")");
		}
		return dslGeoExpression;
	}
}
