/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.commons.analytics.metric;

import java.time.Instant;

import io.vertigo.lang.Assertion;

/**
 * Interface décrivant un résultat de metric.
 *
 * @author mlaroche, pchretien
 */
public final class Metric {

	public enum Status {
		/** Exécution OK*/
		SUCCESS,
		/** Erreur lors de l'exécution*/
		ERROR;
	}

	private final Instant measureInstant;
	private final String name;
	private final String module;// may be null for now
	private final String feature;
	private final Double value;//migth be null
	private final Status status;

	Metric(
			final Instant measureTime,
			final String name,
			final String module,
			final String feature,
			final Double value,
			final Status status) {
		Assertion.checkNotNull(measureTime);
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgNotEmpty(feature);
		Assertion.checkNotNull(status);
		//-----
		measureInstant = measureTime;
		this.name = name;
		this.module = module;
		this.feature = feature;
		this.value = value;
		this.status = status;

	}

	/**
	 * Static method factory for ReportMetricBuilder
	 * @return ReportMetricBuilder
	 */
	public static MetricBuilder builder() {
		return new MetricBuilder();
	}

	public Instant getMeasureInstant() {
		return measureInstant;
	}

	public String getName() {
		return name;
	}

	public String getModule() {
		return module;
	}

	public String getFeature() {
		return feature;
	}

	public Double getValue() {
		return value;
	}

	public Status getStatus() {
		return status;
	}

}
