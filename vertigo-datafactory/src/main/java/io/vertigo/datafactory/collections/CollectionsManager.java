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
package io.vertigo.datafactory.collections;

import java.util.Optional;
import java.util.function.Predicate;

import io.vertigo.core.node.component.Manager;
import io.vertigo.datafactory.collections.definitions.FacetDefinition;
import io.vertigo.datafactory.collections.model.FacetedQuery;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;

/**
 * Some tools on collections/lists to allow
 *  - sort
 *  - filter
 *  - facets.
 * @author  pchretien
 */
public interface CollectionsManager extends Manager {
	/**
	 * Constructeur de la function de filtrage à partir d'un filtre de liste.
	 *
	 * @param listFilter Filtre de liste
	 * @return Function de filtrage
	 */
	<D extends DtObject> Predicate<D> filter(final ListFilter listFilter);

	/**
	 * Filter or sort a list via a listProcessor powered by an index engine, can be composed of filters or sorters.
	 * @return DtListIndexProcessor
	 * @param <D> Type de l'objet de la liste
	 */
	<D extends DtObject> IndexDtListFunctionBuilder<D> createIndexDtListFunctionBuilder();

	/**
	 * Facettage d'une liste selon une requete.
	 * Le facettage s'effectue en deux temps :
	 *  - Filtrage de la liste
	 *  - Facettage proprement dit
	 * @param dtList Liste à facetter
	 * @param facetedQuery Requete à appliquer (filtrage)
	 * @param clusterFacetDefinition Facet de groupement à appliquer (clustering)
	 * @return Résultat correspondant à la requête
	 * @param <R> Type de l'objet de la liste
	 */
	<R extends DtObject> FacetedQueryResult<R, DtList<R>> facetList(final DtList<R> dtList, final FacetedQuery facetedQuery, final Optional<FacetDefinition> clusterFacetDefinition);
}
