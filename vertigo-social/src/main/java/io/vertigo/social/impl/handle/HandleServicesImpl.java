package io.vertigo.social.impl.handle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.eventbus.EventBusSubscribed;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.component.Activeable;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DataAccessor;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.store.StoreEvent;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.social.services.handle.Handle;
import io.vertigo.social.services.handle.HandleServices;
import io.vertigo.util.StringUtil;

public final class HandleServicesImpl implements HandleServices, Activeable {

	private static final int CHUNK_SIZE = 1000;

	private final StoreManager storeManager;
	private final VTransactionManager transactionManager;

	private List<DtDefinition> dtDefinitionsWithHandle;
	private final HandlePlugin handlePlugin;

	@Inject
	public HandleServicesImpl(
			final StoreManager storeManager,
			final VTransactionManager transactionManager,
			final HandlePlugin handlePlugin) {
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(handlePlugin);
		//---
		this.storeManager = storeManager;
		this.transactionManager = transactionManager;
		this.handlePlugin = handlePlugin;

	}

	@Override
	public void start() {
		dtDefinitionsWithHandle = Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
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
						entity = storeManager.getDataStore().readOne(uid);
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
		Assertion.checkNotNull(handlePrefix);
		//---
		if (!StringUtil.isEmpty(handlePrefix) && isStartingByDtDefinition(handlePrefix).isPresent()) {
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

	private final Optional<DtDefinition> isStartingByDtDefinition(final String handlePrefix) {
		return dtDefinitionsWithHandle
				.stream()
				.filter(dtDefinition -> handlePrefix.startsWith(StringUtil.first2LowerCase(dtDefinition.getLocalName()) + "/"))
				.findAny();
	}

	private static final Handle toHandle(final DtDefinition dtDefinition, final Entity entity) {
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
				entities = storeManager.getDataStore().find(dtDefinition, criteria, dtListState);
			}
			lastResultsSize = entities.size();
			lastId = (Serializable) idFieldAccessor.getValue(entities.get(entities.size() - 1));

			handlePlugin.add(entities.stream()
					.map(entity -> toHandle(dtDefinition, entity))
					.collect(Collectors.toList()));
		} while (lastResultsSize == CHUNK_SIZE);
	}

}
