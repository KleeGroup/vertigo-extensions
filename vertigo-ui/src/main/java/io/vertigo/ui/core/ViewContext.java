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
package io.vertigo.ui.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datafactory.collections.definitions.FacetDefinition;
import io.vertigo.datafactory.collections.model.Facet;
import io.vertigo.datafactory.collections.model.FacetValue;
import io.vertigo.datafactory.collections.model.FacetedQuery;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListURIForMasterData;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.webservice.model.UiList;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.DefaultDtObjectValidator;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.ValidationUserException;

/**
 * Liste des couples (clé, object) enregistrés.
 * @author npiedeloup
 */
public final class ViewContext implements Serializable {

	private static final long serialVersionUID = -8237448155016161135L;

	/** Clée de l'id de context dans le context. */
	public static final ViewContextKey<String> CTX = ViewContextKey.of("CTX");

	private final Set<String> modifiedKeys = new HashSet<>();
	private final ViewContextMap viewContextMap;

	private final JsonEngine jsonEngine;

	public ViewContext(final ViewContextMap viewContextMap, final JsonEngine jsonEngine) {
		Assertion.check()
				.isNotNull(viewContextMap)
				.isNotNull(jsonEngine);
		//---
		this.viewContextMap = viewContextMap;
		this.jsonEngine = jsonEngine;
	}

	/* ================================== Life cycle =====================================*/

	/**
	 * @return Clé de ce context
	 */
	public String getId() {
		return getString(CTX);
	}

	public void setInputCtxId(final String ctxId) {
		viewContextMap.put(ViewContextMap.INPUT_CTX, ctxId);
	}

	public void setCtxId() {
		viewContextMap.put(CTX.get(), UUID.randomUUID().toString());
	}

	/**
	 * Génère un nouvel Id et passe le context en modifiable.
	 */
	public void makeModifiable() {
		viewContextMap.makeModifiable();
	}

	/**
	 * passe le context en non-modifiable.
	 */
	public void makeUnmodifiable() {
		viewContextMap.makeUnmodifiable();
		modifiedKeys.add(CTX.get());
	}

	/**
	 * Mark this context as Dirty : shouldn't be stored and keep old id.
	 */
	public void markDirty() {
		viewContextMap.markDirty();
	}

	/**
	 * @return if context dirty : shouldn't be stored and keep old id
	 */
	public boolean isDirty() {
		return viewContextMap.isDirty();
	}

	public ViewContextMap asMap() {
		return viewContextMap;
	}

	public ViewContextMap asUpdatesMap() {
		return viewContextMap.getFilteredViewContext(Optional.of(modifiedKeys));
	}

	public String getFilteredViewContextAsJson() {
		return jsonEngine.toJson(viewContextMap.getFilteredViewContext(Optional.empty()));
	}

	public void markModifiedKeys(final ViewContextKey... newModifiedKeys) {
		modifiedKeys.addAll(Arrays.stream(newModifiedKeys)
				.map(ViewContextKey::get)
				.collect(Collectors.toSet()));
	}

	public void markModifiedKeys(final String... newModifiedKeys) {
		modifiedKeys.addAll(Arrays.asList(newModifiedKeys));
	}

	/* ================================== Map =====================================*/

	public Serializable get(final Object key) {
		if (key instanceof ViewContextKey) {
			return viewContextMap.get(((ViewContextKey<?>) key).get());
		}
		return viewContextMap.get(key);
	}

	/** {@inheritDoc} */
	public boolean containsKey(final Object key) {
		if (key instanceof ViewContextKey) {
			return viewContextMap.containsKey(((ViewContextKey<?>) key).get());
		}
		return viewContextMap.containsKey(key);
	}

	/**
	 * @param uiObject UiObject recherché
	 * @return Clé de context de l'élément (null si non trouvé)
	 */
	public String findKey(final UiObject<?> uiObject) {
		return viewContextMap.findKey(uiObject);
	}

	/**
	 * @param dtObject DtObject recherché
	 * @return Clé de context de l'élément (null si non trouvé)
	 */
	public String findKey(final DtObject dtObject) {
		return viewContextMap.findKey(dtObject);
	}

	private Serializable put(final ViewContextKey<?> key, final Serializable value) {
		modifiedKeys.add(key.get());
		return viewContextMap.put(key.get(), value);
	}

	/* ================================== ContextRef =====================================*/

	/**
	 * @param key Clé de context
	 * @return UiObject du context
	 */
	public <O extends Serializable> void publishRef(final ViewContextKey<O> key, final O value) {
		put(key, value);
	}

