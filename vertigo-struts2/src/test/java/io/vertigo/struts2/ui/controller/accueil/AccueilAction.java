/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.struts2.ui.controller.accueil;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.file.model.FSFile;
import io.vertigo.lang.VUserException;
import io.vertigo.struts2.core.AbstractActionSupport.AcceptCtxQueryParam;
import io.vertigo.struts2.core.ContextForm;
import io.vertigo.struts2.core.ContextList;
import io.vertigo.struts2.core.ContextListModifiable;
import io.vertigo.struts2.core.ContextMdl;
import io.vertigo.struts2.core.ContextRef;
import io.vertigo.struts2.core.ContextVFile;
import io.vertigo.struts2.core.GET;
import io.vertigo.struts2.domain.DtDefinitions.MovieDisplayFields;
import io.vertigo.struts2.domain.movies.Movie;
import io.vertigo.struts2.domain.movies.MovieDisplay;
import io.vertigo.struts2.domain.people.Casting;
import io.vertigo.struts2.domain.reference.Commune;
import io.vertigo.struts2.services.movies.MovieServices;
import io.vertigo.struts2.ui.controller.AbstractTestActionSupport;
import io.vertigo.vega.webservice.validation.UiMessageStack.Level;

@AcceptCtxQueryParam
public class AccueilAction extends AbstractTestActionSupport {

	private static final long serialVersionUID = 1L;

	private final ContextForm<Movie> movie = new ContextForm<>("movie", this);
	private final ContextForm<Casting> casting = new ContextForm<>("casting", this);
	private final ContextList<Movie> movieList = new ContextList<>("movies", this);
	private final ContextListModifiable<Movie> movieListModifiables = new ContextListModifiable<>("moviesModifiable", this);
	private final ContextMdl<Movie> moviesListMdl = new ContextMdl<>("moviesMdl", this);
	private final ContextRef<String> communeId = new ContextRef<>("communeId", String.class, this);
	private final ContextMdl<Commune> communeListMdl = new ContextMdl<>("communesMdl", this);

	private final ContextList<MovieDisplay> movieDisplayList = new ContextList<>("moviesDisplay", MovieDisplayFields.MOV_ID, this);

	private final ContextRef<String> currentDate = new ContextRef<>("currentDate", String.class, this);

	private final ContextVFile fileTestFileRef = new ContextVFile("fileTest", this);

	@Inject
	private MovieServices movieServices;

	@Override
	protected void initContext() {
		movie.publish(new Movie());
		casting.publish(new Casting());
		movieList.publish(movieServices.getMovies(new DtListState(200, 0, null, null)));
		movieListModifiables.publish(movieServices.getMovies(new DtListState(200, 0, null, null)));
		moviesListMdl.publish(Movie.class, null);
		communeListMdl.publish(Commune.class, null);
		movieDisplayList.publish(movieServices.getMoviesDisplay(new DtListState(200, 0, null, null)));

		toModeCreate();
		currentDate.set(new Date().toString());
	}

	public String save() {
		movie.readDto();
		currentDate.set(new Date().toString());
		return NONE;
	}

	public String saveCasting() {
		casting.readDto();
		return NONE;
	}

	public String saveCommune() {
		communeId.set(communeId.get());
		return NONE;
	}

	public String addMovieList() {
		movieListModifiables.getUiListModifiable().add(new Movie());
		return NONE;
	}

	public String saveList() {
		movieListModifiables.readDtList();
		return NONE;
	}

	public String uploadFile() {
		if (!fileTestFileRef.exists()) {
			throw new VUserException("Aucun fichier uploadé.");
		}
		final VFile vFile = fileTestFileRef.get();
		getUiMessageStack().addGlobalMessage(Level.INFO, "Fichier recu : " + vFile.getFileName() + " (" + vFile.getMimeType() + ")");
		return NONE;
	}

	/**
	 * Exporte l'annuaire utilisateur.
	 * @return redirection struts
	 */
	@GET
	public String downloadFile() {
		final String fullPath = getClass().getResource("/data/insee.csv").getFile();
		final File localFile = new File(fullPath);
		final VFile vFile = new FSFile("insee.csv", "text/csv", localFile);
		return createVFileResponseBuilder().send(vFile);
	}

	public String toRead() {
		toModeReadOnly();
		return NONE;
	}

	public String toEdit() {
		toModeEdit();
		return NONE;
	}

	@Override
	public String getPageName() {
		return "Accueil";
	}
}