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
package io.vertigo.quarto.publisher;

import java.util.List;

import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.quarto.publisher.definitions.PublisherDataDefinition;
import io.vertigo.quarto.publisher.definitions.PublisherNodeDefinition;
import io.vertigo.quarto.publisher.definitions.PublisherNodeDefinitionBuilder;

public final class TestPublisherDefinitionProvider implements SimpleDefinitionProvider {

	private static PublisherDataDefinition createTestEnquete() {
		final PublisherNodeDefinition ville = new PublisherNodeDefinitionBuilder()
				.addStringField("nom")
				.addStringField("codePostal")
				.build();

		final PublisherNodeDefinition address = new PublisherNodeDefinitionBuilder()
				.addStringField("rue")
				.addNodeField("ville", ville)
				.build();

		final PublisherNodeDefinition enqueteur = new PublisherNodeDefinitionBuilder()
				.addStringField("nom")
				.addStringField("prenom")
				.addNodeField("adresseRatachement", address)
				.build();

		final PublisherNodeDefinition misEnCause = new PublisherNodeDefinitionBuilder()
				.addBooleanField("siHomme")
				.addStringField("nom")
				.addStringField("prenom")
				.addListField("adressesConnues", address)
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder()
				.addBooleanField("enqueteTerminee")
				.addStringField("codeEnquete")
				.addNodeField("enqueteur", enqueteur)
				.addListField("misEnCause", misEnCause)
				.addStringField("fait")
				.addBooleanField("siGrave")
				.build();

		return new PublisherDataDefinition("PuEnquete", publisherNodeDefinition);
	}

	private static PublisherDataDefinition createTestMock() {
		final PublisherNodeDefinition publisherMockNodeDefinition = new PublisherNodeDefinitionBuilder()
				.addStringField("titre")
				.addStringField("nom")
				.addStringField("prenom")
				.addStringField("address")
				.addStringField("commentaire")
				.addStringField("boolean1")
				.addBooleanField("boolean2")
				.addBooleanField("boolean3")
				.addStringField("testDummy")
				.addStringField("testLong")
				.addStringField("testDouble")
				.addStringField("testInteger")
				.addStringField("testDate")
				.addImageField("logo")
				.build();
		return new PublisherDataDefinition("PuPublisherMock", publisherMockNodeDefinition);
	}

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {

		return List.of(
				createTestEnquete(),
				createTestMock());
	}

}
