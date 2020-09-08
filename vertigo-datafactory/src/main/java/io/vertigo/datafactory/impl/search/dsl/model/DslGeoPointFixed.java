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
 * Fixed GeoPoint.
 * 40.73, -74.14
 * @author npiedeloup
 */
public final class DslGeoPointFixed implements DslQuery {
	private final String geoPointValue;

	/**
	 * @param geoPoint geoPoint value
	 */
	public DslGeoPointFixed(final String geoPointValue) {
		Assertion.check().isNotBlank(geoPointValue);
		//-----
		this.geoPointValue = geoPointValue;
	}

	/**
	 * @return geoPointValue
	 */
	public String getGeoPointValue() {
		return geoPointValue;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "\"" + geoPointValue + "\"";
	}
}
