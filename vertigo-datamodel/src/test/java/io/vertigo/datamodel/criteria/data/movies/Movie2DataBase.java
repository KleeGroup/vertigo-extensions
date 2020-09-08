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

import java.util.List;

import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.util.VCollectors;

/**
 *
 * @author pchretien
 */
public final class Movie2DataBase {
	private final List<Movie2> movies = List.of(
			createMovie2(1933, "king kong"),
			createMovie2(1951, "pandora"),
			createMovie2(1959, "vertigo"),
			createMovie2(1979, "alien"),
			createMovie2(1980, "shining"),
			createMovie2(1984, "amadeus"),
			createMovie2(1984, "1984"),
			createMovie2(1984, "terminator"),
			createMovie2(1985, "porco rosso"),
			createMovie2(2000, "gladiator"),
			createMovie2(2014, "interstellar"),
			createMovie2(2014, "mommy"),
			createMovie2(null, "ordet"));

	private static Movie2 createMovie2(final Integer year, final String title) {
		final Movie2 movie = new Movie2();
		movie.setYear(year);
		movie.setTitle(title);
		return movie;
	}

	public final DtList<Movie2> getAllMovies() {
		return movies
				.stream()
				.collect(VCollectors.toDtList(Movie2.class));
	}
}
