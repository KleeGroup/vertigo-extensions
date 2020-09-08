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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <O> the type of entity
 */
public final class UiListUnmodifiable<O extends DtObject> extends AbstractUiListUnmodifiable<O> {
	private static final long serialVersionUID = 5475819598230056558L;

	private final DtList<O> dtList;

	/**
	 * Constructeur.
	 * @param dtList Liste à encapsuler
	 */
	public UiListUnmodifiable(final DtList<O> dtList, final Optional<DtFieldName<O>> keyFieldNameOpt) {
		super(dtList.getDefinition(), keyFieldNameOpt);
		//-----
		this.dtList = dtList;
		if (dtList.size() < 1000) {
			initUiObjectByIdIndex();
		}
	}

	// ==========================================================================

	/** {@inheritDoc} */
	@Override
	protected DtList<O> obtainDtList() {
		return dtList;
	}

	/**
	 * Vérifie les UiObjects de la liste, met à jour les objets métiers et retourne la liste.
	 * @param validators Validateur à utilisé, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return Liste métier validée.
	 */
	@Override
	public DtList<O> mergeAndCheckInput(final List<DtObjectValidator<O>> validators, final UiMessageStack uiMessageStack) {
		return dtList;
	}

	/**
	 * Vérifie les UiObjects de la liste et remplis la pile d'erreur.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 */
	@Override
	public boolean checkFormat(final UiMessageStack uiMessageStack) {
		// nothing for unmodifiable (we don't use the data in the buffers)
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return dtList
				.stream()
				.limit(50) //we consider only the first 50 elements
				.map(DtObject::toString)
				.collect(Collectors.joining("; ",
						"uiList(" + dtList.size() + " element(s) :", ")"));
	}

}
