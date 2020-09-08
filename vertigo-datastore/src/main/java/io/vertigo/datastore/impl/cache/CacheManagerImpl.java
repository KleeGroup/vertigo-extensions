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
package io.vertigo.datastore.impl.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.datastore.cache.CacheManager;
import io.vertigo.datastore.cache.definitions.CacheDefinition;

/**
 * Manager de gestion du cache.
 *
 * @author pchretien
 */
public final class CacheManagerImpl implements CacheManager, SimpleDefinitionProvider {
	private final CachePlugin cachePlugin;

	/**
	 * Constructor.
	 * @param cachePlugin Plugin de gestion du cache
	 */
	@Inject
	public CacheManagerImpl(final CachePlugin cachePlugin) {
		Assertion.check().isNotNull(cachePlugin);
		//-----
		this.cachePlugin = cachePlugin;
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new CacheDefinition(
				"CacheHealthVertigo",
				false,
				10,
				60,
				60,
				true));
	}

	//===========================================================================
	//==================Gestion du rendu et des interactions=====================
	//===========================================================================
	/** {@inheritDoc} */
	@Override
	public void put(final String context, final Serializable key, final Object value) {
		cachePlugin.put(context, key, value);
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		return cachePlugin.get(context, key);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final String context, final Serializable key) {
		return cachePlugin.remove(context, key);
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		cachePlugin.clear(context);
	}

	/** {@inheritDoc} */
	@Override
	public void clearAll() {
		cachePlugin.clearAll();
	}

}
