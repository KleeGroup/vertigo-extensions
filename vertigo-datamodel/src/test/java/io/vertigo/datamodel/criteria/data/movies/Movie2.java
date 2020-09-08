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
package io.vertigo.datamodel.criteria.data.movies;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.stereotype.Field;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * Movie.
 */
public final class Movie2 implements Entity {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Field(smartType = "STyId", type = "ID", cardinality = Cardinality.ONE, label = "id of the movie")
	private Long id;
	@Field(smartType = "STyString", label = "title")
	private String title;
	@Field(smartType = "STyInteger", label = "year")
	private Integer year;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(final String title) {
		this.title = title;
	}

	public final Integer getYear() {
		return year;
	}

	public final void setYear(final Integer year) {
		this.year = year;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}

	@Override
	public UID<Movie2> getUID() {
		return UID.of(Movie2.class, id);
	}
}
