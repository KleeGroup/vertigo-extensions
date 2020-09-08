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
package io.vertigo.database.timeseries;

import java.io.Serializable;
import java.util.Map;

import io.vertigo.core.lang.Assertion;

/**
 * @author mlaroche
 *
 */
public final class DataFilter implements Serializable {

	private static final long serialVersionUID = -5464636083784385506L;

	private final String measurement;
	private final Map<String, String> filters;

	private final String additionalWhereClause;// may be null

	DataFilter(
			final String measurement,
			final Map<String, String> filters,
			final String additionalWhereClause) {
		Assertion.check()
				.isNotNull(measurement)
				.isNotNull(filters);
		//---
		this.measurement = measurement;
		this.filters = filters;
		this.additionalWhereClause = additionalWhereClause;
	}

	public static DataFilterBuilder builder(final String measurement) {
		return new DataFilterBuilder(measurement);
	}

	public String getAdditionalWhereClause() {
		return additionalWhereClause;
	}

	public Map<String, String> getFilters() {
		return filters;
	}

	public String getMeasurement() {
		return measurement;
	}

}
