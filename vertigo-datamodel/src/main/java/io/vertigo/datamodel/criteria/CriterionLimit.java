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
package io.vertigo.datamodel.criteria;

import java.io.Serializable;

import io.vertigo.datamodel.structure.model.Entity;

/**
 * Defines a limit/boundary.
 * if limit is null then there is ... no limit
 * limit can be inclusive or exclusive.
 *
 * @author pchretien
 *
 * @param <E> the type of Entity
 */
public final class CriterionLimit<E extends Entity> implements Serializable {
	private static final long serialVersionUID = 3903274923414317496L;

	private final Serializable value;
	private final boolean included;// else excluded

	public static <E extends Entity> CriterionLimit<E> ofIncluded(final Serializable value) {
		return new CriterionLimit<>(value, true);
	}

	public static <E extends Entity> CriterionLimit<E> ofExcluded(final Serializable value) {
		return new CriterionLimit<>(value, false);
	}

	private CriterionLimit(final Serializable value, final boolean included) {
		this.value = value;
		this.included = included;
	}

	public boolean isDefined() {
		return value != null;
	}

	public Comparable getValue() {
		return Comparable.class.cast(value);
	}

	public boolean isIncluded() {
		return included;
	}
}
