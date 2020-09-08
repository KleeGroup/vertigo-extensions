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
package io.vertigo.quarto.exporter.data.domain;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.stereotype.Field;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * Continent.
 */
public final class Continent implements Entity {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;

	@Override
	public UID getUID() {
		return UID.of(this);
	}

	@Field(smartType = "STyId", type = "ID", cardinality = Cardinality.ONE, label = "id of the Continent")
	public final Long getId() {
		return id;
	}

	public Continent setId(final Long id) {
		this.id = id;
		return this;
	}

	@Field(smartType = "STyString", label = "name")
	public final String getName() {
		return name;
	}

	public Continent setName(final String name) {
		this.name = name;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}

}
