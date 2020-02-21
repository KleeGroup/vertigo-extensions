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
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
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
		final UiList contextList = viewContext.getUiList(() -> list);
		final DtList dtList = contextList.mergeAndCheckInput(Collections.EMPTY_LIST, getUiMessageStack());
		final DtDefinition dtDefinition = dtList.getDefinition();
		//-----
		final DtField labelDtField = dtDefinition.getField(labelField);

		final Collection<DtField> searchedFields = Collections.singletonList(labelDtField);
		final DtList results;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) { //Open a transaction because all fields are indexed. If there is a MDL it was load too.
			final UnaryOperator<DtList<DtObject>> fullTextFilter = collectionsManager.createIndexDtListFunctionBuilder()
					.filter(terms != null ? terms : "", 20, searchedFields)
					.build();
			results = fullTextFilter.apply(dtList);
		}

		final HashSet<String> fieldsForClient = new HashSet<>();
		fieldsForClient.add(valueField);
		fieldsForClient.add(labelField);
		return new UiListUnmodifiable(results, Optional.empty()).listForClient(fieldsForClient, Collections.emptyMap());
	}

}
