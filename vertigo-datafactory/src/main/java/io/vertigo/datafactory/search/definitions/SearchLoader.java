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
package io.vertigo.datafactory.search.definitions;

import java.util.List;

import io.vertigo.core.node.component.Component;
import io.vertigo.datafactory.search.model.SearchIndex;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.KeyConcept;

/**
 * Specific SearchIndex loader.
 * @param <K> KeyConcept
 * @param <I> Indexed data's type
 * @author npiedeloup, pchretien
 */
public interface SearchLoader<K extends KeyConcept, I extends DtObject> extends Component {
	/**
	 * Load all data from a list of keyConcepts.
	 * @param searchChunk the chunk
	 * @return List of searchIndex
	 */
	List<SearchIndex<K, I>> loadData(SearchChunk<K> searchChunk);

	/**
	 * Create a chunk iterator for crawl all keyConcept data.
	 * @param keyConceptClass keyConcept class
	 * @return Iterator of chunk
	 */
	Iterable<SearchChunk<K>> chunk(final Class<K> keyConceptClass);
}
