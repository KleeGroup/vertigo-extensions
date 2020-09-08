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

import io.vertigo.core.lang.Assertion;

/**
 * Range geo query definition.
 * (preBody)[(geoCriteria|geoFixedQuery) to (geoCriteria|geoFixedQuery)](postBody)
 * @author npiedeloup
 */
public final class DslGeoRangeQuery implements DslQuery {
	private final String preBody;
	private final DslQuery startGeoPoint;
	private final DslQuery endGeoPoint;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param startGeoPoint Start query
	 * @param endGeoPoint End query
	 * @param postBody String after body
	 */
	public DslGeoRangeQuery(
			final String preBody,
			final DslQuery startGeoPoint,
			final DslQuery endGeoPoint,
			final String postBody) {
		Assertion.check().isNotNull(preBody)
				.isNotNull(startGeoPoint)
				.isNotNull(endGeoPoint)
				.isTrue(startGeoPoint instanceof DslGeoPointCriteria
						|| startGeoPoint instanceof DslGeoPointFixed
						|| startGeoPoint instanceof DslGeoDistanceQuery, "DslGeoDistanceQuery only support Criteria or Fixed startGeoPoint, may have distance and unit ({0})", startGeoPoint)
				.isTrue(endGeoPoint instanceof DslGeoPointCriteria
						|| endGeoPoint instanceof DslGeoPointFixed
						|| endGeoPoint instanceof DslGeoDistanceQuery, "DslGeoDistanceQuery only support Criteria or Fixed endGeoPoint, may have distance and unit ({0})", endGeoPoint)
				.isNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.startGeoPoint = startGeoPoint;
		this.endGeoPoint = endGeoPoint;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(preBody).append("[")
				.append(startGeoPoint)
				.append(" to ")
				.append(endGeoPoint)
				.append("]").append(postBody)
				.toString();
	}

	/**
	 * @return preBody
	 */
	public String getPreBody() {
		return preBody;
	}

	/**
	 * @return startGeoPoint
	 */
	public DslQuery getStartGeoPoint() {
		return startGeoPoint;
	}

	/**
	 * @return endGeoPoint
	 */
	public DslQuery getEndGeoPoint() {
		return endGeoPoint;
	}

	/**
	 * @return postBody
	 */
	public String getPostBody() {
		return postBody;
	}
}
