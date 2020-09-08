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
package io.vertigo.datamodel.impl.smarttype.dynamic;

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.DefinitionSupplier;

/**
 *
 * @author  mlaroche
 */
public final class DynamicDefinition {
	private final String name;
	private final DefinitionSupplier definitionSupplier;
	private final List<String> definitionLinkNames;

	public DynamicDefinition(
			final String name,
			final List<String> definitionLinkNames,
			final DefinitionSupplier definitionSupplier) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(definitionLinkNames)
				.isNotNull(definitionSupplier);
		//---
		this.name = name;
		this.definitionLinkNames = definitionLinkNames;
		this.definitionSupplier = definitionSupplier;
	}

	public String getName() {
		return name;
	}

	public List<String> getDefinitionLinkNames() {
		return definitionLinkNames;
	}

	public DefinitionSupplier getDefinitionSupplier() {
		return definitionSupplier;
	}

}
