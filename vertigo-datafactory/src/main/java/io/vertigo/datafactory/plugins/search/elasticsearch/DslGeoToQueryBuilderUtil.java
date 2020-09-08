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
package io.vertigo.datafactory.plugins.search.elasticsearch;

import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.util.BeanUtil;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoDistanceQuery;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoExpression;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoPointCriteria;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoPointFixed;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoRangeQuery;
import io.vertigo.datafactory.impl.search.dsl.model.DslQuery;

public final class DslGeoToQueryBuilderUtil {
	private static final String USER_QUERY_KEYWORD = "query";

	private DslGeoToQueryBuilderUtil() {
		//private for util class
	}

	public static QueryBuilder translateToQueryBuilder(final DslGeoExpression dslGeoExpression, final Object myCriteria, final Map<Class, BasicTypeAdapter> typeAdapters) {
		final String fieldName = dslGeoExpression.getField().getFieldName();
		final DslQuery geoQuery = dslGeoExpression.getGeoQuery();
		if (geoQuery instanceof DslGeoDistanceQuery) {
			return translateToGeoDistanceQuery(fieldName, (DslGeoDistanceQuery) geoQuery, myCriteria, typeAdapters);
		} else if (geoQuery instanceof DslGeoRangeQuery) {
			//range : boundingbox or distance range
			final DslGeoRangeQuery geoRangeQuery = (DslGeoRangeQuery) geoQuery;
			final DslQuery dslStartGeoPoint = geoRangeQuery.getStartGeoPoint();
			final DslQuery dslEndGeoPoint = geoRangeQuery.getEndGeoPoint();
			if (dslStartGeoPoint instanceof DslGeoDistanceQuery || dslEndGeoPoint instanceof DslGeoDistanceQuery) { //range by distance
				Assertion.check().isTrue(dslStartGeoPoint instanceof DslGeoDistanceQuery && dslEndGeoPoint instanceof DslGeoDistanceQuery, "When using query by range geo distance, start AND end must be GeoDistanceQuery ({0})", dslGeoExpression);
				//----
				final QueryBuilder startGeoDistanceQuery = translateToGeoDistanceQuery(fieldName, (DslGeoDistanceQuery) dslStartGeoPoint, myCriteria, typeAdapters);
				final QueryBuilder endGeoDistanceQuery = translateToGeoDistanceQuery(fieldName, (DslGeoDistanceQuery) dslEndGeoPoint, myCriteria, typeAdapters);
				final BoolQueryBuilder rangeDistanceBoolQueryBuilder = QueryBuilders.boolQuery();
				return rangeDistanceBoolQueryBuilder //ie: !(dist <= min) && (dist <= max)
						.must(endGeoDistanceQuery)
						.mustNot(startGeoDistanceQuery);
			}
			final GeoPoint geoPointTopLeft = computeGeoPoint(geoRangeQuery.getStartGeoPoint(), myCriteria, typeAdapters);
			final GeoPoint geoPointBottomRight = computeGeoPoint(geoRangeQuery.getEndGeoPoint(), myCriteria, typeAdapters);
			if (geoPointTopLeft != null || geoPointBottomRight != null) {
				return QueryBuilders.geoBoundingBoxQuery(fieldName)
						.setCorners(geoPointTopLeft, geoPointBottomRight);
			}
			return QueryBuilders.matchAllQuery();

		}
		throw new VSystemException("Can't translate toGeoQuery " + dslGeoExpression);
	}

	private static QueryBuilder translateToGeoDistanceQuery(final String fieldName, final DslGeoDistanceQuery dslGeoDistanceQuery, final Object myCriteria, final Map<Class, BasicTypeAdapter> typeAdapters) {
		final GeoPoint geoPoint = computeGeoPoint(dslGeoDistanceQuery.getGeoPoint(), myCriteria, typeAdapters);
		if (geoPoint != null) {
			return QueryBuilders.geoDistanceQuery(fieldName)
					.point(geoPoint)
					.distance(dslGeoDistanceQuery.getDistance(), DistanceUnit.fromString(dslGeoDistanceQuery.getDistanceUnit()));
		}
		return QueryBuilders.matchAllQuery();
	}

	public static GeoPoint computeGeoPoint(final DslQuery dslGeoPoint, final Object myCriteria, final Map<Class, BasicTypeAdapter> typeAdapters) {
		Assertion.check().isTrue(dslGeoPoint instanceof DslGeoPointFixed
				|| dslGeoPoint instanceof DslGeoPointCriteria, "geoPoint must be a fixed gedoPoint or a criteria ({0})", dslGeoPoint);
		final GeoPoint esGeoPoint;
		if (dslGeoPoint instanceof DslGeoPointFixed) {
			esGeoPoint = new GeoPoint(((DslGeoPointFixed) dslGeoPoint).getGeoPointValue());
		} else if (dslGeoPoint instanceof DslGeoPointCriteria) {
			final String fieldName = ((DslGeoPointCriteria) dslGeoPoint).getGeoPointFieldName();
			final String geoPointValue;
			if (USER_QUERY_KEYWORD.equalsIgnoreCase(fieldName)) {
				geoPointValue = cleanGeoCriteria(myCriteria);
			} else {
				Object geoPoint = BeanUtil.getValue(myCriteria, fieldName);
				if (geoPoint != null && typeAdapters.get(geoPoint.getClass()) != null) {
					geoPoint = typeAdapters.get(geoPoint.getClass()).toBasic(geoPoint);
				}
				geoPointValue = cleanGeoCriteria(geoPoint);
			}
			if (geoPointValue == null) {
				return null;
			}
			esGeoPoint = new GeoPoint(geoPointValue);
		} else {
			throw new VSystemException("Can't compute geoPoint for " + dslGeoPoint);
		}
		return esGeoPoint;
	}

	private static String cleanGeoCriteria(final Object geoPointValue) {
		if (geoPointValue == null) {
			return null;
		}
		return String.valueOf(geoPointValue);
		//replaceAll "(?i)((?<=\\S\\s)(or|and)(?=\\s\\S))"
		//replaceAll "(?i)((?<=\\s)(or|and)(?=\\s))"
	}
}
