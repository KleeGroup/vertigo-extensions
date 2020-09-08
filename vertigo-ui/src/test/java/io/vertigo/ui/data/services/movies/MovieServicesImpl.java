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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.node.Node;
import io.vertigo.datafactory.collections.definitions.FacetDefinition;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datafactory.search.model.SearchQueryBuilder;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.util.VCollectors;
import io.vertigo.ui.data.dao.movies.MovieDAO;
import io.vertigo.ui.data.dao.movies.MoviesPAO;
import io.vertigo.ui.data.domain.movies.Movie;
import io.vertigo.ui.data.domain.movies.MovieDisplay;
import io.vertigo.ui.data.domain.movies.MovieIndex;
import io.vertigo.ui.data.search.movies.MovieSearchClient;

@Transactional
public class MovieServicesImpl implements MovieServices {

	@Inject
	private MovieDAO movieDAO;
	@Inject
	private MovieSearchClient movieSearchClient;
	@Inject
	private MoviesPAO moviesPAO;

	@Override
	public Movie get(final Long movId) {
		return movieDAO.get(movId);
	}

	@Override
	public void save(final Movie movie) {
		movieDAO.save(movie);
	}

	@Override
	@Transactional
	public DtList<Movie> getMovies(final DtListState dtListState) {
		return movieDAO.findAll(Criterions.alwaysTrue(), dtListState);
	}

	@Override
	@Transactional
	public DtList<MovieDisplay> getMoviesDisplay(final DtListState dtListState) {
		return movieDAO.findAll(Criterions.alwaysTrue(), dtListState)
				.stream()
				.map(movie -> {
					final MovieDisplay movieDisplay = new MovieDisplay();
					movieDisplay.setMovId(movie.getMovId());
					movieDisplay.setTitle(movie.getTitle());
					return movieDisplay;
				})
				.collect(VCollectors.toDtList(MovieDisplay.class));
	}

	@Override
	public FacetedQueryResult<MovieIndex, SearchQuery> searchMovies(final String criteria, final SelectedFacetValues listFilters, final DtListState dtListState, final Optional<String> group) {
		final SearchQueryBuilder searchQueryBuilder = movieSearchClient.createSearchQueryBuilderMovie(criteria, listFilters);
		if (group.isPresent()) {
			final FacetDefinition clusteringFacetDefinition = Node.getNode().getDefinitionSpace().resolve(group.get(), FacetDefinition.class);
			searchQueryBuilder.withFacetClustering(clusteringFacetDefinition);
		}
		return movieSearchClient.loadList(searchQueryBuilder.build(), dtListState);
	}

	@Override
	public DtList<MovieIndex> getMovieIndex(final List<Long> movieIds) {
		return moviesPAO.loadMovieIndex(movieIds);
	}

}
