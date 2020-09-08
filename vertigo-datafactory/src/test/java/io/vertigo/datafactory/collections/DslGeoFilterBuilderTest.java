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
package io.vertigo.datafactory.collections;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.datafactory.impl.search.dsl.model.DslGeoExpression;
import io.vertigo.datafactory.impl.search.dsl.rules.DslParserUtil;
import io.vertigo.datafactory.plugins.search.elasticsearch.DslGeoToQueryBuilderUtil;
import io.vertigo.datafactory.search.data.domain.GeoPoint;
import io.vertigo.datafactory.search.data.domain.GeoPointAdapter;

/**
 * @author  npiedeloup
 */
public final class DslGeoFilterBuilderTest {
	private static final Map<Class, BasicTypeAdapter> testTypeAdapters = Collections.singletonMap(GeoPoint.class, new GeoPointAdapter());

	@Test
	public void testStringGeoQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult, OtherAcceptedResult ...
				{ "localisation:#query#~10km", "10.0,20.0", "{\"geo_distance\":{\"localisation\":[20.0,10.0],\"distance\":10000.0,\"distance_type\":\"arc\",\"validation_method\":\"STRICT\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //0
				{ "localisation:#query#~10m", "10.05,20.05", "{\"geo_distance\":{\"localisation\":[20.05,10.05],\"distance\":10.0,\"distance_type\":\"arc\",\"validation_method\":\"STRICT\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //1
				{ "localisation:\"10.0,20.0\"~10m", "", "{\"geo_distance\":{\"localisation\":[20.0,10.0],\"distance\":10.0,\"distance_type\":\"arc\",\"validation_method\":\"STRICT\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //1
				{ "localisation:#query#~10km", "-10.0,-20.0", "{\"geo_distance\":{\"localisation\":[-20.0,-10.0],\"distance\":10000.0,\"distance_type\":\"arc\",\"validation_method\":\"STRICT\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //0
				{ "localisation:\"-10.0,-20.0\"~10m", "", "{\"geo_distance\":{\"localisation\":[-20.0,-10.0],\"distance\":10.0,\"distance_type\":\"arc\",\"validation_method\":\"STRICT\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //1
				{ "localisation:[#query# to \"08.0,22.0\"]", "10.0,20.0", "{\"geo_bounding_box\":{\"localisation\":{\"top_left\":[20.0,10.0],\"bottom_right\":[22.0,8.0]},\"validation_method\":\"STRICT\",\"type\":\"MEMORY\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //2
				{ "localisation:[\"12.0,18.0\" to #query#]", "10.0,20.0", "{\"geo_bounding_box\":{\"localisation\":{\"top_left\":[18.0,12.0],\"bottom_right\":[20.0,10.0]},\"validation_method\":\"STRICT\",\"type\":\"MEMORY\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //3

		};
		testStringFixedGeoQuery(testQueries);
	}

	@Test
	public void testBeanQuery() {
		final GeoPoint geo1 = new GeoPoint(12, 22);
		final GeoPoint geo2 = new GeoPoint(8, 18);

		final TestBean testBean = new TestBean("10.05,20.0", "10.0,20.05", geo1, geo2);
		final Object[][] testQueries = new Object[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "localisation:#str1#~10m", testBean, "{\"geo_distance\":{\"localisation\":[20.0,10.05],\"distance\":10.0,\"distance_type\":\"arc\",\"validation_method\":\"STRICT\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //0
				{ "localisation:#geo1#~10m", testBean, "{\"geo_distance\":{\"localisation\":[22.0,12.0],\"distance\":10.0,\"distance_type\":\"arc\",\"validation_method\":\"STRICT\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //1
				{ "localisation:#null#~10m", testBean, "{\"match_all\":{\"boost\":1.0}}" }, //1
				{ "localisation:[#str1# to #str2#]", testBean, "{\"geo_bounding_box\":{\"localisation\":{\"top_left\":[20.0,10.05],\"bottom_right\":[20.05,10.0]},\"validation_method\":\"STRICT\",\"type\":\"MEMORY\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //2
				{ "localisation:[#geo1# to #geo2#]", testBean, "{\"geo_bounding_box\":{\"localisation\":{\"top_left\":[22.0,12.0],\"bottom_right\":[18.0,8.0]},\"validation_method\":\"STRICT\",\"type\":\"MEMORY\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //2
				//{ "localisation:[#geo1# to #null#]", testBean, "{\"geo_bounding_box\":{\"localisation\":{\"top_left\":[20.0,10.0],\"bottom_right\":[22.0,8.0]},\"validation_method\":\"STRICT\",\"type\":\"MEMORY\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //2
				//{ "localisation:[#null# to #geo2#]", testBean, "{\"geo_bounding_box\":{\"localisation\":{\"top_left\":[20.0,10.0],\"bottom_right\":[22.0,8.0]},\"validation_method\":\"STRICT\",\"type\":\"MEMORY\",\"ignore_unmapped\":false,\"boost\":1.0}}" }, //2
				{ "localisation:[#null# to #null#]", testBean, "{\"match_all\":{\"boost\":1.0}}" }, //2
		};
		testObjectFixedGeoQuery(testQueries);
	}

	int getPreferedResult() {
		return 3;
	}

	private void testStringFixedGeoQuery(final String[]... testData) {
		int i = 0;
		for (final String[] testParam : testData) {
			final DslGeoExpression dslGeoExpression = DslParserUtil.parseGeoExpression(testParam[0]);
			String result = DslGeoToQueryBuilderUtil.translateToQueryBuilder(dslGeoExpression, testParam[1], testTypeAdapters).toString();
			final String expectedResult = testParam[Math.min(getPreferedResult(), testParam.length - 1)];
			result = result.replaceAll("[ \\t\\r\\n]+", "");
			Assertions.assertEquals(expectedResult, result, "Built query #" + i + " incorrect");
			i++;
		}
	}

	private void testObjectFixedGeoQuery(final Object[]... testData) {
		int i = 0;
		for (final Object[] testParam : testData) {
			final DslGeoExpression dslGeoExpression = DslParserUtil.parseGeoExpression((String) testParam[0]);
			String result = DslGeoToQueryBuilderUtil.translateToQueryBuilder(dslGeoExpression, testParam[1], testTypeAdapters).toString();
			final String expectedResult = (String) testParam[Math.min(getPreferedResult(), testParam.length - 1)];
			result = result.replaceAll("[ \\t\\r\\n]+", "");
			Assertions.assertEquals(expectedResult, result, "Built query #" + i + " incorrect");
			i++;
		}
	}

	public static class TestBean {

		private final String str1;
		private final String str2;
		private final io.vertigo.datafactory.search.data.domain.GeoPoint geo1;
		private final io.vertigo.datafactory.search.data.domain.GeoPoint geo2;

		TestBean(
				final String str1,
				final String str2,
				final io.vertigo.datafactory.search.data.domain.GeoPoint geo1,
				final io.vertigo.datafactory.search.data.domain.GeoPoint geo2) {
			this.str1 = str1;
			this.str2 = str2;
			this.geo1 = geo1;
			this.geo2 = geo2;
		}

		public String getStr1() {
			return str1;
		}

		public String getStr2() {
			return str2;
		}

		public io.vertigo.datafactory.search.data.domain.GeoPoint getGeo1() {
			return geo1;
		}

		public io.vertigo.datafactory.search.data.domain.GeoPoint getGeo2() {
			return geo2;
		}

		public Boolean getBooNull() {
			return null;
		}

		public boolean getBooTrue() {
			return true;
		}

		public Object getNull() {
			return null;
		}

	}

}
