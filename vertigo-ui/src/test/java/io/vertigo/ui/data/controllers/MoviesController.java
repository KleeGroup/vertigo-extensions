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
package io.vertigo.ui.data.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.util.VCollectors;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.core.ViewContextKey;
import io.vertigo.ui.data.domain.DtDefinitions.MovieDisplayFields;
import io.vertigo.ui.data.domain.DtDefinitions.MovieIndexFields;
import io.vertigo.ui.data.domain.movies.Movie;
import io.vertigo.ui.data.domain.movies.MovieDisplay;
import io.vertigo.ui.data.domain.movies.MovieIndex;
import io.vertigo.ui.data.services.movies.MovieServices;
import io.vertigo.ui.impl.springmvc.argumentresolvers.ViewAttribute;
import io.vertigo.ui.impl.springmvc.controller.AbstractVSpringMvcController;

@Controller
@RequestMapping("/movies")
public final class MoviesController extends AbstractVSpringMvcController {

	private static final ViewContextKey<String> CRITERIA = ViewContextKey.of("criteria");
	private static final ViewContextKey<MovieDisplay> MOVIES = ViewContextKey.of("movies");
	private static final ViewContextKey<FacetedQueryResult<MovieIndex, SearchQuery>> FCTS = ViewContextKey.of("result");

	@Autowired
	private MovieServices movieServices;
	@Autowired
	private EntityStoreManager entityStoreManager;

	@GetMapping("/")
	public void initContext(final ViewContext viewContext) {
		final DtListState dtListState = DtListState.defaultOf(Movie.class);
		final DtList<MovieDisplay> sortedList = movieServices.getMoviesDisplay(dtListState);
		viewContext.publishDtList(MOVIES, MovieDisplayFields.movId, sortedList);
		viewContext.publishRef(CRITERIA, "");
		final FacetedQueryResult<MovieIndex, SearchQuery> facetedQueryResult = movieServices.searchMovies("", SelectedFacetValues.empty().build(), dtListState, Optional.empty());
		viewContext.publishFacetedQueryResult(FCTS, MovieIndexFields.movId, facetedQueryResult, CRITERIA);
	}

	@PostMapping("/_search")
	public ViewContext doSearch(
			final ViewContext viewContext,
			@ViewAttribute("criteria") final String criteria,
			@ViewAttribute("result") final SelectedFacetValues selectedFacetValues,
			final DtListState dtListState) {
		final FacetedQueryResult<MovieIndex, SearchQuery> facetedQueryResult = movieServices.searchMovies(criteria, selectedFacetValues, dtListState, Optional.empty());
		viewContext.publishFacetedQueryResult(FCTS, MovieIndexFields.movId, facetedQueryResult, CRITERIA);
		return viewContext;
	}

	@PostMapping("/_sort")
	public ViewContext doSort(final ViewContext viewContext, @ViewAttribute("movies") final DtList<MovieDisplay> moviesDisplay, final DtListState dtListState) {
		final DtList<MovieDisplay> sortedDtList = applySortAndPagination(moviesDisplay, dtListState);
		viewContext.publishDtList(MOVIES, MovieDisplayFields.movId, sortedDtList);
		return viewContext;
	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final DtListState dtListState) {
		final DtList<D> sortedList;
		if (dtListState.getSortFieldName().isPresent()) {
			sortedList = entityStoreManager.sort(unFilteredList, dtListState.getSortFieldName().get(), dtListState.isSortDesc().get());
		} else {
			sortedList = unFilteredList;
		}
		if (dtListState.getSkipRows() >= sortedList.size()) {
			return new DtList<>(unFilteredList.getDefinition());
		} else if (dtListState.getMaxRows().isPresent()) {
			return sortedList
					.stream()
					.skip(dtListState.getSkipRows())
					.limit(dtListState.getMaxRows().get())
					.collect(VCollectors.toDtList(sortedList.getDefinition()));
		}
		return sortedList;
	}
}
