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
package io.vertigo.datafactory.search.data.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datafactory.impl.search.loader.AbstractSearchLoader;
import io.vertigo.datafactory.search.SearchManager;
import io.vertigo.datafactory.search.definitions.SearchChunk;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.model.SearchIndex;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.model.UID;

public final class ItemSearchLoader extends AbstractSearchLoader<Long, Item, Item> {
	private static final int SEARCH_CHUNK_SIZE = 5;
	private final SearchManager searchManager;
	private ItemDataBase itemDataBase;

	@Inject
	public ItemSearchLoader(final SearchManager searchManager) {
		Assertion.check().isNotNull(searchManager);
		//---
		this.searchManager = searchManager;
	}

	/**
	 * @param boundedDataBase Database to bound with this loader (specific for tests)
	 */
	public void bindDataBase(final ItemDataBase boundedDataBase) {
		Assertion.check().isNotNull(boundedDataBase);
		//----
		itemDataBase = boundedDataBase;
	}

	/** {@inheritDoc} */
	@Override
	public List<SearchIndex<Item, Item>> loadData(final SearchChunk<Item> searchChunk) {
		Assertion.check().isNotNull(itemDataBase, "itemDataBase not bound");
		//-----
		final SearchIndexDefinition indexDefinition = searchManager.findFirstIndexDefinitionByKeyConcept(Item.class);
		final List<SearchIndex<Item, Item>> itemIndexes = new ArrayList<>();
		final Map<Long, Item> itemPerId = new HashMap<>();
		for (final Item item : itemDataBase.getAllItems()) {
			itemPerId.put(item.getId(), item);
		}
		for (final UID<Item> uid : searchChunk.getAllUIDs()) {
			final Item item = itemPerId.get(uid.getId());
			itemIndexes.add(SearchIndex.createIndex(indexDefinition, uid, item));
		}
		return itemIndexes;
	}

	/** {@inheritDoc} */
	@Override
	protected List<UID<Item>> loadNextURI(final Long lastId, final DtDefinition dtDefinition) {
		final SearchIndexDefinition indexDefinition = searchManager.findFirstIndexDefinitionByKeyConcept(Item.class);
		final List<UID<Item>> uris = new ArrayList<>(SEARCH_CHUNK_SIZE);
		//call loader service
		for (final Item item : itemDataBase.getAllItems()) {
			if (item.getId() > lastId) {
				uris.add(UID.of(indexDefinition.getKeyConceptDtDefinition(), item.getId()));
			}
			if (uris.size() >= SEARCH_CHUNK_SIZE) {
				break;
			}
		}
		return uris;
	}
}
