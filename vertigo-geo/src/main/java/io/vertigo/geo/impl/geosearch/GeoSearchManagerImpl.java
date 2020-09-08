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
package io.vertigo.geo.impl.geosearch;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.geo.geocoder.GeoLocation;
import io.vertigo.geo.geosearch.GeoSearchManager;

public class GeoSearchManagerImpl implements GeoSearchManager {

	private static final int DEFAULT_MAX_ROWS = 1000;
	private static final int MAX_MAX_ROWS = 5000;

	private final GeoSearchPlugin geoSearchPlugin;

	@Inject
	public GeoSearchManagerImpl(
			final GeoSearchPlugin geoSearchPlugin) {
		Assertion.check().isNotNull(geoSearchPlugin);
		//---
		this.geoSearchPlugin = geoSearchPlugin;
	}

	@Override
	public <D extends DtObject> DtList<D> searchInBoundingBox(
			final GeoLocation topLeft,
			final GeoLocation bottomRight,
			final String indexName,
			final Class<D> dtIndexClass,
			final DtFieldName<D> fieldName,
			final Optional<Integer> maxRowsOpt) {
		Assertion.check()
				.when(maxRowsOpt.isPresent(), () -> Assertion.check()
						.isTrue(maxRowsOpt.get() < DEFAULT_MAX_ROWS, "Max rows must be lower than ", MAX_MAX_ROWS));
		//---
		return geoSearchPlugin.searchInBoundingBox(topLeft, bottomRight, indexName, dtIndexClass, fieldName, maxRowsOpt.orElse(DEFAULT_MAX_ROWS));
	}

}