	/**
	 * @param key Clé de context
	 * @return UiObject du context
	 */
	public <O extends DtObject> UiObject<O> getUiObject(final ViewContextKey<O> key) {
		return (UiObject<O>) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return UiList du context
	 */
	public <O extends DtObject> UiList<O> getUiList(final ViewContextKey<O> key) {
		return (UiList<O>) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return UiListModifiable du context
	 */
	public <O extends DtObject> BasicUiListModifiable<O> getUiListModifiable(final ViewContextKey<O> key) {
		return (BasicUiListModifiable<O>) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return String du context
	 */
	public String getString(final ViewContextKey<String> key) {
		final Object value = get(key);
		if (value instanceof String[] && ((String[]) value).length > 0) {
			//Struts set des String[] au lieu des String
			//on prend le premier
			return ((String[]) value)[0];
		}
		return (String) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return Long du context
	 */
	public Long getLong(final ViewContextKey<Long> key) {
		return (Long) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return Integer du context
	 */
	public Integer getInteger(final ViewContextKey<Integer> key) {
		return (Integer) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return Boolean du context
	 */
	public Boolean getBoolean(final ViewContextKey<Boolean> key) {
		return (Boolean) get(key);
	}

	/* ================================ ContextForm ==================================*/
	/**
	 * Ajoute un objet de type form au context.
	 * @param dto Objet à publier
	 */
	public <O extends DtObject> void publishDto(final ViewContextKey<O> contextKey, final O dto) {
		final UiObject<O> strutsUiObject = new MapUiObject<>(dto);
		strutsUiObject.setInputKey(contextKey.get());
		put(contextKey, strutsUiObject);
	}

	/**
	 * Vérifie les erreurs de l'objet. Celles-ci sont ajoutées à l'uiMessageStack si nécessaire.
	 */
	public <O extends DtObject> void checkDtoErrors(final ViewContextKey<O> contextKey, final UiMessageStack uiMessageStack) {
		getUiObject(contextKey).checkFormat(uiMessageStack);
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}
	}

	/**
	 * @return objet métier valid�. Lance une exception si erreur.
	 */
	public <O extends DtObject> O readDto(final ViewContextKey<O> contextKey, final UiMessageStack uiMessageStack) {
		return readDto(contextKey, new DefaultDtObjectValidator<>(), uiMessageStack);
	}

	/**
	 * @return objet métier validé. Lance une exception si erreur.
	 */
	public <O extends DtObject> O readDto(final ViewContextKey<O> contextKey, final DtObjectValidator<O> validator, final UiMessageStack uiMessageStack) {
		checkDtoErrors(contextKey, uiMessageStack);
		// ---
		final O validatedDto = getUiObject(contextKey).mergeAndCheckInput(Collections.singletonList(validator), uiMessageStack);
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}
		return validatedDto;
	}

	/* ================================ ContextList ==================================*/

	/**
	 * Ajoute une liste au context.
	 * @param dtList List à publier
	 */
	private <O extends DtObject> void publishDtList(final ViewContextKey<O> contextKey, final Optional<DtFieldName<O>> keyFieldNameOpt, final DtList<O> dtList, final boolean modifiable) {
		if (modifiable) {
			put(contextKey, new BasicUiListModifiable<>(dtList, contextKey.get()));
		} else {
			put(contextKey, new UiListUnmodifiable<>(dtList, keyFieldNameOpt));
		}
	}

	/**
	 * Ajoute une liste au context.
	 * @param dtList List à publier
	 */
	public <O extends DtObject> void publishDtList(final ViewContextKey<O> contextKey, final DtFieldName<O> keyFieldName, final DtList<O> dtList) {
		publishDtList(contextKey, Optional.of(keyFieldName), dtList, false);
	}

	/**
	 * Ajoute une liste au context.
	 * @param dtList List à publier
	 */
	public <O extends DtObject> void publishDtList(final ViewContextKey<O> contextKey, final DtList<O> dtList) {
		publishDtList(contextKey, Optional.empty(), dtList, false);
	}

	/**
	 * @return List des objets métiers validée. Lance une exception si erreur.
	 */
	public <O extends DtObject> DtList<O> readDtList(final ViewContextKey<O> contextKey, final DtObjectValidator<O> validator, final UiMessageStack uiMessageStack) {
		return ((UiListUnmodifiable<O>) getUiList(contextKey)).mergeAndCheckInput(Collections.singletonList(validator), uiMessageStack);
	}

	/**
	 * @return List des objets métiers validée. Lance une exception si erreur.
	 */
	public <O extends DtObject> DtList<O> readDtList(final ViewContextKey<O> contextKey, final UiMessageStack uiMessageStack) {
		return readDtList(contextKey, new DefaultDtObjectValidator<>(), uiMessageStack);
	}

	/* ================================ ContextListModifiable ==================================*/

	/**
	 * Ajoute une liste au context.
	 * @param dtList List à publier
	 */
	public <O extends DtObject> void publishDtListModifiable(final ViewContextKey<O> contextKey, final DtList<O> dtList) {
		publishDtList(contextKey, Optional.empty(), dtList, true);
	}

	/**
	 * Vérifie les erreurs de la liste. Celles-ci sont ajoutées à l'uiMessageStack si nécessaire.
	 */
	public <O extends DtObject> void checkDtListErrors(final ViewContextKey<O> contextKey, final UiMessageStack uiMessageStack) {
		getUiListModifiable(contextKey).checkFormat(uiMessageStack);
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}
	}

	/**
	 * @return List des objets métiers validée. Lance une exception si erreur.
	 */
	public <O extends DtObject> DtList<O> readDtListModifiable(final ViewContextKey<O> contextKey, final UiMessageStack uiMessageStack) {
		return readDtListModifiable(contextKey, new DefaultDtObjectValidator<>(), uiMessageStack);
	}

	/**
	 * @return List des objets métiers validée. Lance une exception si erreur.
	 */
	public <O extends DtObject> DtList<O> readDtListModifiable(final ViewContextKey<O> contextKey, final DtObjectValidator<O> validator, final UiMessageStack uiMessageStack) {
		checkDtListErrors(contextKey, uiMessageStack);
		// ---
		final DtList<O> validatedList = ((BasicUiListModifiable) getUiListModifiable(contextKey)).mergeAndCheckInput(Collections.singletonList(validator), uiMessageStack);
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}
		return validatedList;
	}

	/* ================================ ContextMdList ==================================*/

	/**
	 * Publie une liste de référence.
	 * @param contextKey Context key
	 * @param entityClass Class associée
	 * @param code Code
	 */
	public <E extends Entity> void publishMdl(final ViewContextKey<E> contextKey, final Class<E> entityClass, final String code) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entityClass);
		put(contextKey, new UiMdList<E>(new DtListURIForMasterData(dtDefinition, code)));
	}

