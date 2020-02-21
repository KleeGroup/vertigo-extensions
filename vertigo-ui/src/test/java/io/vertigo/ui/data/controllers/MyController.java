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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.core.ViewContextKey;
import io.vertigo.ui.data.domain.movies.Movie;
import io.vertigo.ui.data.services.movies.MovieServices;
import io.vertigo.ui.impl.springmvc.argumentresolvers.ViewAttribute;
import io.vertigo.ui.impl.springmvc.controller.AbstractVSpringMvcController;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.UiMessageStack;

@Controller
@RequestMapping("/my")
public class MyController extends AbstractVSpringMvcController {

	private static final ViewContextKey<Long> movIdKey = ViewContextKey.of("movId");
	private static final ViewContextKey<Movie> movieKey = ViewContextKey.of("movie");
	private static final ViewContextKey<Movie> movie2Key = ViewContextKey.of("movie2");
	private static final ViewContextKey<String> messageKey = ViewContextKey.of("message");

	@Autowired
	private MovieServices movieServices;

	@ModelAttribute
	public void postInit(final Model model) {
		model.addAttribute("postInit", "it works");
		// we are after vertigo initialization
	}

	@GetMapping("/")
	public void initContext(final ViewContext viewContext) {
		viewContext.publishRef(movIdKey, 1000L);
		final Movie movie = movieServices.get(viewContext.getLong(movIdKey));
		viewContext.publishDto(movieKey, movie);
		viewContext.publishDto(movie2Key, new Movie());
		viewContext.publishRef(messageKey, "Hello!  movie id :" + viewContext.getLong(movIdKey));
	}

	@PostMapping("/")
	public void doSave(final ViewContext viewContext,
			@ViewAttribute("movie") final UiObject<Movie> movieUiObject,
			@ViewAttribute("movie") final Movie movieDtObject,
			@ViewAttribute("movie2") final Movie movieDtObject2,
			@ViewAttribute("message") final String message,
			final UiMessageStack uiMessageStack) {
		final Movie movie = viewContext.readDto(movieKey, uiMessageStack);
		System.out.println(message);
		viewContext.publishRef(messageKey, "Movie retrieved in storedContext : " + movie.getTitle());
	}

}
