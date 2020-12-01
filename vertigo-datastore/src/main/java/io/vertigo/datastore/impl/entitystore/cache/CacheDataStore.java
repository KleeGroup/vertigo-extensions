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
package io.vertigo.datastore.impl.entitystore.cache;

import java.util.Collections;
import java.util.List;

import io.vertigo.commons.eventbus.definitions.EventBusSubscriptionDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.datamodel.criteria.Criteria;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForNNAssociation;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForSimpleAssociation;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.DtListURI;
import io.vertigo.datamodel.structure.model.DtListURIForMasterData;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datamodel.structure.util.VCollectors;
import io.vertigo.datastore.entitystore.MasterDataConfig;
import io.vertigo.datastore.entitystore.StoreEvent;
import io.vertigo.datastore.impl.entitystore.EntityStoreConfigImpl;
import io.vertigo.datastore.impl.entitystore.EntityStorePlugin;
import io.vertigo.datastore.impl.entitystore.logical.LogicalEntityStoreConfig;

/**
 * Gestion des données mises en cache.
 *
 * @author  pchretien
 */
public final class CacheDataStore implements SimpleDefinitionProvider {
	private final MasterDataConfig masterDataConfig;
	private final CacheDataStoreConfig cacheDataStoreConfig;
	private final LogicalEntityStoreConfig logicalStoreConfig;

	/**
	 * Constructor.
	 * @param storeManager Store manager
	 * @param dataStoreConfig Data store configuration
	 */
	public CacheDataStore(
			final MasterDataConfig masterDataConfig,
			final EntityStoreConfigImpl dataStoreConfig) {
		Assertion.check()
				.isNotNull(masterDataConfig)
				.isNotNull(dataStoreConfig);
		//-----
		this.masterDataConfig = masterDataConfig;
		cacheDataStoreConfig = dataStoreConfig.getCacheStoreConfig();
		logicalStoreConfig = dataStoreConfig.getLogicalStoreConfig();
	}

