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
package io.vertigo.datastore.impl.entitystore;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.datamodel.criteria.Criteria;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForNNAssociation;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForSimpleAssociation;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;

/**
 * The DataStorePlugin class defines the logical way used to read and write data in a data store.
 * This plugin can be implemented in a sql or no sql way.
 *
 * this datatore is linked to a dataSpace.
 * Several dtDefinition can be included in a dataSpace.
 *
 * @author  pchretien
 */
public interface EntityStorePlugin extends Plugin {

	/**
	 * @return the dataSpace
	 */
	String getDataSpace();

	/**
	 * @return the name of the connection
	 */
	String getConnectionName();

	//==========================================================================
	//=============================== READ =====================================
	//==========================================================================

	/**
	 * Returns the number of elements contained in the definition.
	 * @param dtDefinition Définition de DT
	 * @return the number of elements
	 */
	int count(final DtDefinition dtDefinition);

	/**
	 * Récupération de l'objet correspondant à l'URI fournie.
	 *
	 * @param uid UID de l'objet à charger
	 * @param <E> the type of entity
	 * @param dtDefinition Definition
	 * @return D correspondant à l'URI fournie.
	 */
	<E extends Entity> E readNullable(DtDefinition dtDefinition, UID<E> uid);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uid UID de la collection à charger
	 * @param dtDefinition Definition
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <E> the type of entity
	 */
	<E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForNNAssociation uid);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uid UID de la collection à charger
	 * @param dtDefinition Definition
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <E> the type of entity
	 */
	<E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForSimpleAssociation uid);

	//==========================================================================
	//=============================== WRITE ====================================
	//==========================================================================
	/**
	 * Creates an object.
	 * No object with the same id must have been created previously.
	 *
	 * @param dtDefinition Definition
	 * @param entity Object to create
	 * @return the created entity
	 */
	<E extends Entity> E create(DtDefinition dtDefinition, E entity);

	/**
	 * Updates an object.
	 * This object must have an id.
	 * @param dtDefinition Definition
	 * @param entity Object to update
	 */
	void update(DtDefinition dtDefinition, Entity entity);

	/**
	 * Deletes an object identified by an uri.
	 * @param dtDefinition Definition
	 * @param uid UID
	 */
	void delete(DtDefinition dtDefinition, UID<?> uid);

	/**
	 * Loads for update.
	 *
	 * @param dtDefinition Object's definition
	 * @param uid Object's uid
	 * @param <E> the type of entity
	 * @return D Object value.
	 */
	<E extends Entity> E readNullableForUpdate(DtDefinition dtDefinition, UID<?> uid);

	/**
	 * Finds a lists of entities matching a criteria.
	 * @param dtDefinition the definition of entities to find
	 * @param criteria the criteria to match
	 * @param dtListState listState of rows to retrieve
	 * @return the list
	 */
	<E extends Entity> DtList<E> findByCriteria(final DtDefinition dtDefinition, final Criteria<E> criteria, final DtListState dtListState);

}
