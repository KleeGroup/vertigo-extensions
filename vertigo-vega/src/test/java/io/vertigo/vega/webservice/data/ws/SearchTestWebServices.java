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
package io.vertigo.vega.webservice.data.ws;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.node.Node;
import io.vertigo.datafactory.collections.CollectionsManager;
import io.vertigo.datafactory.collections.definitions.FacetDefinition;
import io.vertigo.datafactory.collections.definitions.FacetedQueryDefinition;
import io.vertigo.datafactory.collections.model.FacetValue;
import io.vertigo.datafactory.collections.model.FacetedQuery;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.vega.engines.webservice.json.UiContext;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.stereotype.ExcludedFields;
import io.vertigo.vega.webservice.stereotype.IncludedFields;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

@PathPrefix("/search")
public final class SearchTestWebServices implements WebServices {

	@Inject
	private ContactDao contactDao;

	@Inject
	private CollectionsManager collectionsManager;

	@POST("/selectedFacetValues")
	public UiContext testSelectedFacetValues(final SelectedFacetValues selectedFacetValues) {
		final FacetedQueryDefinition facetedQueryDefinition = Node.getNode().getDefinitionSpace().resolve("QryContactFacet", FacetedQueryDefinition.class);
		final UiContext uiContext = new UiContext();
		for (final FacetDefinition facetDefinition : facetedQueryDefinition.getFacetDefinitions()) {
			if (!selectedFacetValues.getFacetValues(facetDefinition.getName()).isEmpty()) {
				uiContext.put(facetDefinition.getName(),
						selectedFacetValues.getFacetValues(facetDefinition.getName())
								.stream()
								.map(FacetValue::getCode)
								.collect(Collectors.joining(",")));
			}
		}
		return uiContext;
	}

	@POST("/facetedResult")
	@ExcludedFields({ "highlight" })
	@IncludedFields({ "list.name", "list.conId", "list.firstName" })
	public FacetedQueryResult<Contact, DtList<Contact>> testFacetedQueryResult(final SelectedFacetValues selectedFacetValues) {
		final DtList<Contact> allContacts = asDtList(contactDao.getList(), Contact.class);
		final FacetedQueryDefinition facetedQueryDefinition = Node.getNode().getDefinitionSpace().resolve("QryContactFacet", FacetedQueryDefinition.class);
		final FacetedQuery facetedQuery = new FacetedQuery(facetedQueryDefinition, selectedFacetValues);
		return collectionsManager.facetList(allContacts, facetedQuery, Optional.empty());
	}

	@POST("/facetedClusteredResult")
	@ExcludedFields({ "highlight" })
	@IncludedFields({ "list.name", "list.conId", "list.firstName" })
	public FacetedQueryResult<Contact, DtList<Contact>> testFacetedClusterQueryResult(final SelectedFacetValues selectedFacetValues) {
		final DtList<Contact> allContacts = asDtList(contactDao.getList(), Contact.class);
		final FacetedQueryDefinition facetedQueryDefinition = Node.getNode().getDefinitionSpace().resolve("QryContactFacet", FacetedQueryDefinition.class);
		final FacetedQuery facetedQuery = new FacetedQuery(facetedQueryDefinition, selectedFacetValues);
		return collectionsManager.facetList(allContacts, facetedQuery, Optional.of(obtainFacetDefinition("FctHonorificCode")));
	}

	private static FacetDefinition obtainFacetDefinition(final String facetName) {
		return Node.getNode().getDefinitionSpace().resolve(facetName, FacetDefinition.class);
	}

	private static <D extends DtObject> DtList<D> asDtList(final Collection<D> values, final Class<D> dtObjectClass) {
		final DtList<D> result = new DtList<>(dtObjectClass);
		for (final D element : values) {
			result.add(element);
		}
		return result;
	}

}
