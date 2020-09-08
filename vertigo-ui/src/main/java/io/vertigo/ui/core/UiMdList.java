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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListURIForMasterData;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <E> the type of entity
 */
final class UiMdList<E extends Entity> extends AbstractUiListUnmodifiable<E> {

	private static final long serialVersionUID = 4914912057762557563L;

	private final DtListURIForMasterData dtListURIForMasterData;
	private transient DtList<E> lazyDtList;

	/**
	 * Constructeur.
	 *
	 * @param dtListURIForMasterData Uri de la Liste à encapsuler
	 */
	public UiMdList(final DtListURIForMasterData dtListURIForMasterData) {
		super(dtListURIForMasterData.getDtDefinition(), Optional.empty());
		Assertion.check().isTrue(entityStoreManager.get().getMasterDataConfig().containsMasterData(dtListURIForMasterData.getDtDefinition()), "UiMdList can't be use with {0}, it's not a MasterDataList.",
				dtListURIForMasterData.getDtDefinition().getName());
		// -------------------------------------------------------------------------
		this.dtListURIForMasterData = dtListURIForMasterData;

	}

	// ==========================================================================

	/**
	 * @return Liste des données
	 */
	@Override
	public DtList<E> obtainDtList() {
		if (lazyDtList == null) {
			try (final VTransactionWritable transaction = transactionManager.get().createCurrentTransaction()) {
				lazyDtList = entityStoreManager.get().<E> findAll(dtListURIForMasterData);
			}
			if (lazyDtList.size() < 1000) {
				//load UiObjects
				initUiObjectByIdIndex();
			}
		}
		return lazyDtList;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "uiMdList(" + dtListURIForMasterData/*.toString()*/ + (lazyDtList != null ? ", loaded:" + lazyDtList.size() : "") + " )";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		//on surcharge equals pour éviter un appel à super.equals non désiré et qui forcerai le chargement de la liste
		if (o instanceof UiMdList) {
			return dtListURIForMasterData.equals(((UiMdList<?>) o).dtListURIForMasterData);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		//on surcharge hashCode pour eviter un appel à super.hashCode non désiré et qui forcerai le chargement de la liste
		return dtListURIForMasterData.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public DtList<E> mergeAndCheckInput(final List<DtObjectValidator<E>> validator, final UiMessageStack uiMessageStack) {
		return obtainDtList();
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkFormat(final UiMessageStack uiMessageStack) {
		return true;
	}

	/**
	 * Return a Serializable List for client.
	 * @param fieldsForClient List of fields
	 * @param valueTransformers Map of transformers
	 * @return ArrayList of HashMap (needed for Serializable)
	 */
	@Override
	public ArrayList<HashMap<String, Serializable>> listForClient(final Set<String> fieldsForClient, final Map<String, Function<Serializable, String>> valueTransformers) {
		obtainDtList(); // we need the list to be able to give it to the client

		final ArrayList<HashMap<String, Serializable>> listForClient = new ArrayList<>();
		for (final UiObject uiObject : getUiObjectBuffer()) {//if size if lower than 1000 we have prepared uiObjects
			listForClient.add(((MapUiObject) uiObject).mapForClient(fieldsForClient, valueTransformers));
		}
		return listForClient;
	}

}
