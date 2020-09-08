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
import java.time.Instant;
import java.util.Map;

import io.vertigo.core.lang.Assertion;

/*
 * Couple(date, metriques)
 * @author pchretien, npiedeloup, mlaroche
 */
public final class TimedDataSerie implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Instant time;
	private final Map<String, Object> values;

	public TimedDataSerie(final Instant time, final Map<String, Object> values) {
		Assertion.check()
				.isNotNull(time)
				.isNotNull(values);
		//---
		this.time = time;
		this.values = values;
	}

	public Instant getTime() {
		return time;
	}

	public Map<String, Object> getValues() {
		return values;
	}
}
