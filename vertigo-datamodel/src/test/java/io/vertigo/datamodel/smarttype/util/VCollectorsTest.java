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
package io.vertigo.datamodel.smarttype.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.smarttype.data.DtDefinitions;
import io.vertigo.datamodel.smarttype.data.TestSmartTypes;
import io.vertigo.datamodel.smarttype.data.domain.Artist;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.util.VCollectors;

/**
 *
 * @author xdurand
 *
 */
public class VCollectorsTest {
	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.withLocales("fr_FR")
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
								.build())
						.build())
				.build();
	}

	/**
	 * Test du VCollectors.toDtList sur une liste vide
	 */
	@Test
	public void testCollectDtListEmpty() {
		final DtList<Artist> emptyDtList = new DtList<>(Artist.class);
		final DtList<Artist> listCollected = emptyDtList.stream().collect(VCollectors.toDtList(Artist.class));

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty());
		assertEquals(0, listCollected.size());
	}

	private static Artist createArtist(final long id, final String name) {
		final Artist m = new Artist();
		m.setId(id);
		m.setName(name);
		return m;
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide sans filtrage
	 */
	@Test
	public void testCollectDtList() {
		final Artist m1 = createArtist(1, "David Bowie");
		final Artist m2 = createArtist(2, "Joe Strummer");

		final DtList<Artist> dtList = DtList.of(m1, m2);
		// @formatter:off
		final DtList<Artist> listCollected = dtList.stream()
											.sorted( (art1, art2) -> art1.getId().compareTo(art2.getId()))
											.collect(VCollectors.toDtList(Artist.class));
		// @formatter:on

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty() == false);
		assertEquals(2, listCollected.size());
		assertEquals(listCollected.get(0), m1);
		assertEquals(listCollected.get(1), m2);
		assertEquals(2, dtList.size());
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide avec filtrage
	 */
	@Test
	public void testFilterCollectDtList() {
		final Artist m1 = createArtist(1, "Louis Armstrong");
		final Artist m2 = createArtist(2, "Duke Ellington");
		final Artist m3 = createArtist(3, "Jimmy Hendricks");

		final DtList<Artist> dtList = DtList.of(m1, m2, m3);

		// @formatter:off
		final DtList<Artist> listCollected = dtList.stream()
											.filter( m -> m.getId() % 2 == 0)
											.collect(VCollectors.toDtList(Artist.class));
		// @formatter:on
		assertNotNull(listCollected);
		Assertions.assertFalse(listCollected.isEmpty());
		assertEquals(1, listCollected.size());
		assertEquals(listCollected.get(0), m2);
		assertEquals(3, dtList.size());
	}

}
