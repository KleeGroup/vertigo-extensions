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
package io.vertigo.social.impl.handle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.commons.eventbus.EventBusSubscribed;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.criteria.Criteria;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.structure.definitions.DataAccessor;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.entitystore.StoreEvent;
import io.vertigo.social.handle.Handle;
import io.vertigo.social.handle.HandleManager;

public final class HandleManagerImpl implements HandleManager, Activeable {

	private static final int CHUNK_SIZE = 1000;

	private final EntityStoreManager entityStoreManager;
	private final VTransactionManager transactionManager;

	private List<DtDefinition> dtDefinitionsWithHandle;
	private final HandlePlugin handlePlugin;

	@Inject
	public HandleManagerImpl(
			final EntityStoreManager entityStoreManager,
			final VTransactionManager transactionManager,
			final HandlePlugin handlePlugin) {
		Assertion.check()
				.isNotNull(entityStoreManager)
				.isNotNull(transactionManager)
				.isNotNull(handlePlugin);
		//---
		this.entityStoreManager = entityStoreManager;
		this.transactionManager = transactionManager;
		this.handlePlugin = handlePlugin;

	}

	@Override
	public void start() {
		dtDefinitionsWithHandle = Node.getNode().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.filter(dtDefinition -> dtDefinition.getHandleField().isPresent())
				.collect(Collectors.toList());

	}

	@Override
	public void stop() {
		// nothing

	}

	/**
	 * Receive Store event.
	 * @param storeEvent Store event
	 */
	@EventBusSubscribed
	public void onEvent(final StoreEvent storeEvent) {
		final UID uid = storeEvent.getUID();
		//On ne traite l'event que si il porte sur un KeyConcept
		if (uid.getDefinition().getHandleField().isPresent()) {

			switch (storeEvent.getType()) {
				case UPDATE:
				case CREATE:

					final Entity entity;
					try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
						//we need to make better than this...
						entity = entityStoreManager.readOne(uid);
					}
					// add the handle in the plugin
					handlePlugin.add(Collections.singletonList(toHandle(uid.getDefinition(), entity)));
					break;
				case DELETE:
					handlePlugin.remove(Collections.singletonList(uid));
					break;
				default:
					throw new VSystemException("Type of store Event {0} is not supported", storeEvent.getType());
			}

		}
	}

	@Override
	public List<String> getHandlePrefixes() {
		return dtDefinitionsWithHandle.stream()
				.map(DtDefinition::getLocalName)
				.map(StringUtil::first2LowerCase)
				.collect(Collectors.toList());
	}

	@Override
	public List<Handle> searchHandles(final String handlePrefix) {
		Assertion.check().isNotNull(handlePrefix);
		//---
		if (!StringUtil.isBlank(handlePrefix) && isStartingByDtDefinition(handlePrefix).isPresent()) {
			// search with the plugin
			return handlePlugin.search(handlePrefix);
		}
		return Collections.emptyList();
	}

	@Override
	public Handle getHandleByUid(final UID uid) {
		return handlePlugin.getByUid(uid);
	}

	@Override
	public Handle getHandleByCode(final String code) {
		return handlePlugin.getByCode(code);
	}

	private Optional<DtDefinition> isStartingByDtDefinition(final String handlePrefix) {
		return dtDefinitionsWithHandle
				.stream()
				.filter(dtDefinition -> handlePrefix.startsWith(StringUtil.first2LowerCase(dtDefinition.getLocalName()) + "/"))
				.findAny();
	}

	private static Handle toHandle(final DtDefinition dtDefinition, final Entity entity) {
		final DataAccessor dataAccessor = dtDefinition.getHandleField().get().getDataAccessor();
		return new Handle(entity.getUID(),
				StringUtil.first2LowerCase(dtDefinition.getLocalName()) + "/" +
						dataAccessor.getValue(entity));
	}

	@Override
	public void reindexAll() {
		handlePlugin.removeAll();
		dtDefinitionsWithHandle.forEach(this::indexDefinition);
	}

	private void indexDefinition(final DtDefinition dtDefinition) {
		final String idFieldName = dtDefinition.getIdField().get().getName();
		final DataAccessor idFieldAccessor = dtDefinition.getIdField().get().getDataAccessor();
		int lastResultsSize;
		Serializable lastId = null;
		//we do it by chunks

		do {
			final List<Entity> entities;
			try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				final Criteria criteria = lastId != null ? Criterions.isGreaterThan(() -> idFieldName, lastId) : Criterions.alwaysTrue();
				final DtListState dtListState = DtListState.of(CHUNK_SIZE, 0, idFieldName, false);
				entities = entityStoreManager.find(dtDefinition, criteria, dtListState);
			}
			lastResultsSize = entities.size();
			lastId = (Serializable) idFieldAccessor.getValue(entities.get(entities.size() - 1));

			handlePlugin.add(entities.stream()
					.map(entity -> toHandle(dtDefinition, entity))
					.collect(Collectors.toList()));
		} while (lastResultsSize == CHUNK_SIZE);
	}

}
