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
package io.vertigo.ui.data.boot.initializer;

import javax.inject.Inject;

import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.ComponentInitializer;
import io.vertigo.datafactory.search.SearchManager;
import io.vertigo.ui.data.domain.movies.Movie;

/**
 * Initializer de LocaleManager.
 * @author dchallas
 * @version $Id: LocaleManagerInitializer.java,v 1.4 2014/02/07 16:48:27 npiedeloup Exp $
 */
public final class SearchInitializer implements ComponentInitializer {

	@Inject
	private SearchManager searchManager;

	/** {@inheritDoc} */
	@Override
	public void init() {
		Node.getNode().registerPreActivateFunction(() -> searchManager.reindexAll(searchManager.findFirstIndexDefinitionByKeyConcept(Movie.class)));
	}
}
