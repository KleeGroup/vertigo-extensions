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
package io.vertigo.datafactory.collections.model;

import java.io.Serializable;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.DefinitionReference;
import io.vertigo.datafactory.collections.definitions.FacetedQueryDefinition;

/**
 * Requete de filtrage par facettes.
 * @author npiedeloup
 */
public final class FacetedQuery implements Serializable {
	private static final long serialVersionUID = -3215786603726103410L;

	private final DefinitionReference<FacetedQueryDefinition> facetedQueryDefinitionRef;
	private final SelectedFacetValues selectedFacetValues;

	/**
	 * Constructor.
	 * @param facetedQueryDefinition Definition de la requête
	 * @param selectedFacetValue Liste des valeurs de facette selectionnées par facette
	 */
	public FacetedQuery(final FacetedQueryDefinition facetedQueryDefinition, final SelectedFacetValues selectedFacetValue) {
		Assertion.check()
				.isNotNull(facetedQueryDefinition)
				.isNotNull(selectedFacetValue);
		//-----
		facetedQueryDefinitionRef = new DefinitionReference<>(facetedQueryDefinition);
		selectedFacetValues = selectedFacetValue;
	}

	/**
	 * @return Définition du FacetedQuery.
	 */
	public FacetedQueryDefinition getDefinition() {
		return facetedQueryDefinitionRef.get();
	}

	/**
	 * Liste des supplémentaires.
	 * @return Liste des filtres.
	 */
	public SelectedFacetValues getSelectedFacetValues() {
		return selectedFacetValues;
	}
}
