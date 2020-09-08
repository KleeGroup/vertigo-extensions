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
package io.vertigo.ui.data.services.movies;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.datafactory.impl.search.loader.AbstractSqlSearchLoader;
import io.vertigo.datafactory.search.SearchManager;
import io.vertigo.datafactory.search.definitions.SearchChunk;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.model.SearchIndex;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.ui.data.domain.movies.Movie;
import io.vertigo.ui.data.domain.movies.MovieIndex;

public final class MovieSearchLoader extends AbstractSqlSearchLoader<Long, Movie, MovieIndex> {

	private final MovieServices movieServices;
	private final SearchManager searchManager;

	@Inject
	public MovieSearchLoader(final TaskManager taskManager, final SearchManager searchManager, final VTransactionManager transactionManager, final MovieServices movieServices) {
		super(taskManager, transactionManager);
		this.searchManager = searchManager;
		this.movieServices = movieServices;
	}

	@Override
	public List<SearchIndex<Movie, MovieIndex>> loadData(final SearchChunk<Movie> searchChunk) {
		final SearchIndexDefinition indexDefinition = searchManager.findFirstIndexDefinitionByKeyConcept(Movie.class);
		final List<Long> movieIds = new ArrayList<>();
		for (final UID<Movie> uri : searchChunk.getAllUIDs()) {
			movieIds.add((Long) uri.getId());
		}
		final DtList<MovieIndex> movieIndexes = movieServices.getMovieIndex(movieIds);
		final List<SearchIndex<Movie, MovieIndex>> movieSearchIndexes = new ArrayList<>(searchChunk.getAllUIDs().size());
		for (final MovieIndex movieIndex : movieIndexes) {
			movieSearchIndexes.add(SearchIndex.createIndex(indexDefinition,
					UID.of(indexDefinition.getKeyConceptDtDefinition(), movieIndex.getMovId()), movieIndex));
		}
		return movieSearchIndexes;
	}

}
