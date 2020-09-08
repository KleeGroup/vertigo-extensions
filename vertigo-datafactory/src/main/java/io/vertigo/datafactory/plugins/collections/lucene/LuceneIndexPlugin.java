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
package io.vertigo.datafactory.plugins.collections.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.eventbus.EventBusSubscribed;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.impl.collections.IndexPlugin;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.DtListURI;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datastore.cache.CacheManager;
import io.vertigo.datastore.cache.definitions.CacheDefinition;
import io.vertigo.datastore.entitystore.StoreEvent;

/**
 * Plugin de d'indexation de DtList utilisant Lucene en Ram.
 *
 * @author npiedeloup
 */
public final class LuceneIndexPlugin implements IndexPlugin, SimpleDefinitionProvider {

	private final CacheManager cacheManager;
	private final SmartTypeManager smartTypeManager;

	private static final String CACHE_LUCENE_INDEX = "CacheLuceneIndex";

	/**
	 * Constructor.
	 * @param localeManager Manager des messages localisés
	 * @param cacheManager Manager des caches
	 * @param eventBusManager Event manager
	 */
	@Inject
	public LuceneIndexPlugin(
			final LocaleManager localeManager,
			final CacheManager cacheManager,
			final EventBusManager eventBusManager,
			final SmartTypeManager smartTypeManager) {
		Assertion.check()
				.isNotNull(localeManager)
				.isNotNull(cacheManager)
				.isNotNull(smartTypeManager);
		//-----
		this.cacheManager = cacheManager;
		this.smartTypeManager = smartTypeManager;
		localeManager.add(Resources.class.getName(), Resources.values());
	}

	/**
	 * Subscription to store events
	 * @param event the incomming event
	 */
	@EventBusSubscribed
	public void onStoreEvent(final StoreEvent event) {
		cacheManager.remove(CACHE_LUCENE_INDEX, getIndexCacheContext(event.getUID().getDefinition()));
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new CacheDefinition(CACHE_LUCENE_INDEX, false, 1000, 30 * 60, 60 * 60, true));
	}

	private <D extends DtObject> RamLuceneIndex<D> indexList(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		//TODO : gestion du cache a revoir... et le lien avec le CacheStore.
		//L'index devrait être interrogé par le Broker ? on pourrait alors mettre en cache dans le DataCache.
		final DtListURI dtcUri = fullDtc.getURI();
		final boolean useCache = dtcUri != null; //no cache if no URI
		RamLuceneIndex<D> index;
		if (useCache) {
			final String indexName = "INDEX_" + dtcUri.urn();
			final String cacheContext = getIndexCacheContext(fullDtc.getDefinition());
			//TODO non threadSafe.
			Map<String, RamLuceneIndex> luceneIndexMap = Map.class.cast(cacheManager.get(CACHE_LUCENE_INDEX, cacheContext));
			if (luceneIndexMap == null) {
				luceneIndexMap = new HashMap<>();
			}
			if (!luceneIndexMap.containsKey(indexName)) {
				index = createIndex(fullDtc, storeValue);
				luceneIndexMap.put(indexName, index);
				cacheManager.put(CACHE_LUCENE_INDEX, cacheContext, luceneIndexMap);
				return index;
			}
			return luceneIndexMap.get(indexName);
		}
		return createIndex(fullDtc, storeValue);
	}

	private static String getIndexCacheContext(final DtDefinition dtDefinition) {
		return "IndexCache:" + dtDefinition.getName();
	}

	private <D extends DtObject> RamLuceneIndex<D> createIndex(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		Assertion.check().isNotNull(fullDtc);
		//-----
		final RamLuceneIndex<D> luceneDb = new RamLuceneIndex<>(fullDtc.getDefinition(), smartTypeManager);
		luceneDb.addAll(fullDtc, storeValue);
		return luceneDb;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> getCollection(
			final String keywords,
			final Collection<DtField> searchedFields,
			final List<ListFilter> listFilters,
			final DtListState listState,
			final Optional<DtField> boostedField,
			final DtList<D> dtc) {
		Assertion.check().isTrue(listState.getMaxRows().isPresent(), "Can't return all results, you must define maxRows");
		try {
			final RamLuceneIndex<D> index = indexList(dtc, false);
			return index.getCollection(keywords, searchedFields, listFilters, listState, boostedField);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Erreur d'indexation");
		}
	}
}
