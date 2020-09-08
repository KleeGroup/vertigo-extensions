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
package io.vertigo.datafactory.collections.data.domain;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.stereotype.Field;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

public final class SmartItem implements Entity {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Field(smartType = "STyId", type = "ID", cardinality = Cardinality.ONE, label = "id")
	private Long id;

	@Field(smartType = "STyText", label = "label")
	private String label;

	/** {@inheritDoc} */
	@Override
	public UID<SmartItem> getUID() {
		return UID.of(this);
	}

	public final Long getId() {
		return id;
	}

	public final void setId(final Long id) {
		this.id = id;
	}

	public final String getLabel() {
		return label;
	}

	public final void setLabel(final String label) {
		this.label = label;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
