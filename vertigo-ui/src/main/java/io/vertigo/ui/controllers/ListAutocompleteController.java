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
package io.vertigo.ui.controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.datafactory.collections.CollectionsManager;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.ui.core.UiListUnmodifiable;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.impl.springmvc.controller.AbstractVSpringMvcController;
import io.vertigo.vega.webservice.model.UiList;

/**
 * Service web de l'autocomplete des listes.
 * @author npiedeloup
 */
@Controller
@RequestMapping("/autocomplete")
public final class ListAutocompleteController extends AbstractVSpringMvcController {

	@Inject
	private CollectionsManager collectionsManager;
	@Inject
	private VTransactionManager transactionManager; //used for search in linked masterdatalist

	@PostMapping("/_searchFullText")
	@ResponseBody
	public List searchFullText(
			final ViewContext viewContext,
			@RequestParam("terms") final String terms,
			@RequestParam("list") final String list,
			@RequestParam("valueField") final String valueField,
			@RequestParam("labelField") final String labelField) {

		final DtList dtList = obtainDtList(viewContext, list);
		final DtDefinition dtDefinition = dtList.getDefinition();
		final DtField labelDtField = dtDefinition.getField(labelField);
		final Collection<DtField> searchedFields = Collections.singletonList(labelDtField);
		//-----
		final UnaryOperator<DtList<DtObject>> fullTextFilter = collectionsManager.createIndexDtListFunctionBuilder()
				.filter(terms != null ? terms : "", 20, searchedFields)
				.build();
		return loadList(fullTextFilter, dtList, valueField, labelField);
	}

	@PostMapping("/_searchByValue")
	@ResponseBody
	public List searchByValue(
			final ViewContext viewContext,
			@RequestParam("value") final String value,
			@RequestParam("list") final String list,
			@RequestParam("valueField") final String valueField,
			@RequestParam("labelField") final String labelField) {
		final DtList dtList = obtainDtList(viewContext, list);
		final UnaryOperator<DtList<DtObject>> byValueFilter = collectionsManager.createIndexDtListFunctionBuilder()
				.filterByValue(valueField, value)
				.build();
		return loadList(byValueFilter, dtList, valueField, labelField);
	}

	private DtList obtainDtList(final ViewContext viewContext, final String list) {
		final UiList contextList = viewContext.getUiList(() -> list);
		return contextList.mergeAndCheckInput(Collections.EMPTY_LIST, getUiMessageStack());
	}

	private List loadList(final UnaryOperator<DtList<DtObject>> listFilter, final DtList dtList, final String valueField,
			final String labelField) {
		//-----
		final DtList results;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) { //Open a transaction because all fields are indexed. If there is a MDL it was load too.
			results = listFilter.apply(dtList);
		}

		final HashSet<String> fieldsForClient = new HashSet<>();
		fieldsForClient.add(valueField);
		fieldsForClient.add(labelField);
		return new UiListUnmodifiable(results, Optional.empty()).listForClient(fieldsForClient, Collections.emptyMap());
	}

}
