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
package io.vertigo.quarto.publisher.standard;

import java.util.List;

import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.quarto.publisher.definitions.PublisherDataDefinition;
import io.vertigo.quarto.publisher.definitions.PublisherNodeDefinition;
import io.vertigo.quarto.publisher.definitions.PublisherNodeDefinitionBuilder;

public final class TestStandardPublisherDefinitionProvider implements SimpleDefinitionProvider {

	private static PublisherDataDefinition createTest() {
		final PublisherNodeDefinition rootNodeDefinition = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.build();
		return new PublisherDataDefinition("PuTest", rootNodeDefinition);
	}

	private static PublisherDataDefinition createTest2() {

		final PublisherNodeDefinition subDefinition = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.addNodeField("testData", subDefinition)
				.build();
		return new PublisherDataDefinition("PuTest2", publisherNodeDefinition);
	}

	private static PublisherDataDefinition createTest3() {
		final PublisherNodeDefinition subDefinition1 = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.build();

		final PublisherNodeDefinition subDefinition3 = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.addNodeField("testData", subDefinition1)
				.addListField("testList", subDefinition3)
				.build();
		return new PublisherDataDefinition("PuTest3", publisherNodeDefinition);
	}

	private static PublisherDataDefinition createTest4() {
		final PublisherNodeDefinition subDefinition1 = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.build();

		final PublisherNodeDefinition subDefinition3 = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.addNodeField("testData", subDefinition1)
				.addListField("testList", subDefinition3)
				.addImageField("testImage")
				.build();
		return new PublisherDataDefinition("PuTest4", publisherNodeDefinition);

	}

	private static PublisherDataDefinition createTest5() {
		final PublisherNodeDefinition subDefinition2 = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.build();

		final PublisherNodeDefinition subDefinition1 = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.addNodeField("testData", subDefinition2)
				.build();

		final PublisherNodeDefinition subDefinition4 = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.build();

		final PublisherNodeDefinition subDefinition3 = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.addListField("testList", subDefinition4)
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder()
				.addBooleanField("testBoolean")
				.addStringField("testString")
				.addNodeField("testData", subDefinition1)
				.addListField("testList", subDefinition3)
				.build();
		return new PublisherDataDefinition("PuTest5", publisherNodeDefinition);
	}

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

		return new PublisherDataDefinition("PuTestEnquete", publisherNodeDefinition);
	}

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {

		return List.of(
				createTest(),
				createTest2(),
				createTest3(),
				createTest4(),
				createTest5(),
				createTestEnquete());
	}

}
