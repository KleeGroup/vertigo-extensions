/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.ui.data.controllers;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertigo.account.security.VSecurityManager;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.core.ViewContextKey;
import io.vertigo.ui.data.domain.DtDefinitions.MovieDisplayFields;
import io.vertigo.ui.data.domain.movies.Movie;
import io.vertigo.ui.data.domain.movies.MovieDisplay;
import io.vertigo.ui.data.domain.people.Casting;
import io.vertigo.ui.data.domain.reference.Commune;
import io.vertigo.ui.data.services.movies.MovieServices;
import io.vertigo.ui.impl.springmvc.argumentresolvers.ViewAttribute;
import io.vertigo.ui.impl.springmvc.controller.AbstractVSpringMvcController;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.DefaultDtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.ValidationUserException;

@Controller
@RequestMapping("/componentsDemo")
public class ComponentsDemoController extends AbstractVSpringMvcController {

	private final ViewContextKey<Movie> movieKey = ViewContextKey.of("movie");
	private final ViewContextKey<Casting> castingKey = ViewContextKey.of("casting");
	private final ViewContextKey<Movie> movieList = ViewContextKey.of("movies");
	private final ViewContextKey<Movie> movieListModifiables = ViewContextKey.of("moviesModifiable");
	private final ViewContextKey<Movie> moviesListMdl = ViewContextKey.of("moviesMdl");

	private final ViewContextKey<String> communeId = ViewContextKey.of("communeId");
	private final ViewContextKey<Commune> communeListMdl = ViewContextKey.of("communesMdl");
	private final ViewContextKey<MovieDisplay> movieDisplayList = ViewContextKey.of("moviesDisplay");

	private final ViewContextKey<Instant> currentInstant = ViewContextKey.of("currentInstant");
	private final ViewContextKey<String> currentZoneId = ViewContextKey.of("currentZoneId");

	private static final String[] timeZoneListStatic = { "Europe/Paris", "America/Cayenne", "Indian/Reunion" };
	private final ViewContextKey<String[]> timeZoneList = ViewContextKey.of("timeZoneList");
	private final ViewContextKey<String> zoneId = ViewContextKey.of("zoneId");

	@Inject
	private VSecurityManager securityManager;

	@Inject
	private LocaleManager localeManager;

	@Inject
	private MovieServices movieServices;

	@GetMapping("/")
	public void initContext(final ViewContext viewContext) {
		viewContext.publishDto(movieKey, new Movie());
		viewContext.publishDto(castingKey, new Casting());
		viewContext.publishDtList(movieList, movieServices.getMovies(DtListState.defaultOf(Movie.class)));
		viewContext.publishDtListModifiable(movieListModifiables, movieServices.getMovies(DtListState.defaultOf(Movie.class)));
		viewContext.publishMdl(moviesListMdl, Movie.class, null);
		viewContext.publishDtList(movieDisplayList, MovieDisplayFields.movId, movieServices.getMoviesDisplay(DtListState.defaultOf(Movie.class)));

		viewContext.publishMdl(communeListMdl, Commune.class, null);

		viewContext.publishRef(currentInstant, Instant.now());
		viewContext.publishRef(currentZoneId, localeManager.getCurrentZoneId().getId());
		viewContext.publishRef(zoneId, timeZoneListStatic[0]);
		viewContext.publishRef(timeZoneList, timeZoneListStatic);

		toModeCreate();
	}

	@PostMapping("/movies/{movieId}")
	public void doSaveMovie(final ViewContext viewContext,
			@ViewAttribute("movie") final UiObject<Movie> movieUiObject,
			@PathVariable("movieId") final Long movieId,
			final UiMessageStack uiMessageStack) {
		movieUiObject.mergeAndCheckInput(Collections.singletonList(new DefaultDtObjectValidator<Movie>()), uiMessageStack);
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}

		viewContext.publishRef(currentInstant, Instant.now());
	}

	@PostMapping("/movies/_add")
	public void addMovieList(final ViewContext viewContext, @ViewAttribute("movieListModifiables") final DtList<Movie> movies) {
		movies.add(new Movie());
		viewContext.publishDtListModifiable(movieListModifiables, movies);
	}

	@PostMapping("/movies")
	public void saveList(final ViewContext viewContext, @ViewAttribute("movieListModifiables") final DtList<Movie> movies) {
		viewContext.publishDtListModifiable(movieListModifiables, movies);
	}

	@PostMapping("/casting")
	public void doSaveCasting(@ViewAttribute("casting") final Casting casting) {
		nop(casting);
	}

	@PostMapping("/commune")
	public void doSaveCommune(final ViewContext viewContext, @ViewAttribute("communeId") final String _communeId) {
		viewContext.publishRef(communeId, _communeId);
	}

	@PostMapping("/_saveInstant")
	public void saveInstant(final ViewContext viewContext, @ViewAttribute("movie") final Movie movie) {
		viewContext.publishRef(currentInstant, movie.getLastModified());

		final TestUserSession userSession = securityManager.<TestUserSession> getCurrentUserSession().get();
		userSession.setZoneId(ZoneId.of(zoneId.get()));
		viewContext.publishRef(currentZoneId, localeManager.getCurrentZoneId().getId());
	}

	@PostMapping("/_read")
	public void toRead() {
		toModeReadOnly();
	}

	@PostMapping("/_edit")
	public void toEdit() {
		toModeEdit();
	}

	private void nop(final Object obj) {
		//just nothing
	}
}
