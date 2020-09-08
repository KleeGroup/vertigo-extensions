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
package io.vertigo.datafactory.search.withstore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.basics.task.TaskEngineSelect;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.datafactory.impl.search.loader.AbstractSqlSearchLoader;
import io.vertigo.datafactory.search.SearchManager;
import io.vertigo.datafactory.search.data.domain.Item;
import io.vertigo.datafactory.search.definitions.SearchChunk;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.model.SearchIndex;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.model.Task;

/**
 * SearchLoader of Item keyconcept, load uses StoreManager.
 * @author npiedeloup
 */
public final class ItemSearchLoader extends AbstractSqlSearchLoader<Long, Item, Item> {
	private final SearchManager searchManager;
	private final DefinitionSpace definitionSpace;

	/**
	 * Constructor.
	 * @param taskManager Task manager
	 * @param searchManager Search manager
	 */
	@Inject
	public ItemSearchLoader(final TaskManager taskManager, final SearchManager searchManager, final VTransactionManager transactionManager) {
		super(taskManager, transactionManager);
		Assertion.check().isNotNull(searchManager);
		//---
		this.searchManager = searchManager;
		definitionSpace = Node.getNode().getDefinitionSpace();
	}

	/** {@inheritDoc} */
	@Override
	public List<SearchIndex<Item, Item>> loadData(final SearchChunk<Item> searchChunk) {
		final SearchIndexDefinition indexDefinition = searchManager.findFirstIndexDefinitionByKeyConcept(Item.class);
		try (final VTransactionWritable tx = getTransactionManager().createCurrentTransaction()) {
			final List<SearchIndex<Item, Item>> result = new ArrayList<>();
			for (final Item item : loadItems(searchChunk)) {
				final UID<Item> uid = item.getUID();
				result.add(SearchIndex.createIndex(indexDefinition, uid, item));
			}
			return result;
		}
	}

	private DtList<Item> loadItems(final SearchChunk<Item> searchChunk) {
		final TaskDefinition taskDefinition = getTaskDefinition(searchChunk);

		final Task task = Task.builder(taskDefinition)
				.build();

		return getTaskManager()
				.execute(task)
				.getResult();
	}

	private TaskDefinition getTaskDefinition(final SearchChunk<Item> searchChunk) {
		final SmartTypeDefinition smartTypeItem = definitionSpace.resolve("STyDtItem", SmartTypeDefinition.class);
		final String sql = searchChunk.getAllUIDs()
				.stream()
				.map(uri -> uri.getId().toString())
				.collect(Collectors.joining(", ", "select * from ITEM where ID in (", ")"));

		return TaskDefinition.builder("TkLoadAllItems")
				.withEngine(TaskEngineSelect.class)
				.withRequest(sql)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withOutAttribute("dtc", smartTypeItem, Cardinality.MANY)
				.build();
	}
}