	private EntityStorePlugin getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalDataStore(dtDefinition);
	}

	/**
	 * @param <E> the type of entity
	 * @param uid Element uid
	 * @return Element by uid
	 */
	public <E extends Entity> E readNullable(final UID<E> uid) {
		Assertion.check().isNotNull(uid);
		//-----
		final DtDefinition dtDefinition = uid.getDefinition();
		E entity;
		if (cacheDataStoreConfig.isCacheable(dtDefinition)) {
			// - Prise en compte du cache
			entity = cacheDataStoreConfig.getDataCache().getDtObject(uid);
			// - Prise en compte du cache
			if (entity == null) {
				//Cas ou le dto représente un objet non mis en cache
				entity = this.<E> loadNullable(dtDefinition, uid);
			}
		} else {
			entity = getPhysicalStore(dtDefinition).readNullable(dtDefinition, uid);
		}
		return entity;
	}

	private <E extends Entity> E loadNullable(final DtDefinition dtDefinition, final UID<E> uid) {
		final E entity;
		synchronized (CacheData.getContextLock(dtDefinition)) {
			if (cacheDataStoreConfig.isReloadedByList(dtDefinition)) {
				//On ne charge pas les cache de façon atomique.
				final DtListURI dtcURIAll = new DtListURIForCriteria<>(dtDefinition, Criterions.alwaysTrue(), DtListState.of(null));
				loadList(dtcURIAll); //on charge la liste complete (et on remplit les caches)
				entity = cacheDataStoreConfig.getDataCache().getDtObject(uid);
			} else {
				//On charge le cache de façon atomique à partir du dataStore
				entity = getPhysicalStore(dtDefinition).readNullable(dtDefinition, uid);
				if (entity != null) {
					cacheDataStoreConfig.getDataCache().putDtObject(entity);
				}
			}
		}
		return entity;
	}

	private <E extends Entity> DtList<E> doLoadList(final DtDefinition dtDefinition, final DtListURI listUri) {
		Assertion.check().isNotNull(listUri);
		//-----
		final DtList<E> list;
		if (listUri instanceof DtListURIForMasterData) {
			list = loadMDList((DtListURIForMasterData) listUri);
		} else if (listUri instanceof DtListURIForSimpleAssociation) {
			list = getPhysicalStore(dtDefinition).findAll(dtDefinition, (DtListURIForSimpleAssociation) listUri);
		} else if (listUri instanceof DtListURIForNNAssociation) {
			list = getPhysicalStore(dtDefinition).findAll(dtDefinition, (DtListURIForNNAssociation) listUri);
		} else if (listUri instanceof DtListURIForCriteria<?>) {
			final DtListURIForCriteria<E> castedListUri = DtListURIForCriteria.class.cast(listUri);
			list = getPhysicalStore(dtDefinition).findByCriteria(dtDefinition, castedListUri.getCriteria(), castedListUri.getDtListState());
		} else {
			throw new IllegalArgumentException("cas non traité " + listUri);
		}
		return new DtList(list, listUri);
	}

	private <E extends Entity> DtList<E> loadMDList(final DtListURIForMasterData uri) {
		Assertion.check()
				.isNotNull(uri)
				.isTrue(uri.getDtDefinition().getSortField().isPresent(), "Sortfield on definition {0} wasn't set. It's mandatory for MasterDataList.", uri.getDtDefinition().getName());
		//-----
		//On cherche la liste complete
		final DtList<E> unFilteredDtc = getPhysicalStore(uri.getDtDefinition()).findByCriteria(uri.getDtDefinition(), Criterions.alwaysTrue(), DtListState.of(null, 0, uri.getDtDefinition().getSortField().get().getName(), false));

		return unFilteredDtc
				.stream()
				//1.on filtre
				.filter(masterDataConfig.getFilter(uri))
				//2.on trie
				.sorted((dt1, dt2) -> DtObjectUtil.compareFieldValues(dt1, dt2, uri.getDtDefinition().getSortField().get(), false))
				.collect(VCollectors.toDtList(unFilteredDtc.getDefinition()));
	}

	/**
	 * @param <E> the type of entity
	 * @param uri List uri
	 * @return List of this uri
	 */
	public <E extends Entity> DtList<E> findAll(final DtListURI uri) {
		Assertion.check().isNotNull(uri);
		//-----
		//- Prise en compte du cache
		//On ne met pas en cache les URI d'une association NN
		if (cacheDataStoreConfig.isCacheable(uri.getDtDefinition()) && !isMultipleAssociation(uri)) {
			DtList<E> list = cacheDataStoreConfig.getDataCache().getDtList(uri);
			if (list == null) {
				list = this.<E> loadList(uri);
			}
			return list;
		}
		//Si la liste n'est pas dans le cache alors on lit depuis le store.
		return doLoadList(uri.getDtDefinition(), uri);
	}

	public <E extends Entity> DtList<E> findByCriteria(final DtDefinition dtDefinition, final Criteria<E> criteria, final DtListState dtListState) {
		return findAll(new DtListURIForCriteria(dtDefinition, criteria, dtListState));
	}

	private static boolean isMultipleAssociation(final DtListURI uri) {
		return uri instanceof DtListURIForNNAssociation;
	}

	private <E extends Entity> DtList<E> loadList(final DtListURI uri) {
		synchronized (CacheData.getContextLock(uri.getDtDefinition())) {
			// On charge la liste initiale avec les critéres définis en amont
			final DtList<E> list = doLoadList(uri.getDtDefinition(), uri);
			// Mise en cache de la liste et des éléments.
			cacheDataStoreConfig.getDataCache().putDtList(list);
			return list;
		}
	}

	/* On notifie la mise à jour du cache, celui-ci est donc vidé. */
	private void clearCache(final DtDefinition dtDefinition) {
		Assertion.check().isNotNull(dtDefinition);
		//-----
		// On ne vérifie pas que la definition est cachable, Lucene utilise le même cache
		// A changer si on gère lucene différemment
		cacheDataStoreConfig.getDataCache().clear(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		final EventBusSubscriptionDefinition<StoreEvent> eventBusSubscription = new EventBusSubscriptionDefinition<>(
				"EvtClearCache",
				StoreEvent.class,
				event -> clearCache(event.getUID().getDefinition()));
		return Collections.singletonList(eventBusSubscription);
	}
}
