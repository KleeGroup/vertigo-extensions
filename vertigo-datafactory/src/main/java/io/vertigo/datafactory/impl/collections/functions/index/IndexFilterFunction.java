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
package io.vertigo.datafactory.impl.collections.functions.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.impl.collections.IndexPlugin;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.DtObject;

/**
 * List Function powered with index engine.
 * @author npiedeloup (5 janv. 2015 10:47:08)
 * @param <D> Object type
 */
public final class IndexFilterFunction<D extends DtObject> implements UnaryOperator<DtList<D>> {

	private static final int DEFAULT_MAX_ROWS = 250;
	private String keywords;
	private Collection<DtField> searchedFields = Collections.emptyList();
	private final List<ListFilter> listFilters = new ArrayList<>();
	private int skip = 0;
	private int top = DEFAULT_MAX_ROWS;

	private final IndexPlugin indexPlugin;
	private Boolean sortDesc;
	private String sortFieldName;

	/**
	 * Constructor.
	 * @param indexPlugin Index plugin
	 */
	public IndexFilterFunction(final IndexPlugin indexPlugin) {
		Assertion.check().isNotNull(indexPlugin, "An IndexPlugin is required to use this method");
		//-----
		this.indexPlugin = indexPlugin;
	}

	/**
	 * Set filter by keywords.
	 * @param userKeywords user keywords
	 * @param maxRows Max rows
	 * @param keywordsSearchedFields searched fields
	 */
	public void filter(final String userKeywords, final int maxRows, final Collection<DtField> keywordsSearchedFields) {
		Assertion.check()
				.isNull(keywords, "Keywords was already set on this processor : {0}. Only one is supported.", keywords)
				.isNotNull(userKeywords)
				.isNotNull(keywordsSearchedFields);
		//-----
		keywords = userKeywords;
		top = maxRows;
		searchedFields = keywordsSearchedFields;
	}

	/**
	 * Set sort directives.
	 * Some directives can't be realized.
	 * @param fieldName Sort field name
	 * @param desc if sort desc
	 */
	public void sort(final String fieldName, final boolean desc) {
		Assertion.check().isNull(sortFieldName, "sortFieldName was already set on this processor : {0}. Only one is supported.", sortFieldName);
		//-----
		sortFieldName = fieldName;
		sortDesc = desc;
	}

	/**
	 * Add a listfilter (check ListFilter syntax).
	 * @param listFilter ListFilter
	 */
	public void filter(final ListFilter listFilter) {
		listFilters.add(listFilter);
	}

	/**
	 * Set sublist filter.
	 * @param start first index
	 * @param end last index
	 */
	public void filterSubList(final int start, final int end) {
		Assertion.check()
				.isTrue(start >= 0, "IndexOutOfBoundException, le start du subList doit être positif (start:{0}, end:{1})", String.valueOf(start), String.valueOf(end))
				.isTrue(start < end, "IndexOutOfBoundException, le start du subList doit être inférieur au end (start:{0}, end:{1})", String.valueOf(start), String.valueOf(end));
		//-----
		skip = start;
		top = end - start;
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.check().isNotNull(dtc);
		//-----
		final DtListState dtListState = DtListState.of(top, skip, sortFieldName, sortDesc);
		return indexPlugin.getCollection(keywords, searchedFields, listFilters, dtListState, Optional.empty(), dtc);
	}
}
