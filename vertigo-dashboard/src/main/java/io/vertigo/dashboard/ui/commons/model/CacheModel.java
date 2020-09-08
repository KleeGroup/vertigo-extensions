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
package io.vertigo.dashboard.ui.commons.model;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.cache.definitions.CacheDefinition;

public class CacheModel {
	private final CacheDefinition cacheDefinition;

	public CacheModel(final CacheDefinition cacheDefinition) {
		Assertion.check().isNotNull(cacheDefinition);
		//---
		this.cacheDefinition = cacheDefinition;
	}

	public String getName() {
		return cacheDefinition.getName();
	}

	public int getTtlIdle() {
		return cacheDefinition.getTimeToIdleSeconds();
	}

	public int getTtl() {
		return cacheDefinition.getTimeToLiveSeconds();
	}

	public int getMaxElements() {
		return cacheDefinition.getMaxElementsInMemory();
	}

}
