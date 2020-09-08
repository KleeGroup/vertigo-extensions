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
package io.vertigo.datafactory;

import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.datafactory.collections.CollectionsManager;
import io.vertigo.datafactory.impl.collections.CollectionsManagerImpl;
import io.vertigo.datafactory.impl.search.SearchManagerImpl;
import io.vertigo.datafactory.plugins.collections.lucene.LuceneIndexPlugin;
import io.vertigo.datafactory.search.SearchManager;

/**
 * Defines dynamo features.
 *
 * @author pchretien
 */
public final class DataFactoryFeatures extends Features<DataFactoryFeatures> {

	/**
	 * Constructor.
	 */
	public DataFactoryFeatures() {
		super("vertigo-datafactory");
	}

	@Feature("search")
	public DataFactoryFeatures withSearch() {
		getModuleConfigBuilder()
				.addComponent(SearchManager.class, SearchManagerImpl.class);
		return this;
	}

	@Feature("search.elasticsearch.client")
	public DataFactoryFeatures withESClient(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(io.vertigo.datafactory.plugins.search.elasticsearch.client.ClientESSearchServicesPlugin.class, params);
		return this;
	}

	@Feature("search.elasticsearch.restHL")
	public DataFactoryFeatures withESHL(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(io.vertigo.datafactory.plugins.search.elasticsearch.rest.RestHLClientESSearchServicesPlugin.class, params);
		return this;
	}

	@Feature("collections.luceneIndex")
	public DataFactoryFeatures withLuceneIndex() {
		getModuleConfigBuilder()
				.addPlugin(LuceneIndexPlugin.class);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(CollectionsManager.class, CollectionsManagerImpl.class);

	}
}
