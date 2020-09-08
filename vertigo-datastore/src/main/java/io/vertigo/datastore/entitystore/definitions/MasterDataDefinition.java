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
package io.vertigo.datastore.entitystore.definitions;

import java.util.function.Predicate;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.datamodel.structure.model.DtListURIForMasterData;

@DefinitionPrefix(MasterDataDefinition.PREFIX)
public final class MasterDataDefinition extends AbstractDefinition {
	public static final String PREFIX = "Md";

	private final DtListURIForMasterData uri;
	private final Predicate predicate;

	public MasterDataDefinition(
			final String name,
			final DtListURIForMasterData uri,
			final Predicate predicate) {
		super(name);
		//---
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(uri)
				.isNotNull(predicate);
		//---
		this.uri = uri;
		this.predicate = predicate;
	}

	public DtListURIForMasterData getUri() {
		return uri;
	}

	public Predicate getPredicate() {
		return predicate;
	}
}
