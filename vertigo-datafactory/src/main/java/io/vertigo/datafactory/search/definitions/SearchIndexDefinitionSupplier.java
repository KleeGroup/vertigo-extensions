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
package io.vertigo.datafactory.search.definitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.DefinitionSupplier;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;

public final class SearchIndexDefinitionSupplier implements DefinitionSupplier {

	private final String myName;
	private String myKeyConceptDtDefinitionName;
	private String myIndexDtDefinitionName;
	private String myIndexLoaderId;
	private final Map<String, String[]> myIndexCopyTo = new HashMap<>();

	public SearchIndexDefinitionSupplier(final String name) {
		this.myName = name;
	}

	public SearchIndexDefinitionSupplier withKeyConcept(final String keyConceptDtDefinitionName) {
		this.myKeyConceptDtDefinitionName = keyConceptDtDefinitionName;
		return this;
	}

	public SearchIndexDefinitionSupplier withIndexDtDefinition(final String indexDtDefinitionName) {
		this.myIndexDtDefinitionName = indexDtDefinitionName;
		return this;
	}

	public SearchIndexDefinitionSupplier withCopyToFields(final String fromField, final String... toFields) {
		myIndexCopyTo.put(fromField, toFields);
		return this;
	}

	public SearchIndexDefinitionSupplier withLoaderId(final String indexLoaderId) {
		this.myIndexLoaderId = indexLoaderId;
		return this;
	}

	@Override
	public SearchIndexDefinition get(final DefinitionSpace definitionSpace) {
		final DtDefinition keyConceptDtDefinition = definitionSpace.resolve(myKeyConceptDtDefinitionName, DtDefinition.class);
		final DtDefinition indexDtDefinition = definitionSpace.resolve(myIndexDtDefinitionName, DtDefinition.class);

		final Map<DtField, List<DtField>> copyToFields = new HashMap<>(); //(map fromField : [toField, toField, ...])
		for (final Map.Entry<String, String[]> indexCopyTo : myIndexCopyTo.entrySet()) {
			final DtField dtFieldFrom = indexDtDefinition.getField(indexCopyTo.getKey());
			final List<DtField> dtFieldTos = Stream.of(indexCopyTo.getValue())
					.map(indexDtDefinition::getField)
					.collect(Collectors.toList());
			copyToFields.put(dtFieldFrom, dtFieldTos);
		}
		return new SearchIndexDefinition(
				myName,
				keyConceptDtDefinition,
				indexDtDefinition,
				copyToFields,
				myIndexLoaderId);
	}
}
