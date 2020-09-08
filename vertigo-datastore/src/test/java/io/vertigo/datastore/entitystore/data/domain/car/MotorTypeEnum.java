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
package io.vertigo.datastore.entitystore.data.domain.car;

import java.io.Serializable;

import io.vertigo.datamodel.structure.model.MasterDataEnum;
import io.vertigo.datamodel.structure.model.UID;

public enum MotorTypeEnum implements MasterDataEnum<MotorType> {

	essence("ESSENCE"), //
	diesel("DIESEL");

	private final UID<MotorType> entityUri;

	private MotorTypeEnum(final Serializable id) {
		entityUri = UID.of(MotorType.class, id);
	}

	@Override
	public UID<MotorType> getEntityUID() {
		return entityUri;
	}

}
