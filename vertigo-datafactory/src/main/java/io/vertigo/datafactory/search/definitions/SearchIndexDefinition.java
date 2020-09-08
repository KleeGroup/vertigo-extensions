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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.DtStereotype;

/**
 * Définition de l'index de recherche.
 *
 * Fondalementalement un index est constitué de deux types d'objets.
 * - Un objet d'index (les champs indexés)
 * - Un keyConcept représentant le concept métier réprésenté par cet index.
 * La définition d'index précise également un SearchLoader permettant la mise à jour autonome de l'index.
 *
 * L'objet d'index est à la fois porteur des champs de recherche, et ceux utilisé à l'affichage.
 * La différence entre les deux peut-être affiné par :
 * - la propriété 'persistent' des fields pour savoir si le champs fait partit ou non du résultat utilisé pour l'affichage
 * - le smartType et sa propriété indexType pour savoir si le champs est indéxé ou non
 *
 * L'objet d'affichage peut être simple (Ex: résultat google) alors qu'il se réfère à un index plus riche.
 *
 * @author dchallas, npiedeloup
 */
@DefinitionPrefix(SearchIndexDefinition.PREFIX)
public final class SearchIndexDefinition extends AbstractDefinition {
	public static final String PREFIX = "Idx";

	/** Structure des éléments indexés. */
	private final DtDefinition indexDtDefinition;

	private final DtDefinition keyConceptDtDefinition;

	private final Map<DtField, List<DtField>> indexCopyFromFieldsMap; //(map toField : [fromField, fromField, ...])
	private final Map<DtField, List<DtField>> indexCopyToFieldsMap; //(map fromField : [toField, toField, ...])

	//private final Set<DtField> indexCopyFromFields;

	private final String searchLoaderId;

	/**
	 * Constructor.
	 * @param name Index name
	 * @param keyConceptDtDefinition KeyConcept associé à l'index
	 * @param indexDtDefinition Structure des éléments indexés.
	 * @param indexCopyFromFieldsMap CopyField map : (map toField : [fromField, fromField, ...])
	 * @param searchLoaderId Loader de chargement des éléments indéxés et résultat
	 */
	public SearchIndexDefinition(
			final String name,
			final DtDefinition keyConceptDtDefinition,
			final DtDefinition indexDtDefinition,
			final Map<DtField, List<DtField>> indexCopyFromFieldsMap,
			final String searchLoaderId) {
		super(name);
		//---
		Assertion.check()
				.isNotNull(keyConceptDtDefinition)
				.isTrue(
						keyConceptDtDefinition.getStereotype() == DtStereotype.KeyConcept,
						"keyConceptDtDefinition ({0}) must be a DtDefinition of a KeyConcept class", keyConceptDtDefinition.getName())
				.isNotNull(indexDtDefinition)
				.isNotNull(indexCopyFromFieldsMap)
				.isNotBlank(searchLoaderId);
		//-----
		this.keyConceptDtDefinition = keyConceptDtDefinition;
		this.indexDtDefinition = indexDtDefinition;
		this.indexCopyFromFieldsMap = indexCopyFromFieldsMap;
		this.searchLoaderId = searchLoaderId;

		indexCopyToFieldsMap = new HashMap<>();
		for (final Entry<DtField, List<DtField>> entry : indexCopyFromFieldsMap.entrySet()) {
			final List<DtField> fromFields = entry.getValue();
			for (final DtField fromField : fromFields) {
				indexCopyToFieldsMap.computeIfAbsent(fromField, k -> new ArrayList<>()).add(entry.getKey());
			}
		}
	}

	/**
	 * Définition de l'objet représentant le contenu de l'index (indexé et résultat).
	 * @return Définition des champs indexés.
	 */
	public DtDefinition getIndexDtDefinition() {
		return indexDtDefinition;
	}

	/**
	 * Définition du keyConcept maitre de cet index.
	 * Le keyConcept de l'index est surveillé pour rafraichir l'index.
	 * @return Définition du keyConcept.
	 */
	public DtDefinition getKeyConceptDtDefinition() {
		return keyConceptDtDefinition;
	}

	/**
	 * @param fromField Field to copy to others
	 * @return list des copyToFields.
	 */
	public List<DtField> getIndexCopyToFields(final DtField fromField) {
		final List<DtField> copyToFields = indexCopyToFieldsMap.get(fromField);
		Assertion.check().isNotNull(copyToFields);
		//-----
		return Collections.unmodifiableList(copyToFields);
	}

	/**
	 * @return copyFields from.
	 */
	public Set<DtField> getIndexCopyFromFields() {
		return Collections.unmodifiableSet(indexCopyToFieldsMap.keySet());
	}

	/**
	 * @return copyFields to.
	 */
	public Set<DtField> getIndexCopyToFields() {
		return Collections.unmodifiableSet(indexCopyFromFieldsMap.keySet());
	}

	/**
	 * Nom du composant de chargement des éléments à indexer.
	 * @return Nom du composant de chargement des éléments à indexer.
	 */
	public String getSearchLoaderId() {
		return searchLoaderId;
	}
}