	/* ================================ FacetedQueryResult ==================================*/

	/**
	 * Publie une FacetedQueryResult.
	 * @param contextKey Context key
	 * @param keyFieldName Id's fieldName
	 * @param facetedQueryResult Result
	 */
	public <O extends DtObject> void publishFacetedQueryResult(final ViewContextKey<FacetedQueryResult<O, SearchQuery>> contextKey,
			final DtFieldName<O> keyFieldName, final FacetedQueryResult<O, SearchQuery> facetedQueryResult, final ViewContextKey<?> criteriaContextKey) {
		publishDtList(() -> contextKey.get() + "_list", Optional.of(keyFieldName), facetedQueryResult.getDtList(), false);
		put(() -> contextKey.get() + "_facets", translateFacets(facetedQueryResult.getFacets()));

		final FacetedQuery facetedQuery = facetedQueryResult.getSource().getFacetedQuery().get();
		put(() -> contextKey.get() + "_selectedFacets", new UiSelectedFacetValues(
				facetedQuery.getSelectedFacetValues(),
				facetedQuery.getDefinition().getFacetDefinitions()
						.stream()
						.map(FacetDefinition::getName)
						.collect(Collectors.toList())));
		put(() -> contextKey.get() + "_totalcount", facetedQueryResult.getCount());
		put(() -> contextKey.get() + "_criteriaContextKey", criteriaContextKey.get());
	}

	private static ArrayList<Serializable> translateFacets(final List<Facet> facets) {
		final ArrayList<Serializable> facetsList = new ArrayList<>();
		for (final Facet facet : facets) {
			final ArrayList<HashMap<String, Serializable>> facetValues = new ArrayList<>();
			for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
				if (entry.getValue() > 0) {
					final HashMap<String, Serializable> facetValue = new HashMap<>();
					facetValue.put("code", entry.getKey().getCode());
					facetValue.put("count", entry.getValue());
					facetValue.put("label", entry.getKey().getLabel().getDisplay());
					facetValues.add(facetValue);
				}
			}
			final HashMap<String, Serializable> facetAsMap = new HashMap<>();
			facetAsMap.put("code", facet.getDefinition().getName());
			facetAsMap.put("multiple", facet.getDefinition().isMultiSelectable());
			facetAsMap.put("label", facet.getDefinition().getLabel().getDisplay());
			facetAsMap.put("values", facetValues);
			facetsList.add(facetAsMap);
		}
		return facetsList;
	}

	/**
	 * Returns selectedFacetValues of a given facetedQuery.
	 * @param contextKey Context key
	 * @return selectedFacetValues
	 */
	public <O extends DtObject> SelectedFacetValues getSelectedFacetValues(final ViewContextKey<FacetedQueryResult<O, FacetedQuery>> contextKey) {
		return ((UiSelectedFacetValues) get(contextKey.get() + "_selectedFacets")).toSelectedFacetValues();
	}

}
