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
package io.vertigo.quarto.exporter.data;

import java.util.Arrays;
import java.util.Iterator;

import io.vertigo.datamodel.structure.definitions.DtFieldName;
import io.vertigo.quarto.exporter.data.domain.Continent;
import io.vertigo.quarto.exporter.data.domain.Country;

public final class DtDefinitions implements Iterable<Class<?>> {
	@Override
	public Iterator<Class<?>> iterator() {
		return Arrays.asList(new Class<?>[] {
				Country.class,
				Continent.class,
		}).iterator();
	}

	/**
	 * Enumération des champs de Famille.
	 */
	public enum CountryFields implements DtFieldName {
		id, conId, name, active, localDate, instant
	}

	public enum ContinentFields implements DtFieldName {
		id, name,
	}
}
