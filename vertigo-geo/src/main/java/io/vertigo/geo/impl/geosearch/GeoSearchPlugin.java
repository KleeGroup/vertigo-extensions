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

import io.vertigo.core.node.component.Plugin;
import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.geo.geocoder.GeoLocation;

public interface GeoSearchPlugin extends Plugin {

	<D extends DtObject> DtList<D> searchInBoundingBox(
			final GeoLocation topLeft,
			final GeoLocation bottomRight,
			final String indexName,
			final Class<D> dtIndexClass,
			final DtFieldName<D> fieldName,
			final Integer maxRows);
}
