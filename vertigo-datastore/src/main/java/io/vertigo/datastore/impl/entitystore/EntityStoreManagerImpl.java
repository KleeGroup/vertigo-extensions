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

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.datamodel.criteria.Criteria;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.DtListURI;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datamodel.structure.util.VCollectors;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datastore.cache.CacheManager;
import io.vertigo.datastore.cache.definitions.CacheDefinition;
import io.vertigo.datastore.entitystore.BrokerNN;
import io.vertigo.datastore.entitystore.EntityStoreConfig;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.entitystore.MasterDataConfig;
import io.vertigo.datastore.entitystore.StoreEvent;
import io.vertigo.datastore.entitystore.definitions.MasterDataDefinition;
import io.vertigo.datastore.impl.entitystore.cache.CacheData;
import io.vertigo.datastore.impl.entitystore.cache.CacheDataStore;
import io.vertigo.datastore.impl.entitystore.logical.LogicalEntityStoreConfig;

/**
 * Implementation of DataStore.
 * @author pchretien
 */
public final class EntityStoreManagerImpl implements EntityStoreManager, Activeable, SimpleDefinitionProvider {
	private static final Criteria CRITERIA_ALWAYS_TRUE = Criterions.alwaysTrue();

	/** Le store est le point d'accès unique à la base (sql, xml, fichier plat...). */
	private final CacheDataStore cacheDataStore;
	private final EventBusManager eventBusManager;
	private final VTransactionManager transactionManager;

	private final BrokerNNImpl brokerNN;

	private final LogicalEntityStoreConfig logicalStoreConfig;
	private final EntityStoreConfigImpl dataStoreConfig;
	private final MasterDataConfigImpl masterDataConfig;

	/**
	 * Constructor
	 * @param entityStoreManager storeManager
	 * @param transactionManager transactionManager
	 * @param eventBusManager eventBusManager
	 * @param dataStoreConfig dataStoreConfig
	 */
	@Inject
	public EntityStoreManagerImpl(
			final CacheManager cacheManager,
			final VTransactionManager transactionManager,
			final EventBusManager eventBusManager,
			final TaskManager taskManager,
			final List<EntityStorePlugin> entityStorePlugins) {
		Assertion.check()
				.isNotNull(cacheManager)
				.isNotNull(transactionManager)
				.isNotNull(eventBusManager)
				.isNotNull(taskManager);
		//-----
		dataStoreConfig = new EntityStoreConfigImpl(entityStorePlugins, cacheManager);
		logicalStoreConfig = dataStoreConfig.getLogicalStoreConfig();
		masterDataConfig = new MasterDataConfigImpl();
		cacheDataStore = new CacheDataStore(masterDataConfig, dataStoreConfig);
		this.eventBusManager = eventBusManager;
		this.transactionManager = transactionManager;
		brokerNN = new BrokerNNImpl(taskManager);
	}

	@Override
	public void start() {
		// register as cacheable the dtDefinitions that are persistant and have a corresponding CacheDefinition
		Node.getNode().getDefinitionSpace().getAll(DtDefinition.class).stream()
				.filter(DtDefinition::isPersistent)
				.filter(dtDefinition -> Node.getNode().getDefinitionSpace().contains(CacheData.getContext(dtDefinition)))
				.forEach(dtDefinition -> dataStoreConfig.getCacheStoreConfig().registerCacheable(dtDefinition, Node.getNode().getDefinitionSpace().resolve(CacheData.getContext(dtDefinition), CacheDefinition.class).isReloadedByList()));

		Node.getNode().getDefinitionSpace().getAll(MasterDataDefinition.class)
				.forEach(masterDataConfig::register);

	}

	@Override
	public void stop() {
		// nothing

	}

	private EntityStorePlugin getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalDataStore(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E readOneForUpdate(final UID<E> uri) {
		Assertion.check().isNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		final E entity = getPhysicalStore(dtDefinition).readNullableForUpdate(dtDefinition, uri);
		//-----
		Assertion.check().isNotNull(entity, "no entity found for : '{0}'", uri);
		//-----
		fireAfterCommit(StoreEvent.Type.UPDATE, uri);
		return entity;
	}

	private void fireAfterCommit(final StoreEvent.Type evenType, final UID<?> uri) {
		transactionManager.getCurrentTransaction().addAfterCompletion(
				(final boolean txCommitted) -> {
					if (txCommitted) {//send event only is tx successful
						eventBusManager.post(new StoreEvent(evenType, uri));
					}
				});
	}

	//--- Transactionnal Event

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E create(final E entity) {
		Assertion.check().isNotNull(entity);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		final E createdEntity = getPhysicalStore(dtDefinition).create(dtDefinition, entity);
		//-----
		fireAfterCommit(StoreEvent.Type.CREATE, UID.of(dtDefinition, DtObjectUtil.getId(createdEntity)));
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		return createdEntity;
	}

	/** {@inheritDoc} */
	@Override
	public void update(final Entity entity) {
		Assertion.check().isNotNull(entity);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		getPhysicalStore(dtDefinition).update(dtDefinition, entity);
		//-----
		fireAfterCommit(StoreEvent.Type.UPDATE, UID.of(dtDefinition, DtObjectUtil.getId(entity)));
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final UID<? extends Entity> uri) {
		Assertion.check().isNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		getPhysicalStore(dtDefinition).delete(dtDefinition, uri);
		//-----
		fireAfterCommit(StoreEvent.Type.DELETE, uri);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E readOne(final UID<E> uri) {
		Assertion.check().isNotNull(uri);
		//-----
		final E entity = cacheDataStore.readNullable(uri);
		//-----
		Assertion.check().isNotNull(entity, "no entity found for : '{0}'", uri);
		return entity;
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtListURI uri) {
		Assertion.check().isNotNull(uri);
		//-----
		final DtList<E> list = cacheDataStore.findAll(uri);
		//-----
		Assertion.check().isNotNull(list);
		return list;
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return getPhysicalStore(dtDefinition).count(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> find(final DtDefinition dtDefinition, final Criteria<E> criteria, final DtListState dtListState) {
		Assertion.check()
				.isNotNull(dtDefinition)
				.isNotNull(dtListState);
		//-----
		final DtList<E> list = cacheDataStore.findByCriteria(dtDefinition, criteria != null ? criteria : CRITERIA_ALWAYS_TRUE, dtListState);
		//-----
		Assertion.check().isNotNull(list);
		return list;

	}

	/** {@inheritDoc} */
	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return cacheDataStore.provideDefinitions(definitionSpace);
	}

	//------

	/** {@inheritDoc} */
	@Override
	public BrokerNN getBrokerNN() {
		return brokerNN;
	}

	/** {@inheritDoc} */
	@Override
	public MasterDataConfig getMasterDataConfig() {
		return masterDataConfig;
	}

	/**
	 * @return Configuration du StoreManager
	 */
	@Override
	public EntityStoreConfig getDataStoreConfig() {
		return dataStoreConfig;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> sort(final DtList<D> list, final String fieldName, final boolean desc) {
		Assertion.check()
				.isNotNull(list)
				.isNotBlank(fieldName);
		//-----
		final Comparator<D> comparator = new DtObjectComparator<>(this, list.getDefinition().getField(fieldName), desc);
		return list.stream()
				.sorted(comparator)
				.collect(VCollectors.toDtList(list.getDefinition()));
	}

}
