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
package io.vertigo.datafactory.search.model;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * Objet d'échange avec l'index.
 * Cet objet permet de
 *  - construire l'index de recherche
 *  - consulter le résultat d'une recherhe
 *
 *  Le DtObject d'index utilise
 *  - la propriété 'persistent' des fields pour savoir si le champs fait partit du résultat ou non
 *  - le smartType et sa propriété indexType pour savoir si le champs est indéxé ou non
 *
 * @author dchallas
 * @param <K> keyConcept type
 * @param <I> Index type
 */
public final class SearchIndex<K extends KeyConcept, I extends DtObject> {
	/** Définition de l'index. */
	private final SearchIndexDefinition indexDefinition;

	/** UID de l'objet indexé : par convention il s'agit de l'uri de K.*/
	private final UID<K> uid;

	/** DtObject d'index. */
	private final I indexDtObject;

	/**
	 * Constructor.
	 * @param indexDefinition definition de O, I
	 * @param uid UID de l'objet indexé
	 */
	private SearchIndex(final SearchIndexDefinition indexDefinition, final UID<K> uid, final I indexDtObject) {
		Assertion.check()
				.isNotNull(uid)
				.isNotNull(indexDefinition)
				.isNotNull(indexDtObject)
				//On vérifie la consistance des données.
				.isTrue(indexDefinition.getKeyConceptDtDefinition().equals(uid.getDefinition()),
						"Le type de l'URI de l'objet indexé  ({0}) ne correspond pas au KeyConcept de l'index ({1})", uid.toString(), indexDefinition.getKeyConceptDtDefinition().getName())
				.isTrue(indexDefinition.getIndexDtDefinition().equals(DtObjectUtil.findDtDefinition(indexDtObject)),
						"Le type l'objet indexé ({1}) ne correspond pas à celui de l'index ({1})", DtObjectUtil.findDtDefinition(indexDtObject).getName(), indexDefinition.getIndexDtDefinition().getName());
		//-----
		this.uid = uid;
		this.indexDefinition = indexDefinition;
		this.indexDtObject = indexDtObject;
	}

	/**
	 * @return Définition de l'index.
	 */
	public SearchIndexDefinition getDefinition() {
		return indexDefinition;
	}

	/**
	 * Récupération de l'uri de la ressource indexée.
	 *  - Utilisé pour la récupération de highlight.
	 * @return UID de la ressource indexée.
	 */
	public UID<K> getUID() {
		return uid;
	}

	/**
	 * Récupération de l'object contenant les champs à indexer.
	 * @return Objet contenant les champs à indexer
	 */
	public I getIndexDtObject() {
		Assertion.check().isTrue(hasIndex(), "Index n'est pas dans l'état indexable.");
		//-----
		return indexDtObject;
	}

	/**
	 * @return Contient l'objet d'index
	 */
	private boolean hasIndex() {
		return indexDtObject != null;
	}

	/**
	 * Constructeur de l'Objet permettant de créer l'index.
	 * @param <I> Type de l'objet représentant l'index
	 * @param uid UID de l'objet indexé
	 * @param indexDefinition Définition de l'index de recherche.
	 * @param indexDto  DTO représentant l'index
	 * @return  Objet permettant de créer l'index
	 */
	public static <S extends KeyConcept, I extends DtObject> SearchIndex<S, I> createIndex(final SearchIndexDefinition indexDefinition, final UID<S> uid, final I indexDto) {
		return new SearchIndex<>(indexDefinition, uid, indexDto);
	}

}
