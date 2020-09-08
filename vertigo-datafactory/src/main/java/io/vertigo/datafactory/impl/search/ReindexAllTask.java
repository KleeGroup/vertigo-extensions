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
package io.vertigo.datafactory.impl.search;

import java.io.Serializable;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.Node;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.search.SearchManager;
import io.vertigo.datafactory.search.definitions.SearchChunk;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.definitions.SearchLoader;
import io.vertigo.datafactory.search.model.SearchIndex;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.KeyConcept;

/**
 * Reindex all data taks.
 * @author npiedeloup (27 juil. 2015 14:35:14)
 * @param <S> KeyConcept type
 */
final class ReindexAllTask<S extends KeyConcept> implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger(ReindexAllTask.class);
	private static volatile boolean REINDEXATION_IN_PROGRESS;
	private static volatile long REINDEX_COUNT;
	private final WritableFuture<Long> reindexFuture;
	private final SearchIndexDefinition searchIndexDefinition;
	private final SearchManager searchManager;

	/**
	 * Constructor.
	 * @param searchIndexDefinition Search index definition
	 * @param reindexFuture Future for result
	 * @param searchManager Search manager
	 */
	ReindexAllTask(final SearchIndexDefinition searchIndexDefinition, final WritableFuture<Long> reindexFuture, final SearchManager searchManager) {
		Assertion.check()
				.isNotNull(searchIndexDefinition)
				.isNotNull(reindexFuture)
				.isNotNull(searchManager);
		//-----
		this.searchIndexDefinition = searchIndexDefinition;
		this.reindexFuture = reindexFuture;
		this.searchManager = searchManager;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		if (isReindexInProgress()) {
			final String warnMessage = "Reindexation of " + searchIndexDefinition.getName() + " is already in progess (" + getReindexCount() + " elements done)";
			LOGGER.warn(warnMessage);
			reindexFuture.fail(new VSystemException(warnMessage));
		} else {
			//-----
			startReindex();
			long reindexCount = 0;
			final long startTime = System.currentTimeMillis();
			try {
				final Class<S> keyConceptClass = (Class<S>) ClassUtil.classForName(searchIndexDefinition.getKeyConceptDtDefinition().getClassCanonicalName(), KeyConcept.class);
				final SearchLoader<S, DtObject> searchLoader = Node.getNode().getComponentSpace().resolve(searchIndexDefinition.getSearchLoaderId(), SearchLoader.class);
				Serializable lastUID = null;
				LOGGER.info("Reindexation of {} started", searchIndexDefinition.getName());

				for (final SearchChunk<S> searchChunk : searchLoader.chunk(keyConceptClass)) {
					final Collection<SearchIndex<S, DtObject>> searchIndexes = searchLoader.loadData(searchChunk);

					final Serializable maxUID = searchChunk.getLastUID().getId();
					Assertion.check().isFalse(maxUID.equals(lastUID), "SearchLoader ({0}) error : return the same uid list", searchIndexDefinition.getSearchLoaderId());
					searchManager.removeAll(searchIndexDefinition, urisRangeToListFilter(lastUID, maxUID));
					if (!searchIndexes.isEmpty()) {
						searchManager.putAll(searchIndexDefinition, searchIndexes);
					}
					lastUID = maxUID;
					reindexCount += searchChunk.getAllUIDs().size();
					updateReindexCount(reindexCount);
				}
				//On vide la suite, pour le cas ou les dernières données ne sont plus là
				searchManager.removeAll(searchIndexDefinition, urisRangeToListFilter(lastUID, null));
				//On ne retire pas la fin, il y a un risque de retirer les données ajoutées depuis le démarrage de l'indexation
				reindexFuture.success(reindexCount);
			} catch (final Exception e) {
				LOGGER.error("Reindexation error", e);
				reindexFuture.fail(e);
			} finally {
				stopReindex();
				LOGGER.info("Reindexation of {} finished in {} ms ({} elements done)", searchIndexDefinition.getName(), System.currentTimeMillis() - startTime, reindexCount);
			}
		}
	}

	private static boolean isReindexInProgress() {
		return REINDEXATION_IN_PROGRESS;
	}

	private static void startReindex() {
		REINDEXATION_IN_PROGRESS = true;
	}

	private static void stopReindex() {
		REINDEXATION_IN_PROGRESS = false;
	}

	private static void updateReindexCount(final long reindexCount) {
		REINDEX_COUNT = reindexCount;
	}

	private static long getReindexCount() {
		return REINDEX_COUNT;
	}

	private static ListFilter urisRangeToListFilter(final Serializable firstUri, final Serializable lastUri) {
		final String filterValue = new StringBuilder()
				.append("docId").append(":{") //{ for exclude min
				.append(firstUri != null ? escapeStringId(firstUri) : "*")
				.append(" TO ")
				.append(lastUri != null ? escapeStringId(lastUri) : "*")
				.append("]")
				.toString();
		return ListFilter.of(filterValue);
	}

	private static Serializable escapeStringId(final Serializable id) {
		if (id instanceof String) {
			return "\"" + id + "\"";
		}
		return id;
	}
}
