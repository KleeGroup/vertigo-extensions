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
package io.vertigo.datastore.entitystore;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.util.VCollectors;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.entitystore.data.DtDefinitions;
import io.vertigo.datastore.entitystore.data.TestSmartTypes;
import io.vertigo.datastore.entitystore.data.domain.SmartItem;

public class SortStoreManagerTest {
	private static final String Ba_aa = "Ba aa";
	private static final String aaa_ba = "aaa ba";
	private static final String bb_aa = "bb aa";
	@Inject
	private EntityStoreManager entityStoreManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() throws Exception {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public final void tearDown() throws Exception {
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
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withMemoryCache()
						.withEntityStore()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
								.build())
						.build())
				.build();
	}

	@Test
	public void testHeavySort() {
		// final DtList<Item> sortDtc;
		final DtList<SmartItem> dtc = createItems();
		//
		for (int i = 0; i < 50000; i++) {
			final SmartItem mocka = new SmartItem();
			mocka.setLabel(String.valueOf(i % 100));
			dtc.add(mocka);
		}

		final DtList<SmartItem> sortedDtc = entityStoreManager.sort(dtc, "label", false);

		nop(sortedDtc);

	}

	private void nop(DtList<SmartItem> sortedDtc) {
		//nop		
	}

	@Test
	public void testSort() {
		DtList<SmartItem> sortDtc;
		final DtList<SmartItem> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = entityStoreManager.sort(dtc, "label", false);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = entityStoreManager.sort(dtc, "label", true);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { null, bb_aa, Ba_aa, aaa_ba }, extractLabels(sortDtc));
	}

	@Test
	public void testNumericSort() {
		DtList<SmartItem> sortDtc;
		final DtList<SmartItem> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		// Cas de base.
		// ======================== Ascendant
		// =================================== nullLast
		// ================================================ ignoreCase
		sortDtc = entityStoreManager.sort(dtc, "id", false);

		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { Ba_aa, null, aaa_ba, bb_aa }, extractLabels(sortDtc));

		// ======================== Descendant
		// =================================== not nullLast
		// ================================================ ignoreCase
		sortDtc = entityStoreManager.sort(dtc, "id", true);
		assertEquals(indexDtc, extractLabels(dtc));
		assertEquals(new String[] { bb_aa, aaa_ba, null, Ba_aa }, extractLabels(sortDtc));
	}

	/**
	 * combiner sort/filter ; filter/sort ; sublist/sort ; filter/sublist.
	 *
	 */
	@Test
	public void testChainFilterSortSubList() {

		final DtList<SmartItem> dtc = createItems();
		final String[] indexDtc = extractLabels(dtc);

		final Predicate<SmartItem> predicate = Criterions.<SmartItem> isEqualTo(() -> "label", aaa_ba).toPredicate();
		final Function<DtList<SmartItem>, DtList<SmartItem>> sort = (list) -> entityStoreManager.sort(list, "label", false);

		final int sizeDtc = dtc.size();

		DtList<SmartItem> sortDtc, filterDtc, subList;
		// ======================== sort/filter
		sortDtc = sort.apply(dtc);
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));
		filterDtc = sortDtc.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(SmartItem.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sort/sublist
		sortDtc = sort.apply(dtc);
		assertEquals(new String[] { aaa_ba, Ba_aa, bb_aa, null }, extractLabels(sortDtc));
		subList = subList(sortDtc, 0, sizeDtc - 1);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== filter/sort
		filterDtc = dtc.stream().filter(predicate).collect(VCollectors.toDtList(SmartItem.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));
		sortDtc = sort.apply(filterDtc);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== filter/sublist
		filterDtc = dtc.stream().filter(predicate).collect(VCollectors.toDtList(SmartItem.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));
		subList = subList(filterDtc, 0, filterDtc.size() - 1);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sublist/sort
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba_aa, null, aaa_ba }, extractLabels(subList));
		sortDtc = sort.apply(subList);
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// ======================== sublist/filter
		subList = subList(dtc, 0, sizeDtc - 1);
		assertEquals(new String[] { Ba_aa, null, aaa_ba }, extractLabels(subList));
		filterDtc = subList.stream().filter(predicate).collect(VCollectors.toDtList(SmartItem.class));
		assertEquals(new String[] { aaa_ba }, extractLabels(filterDtc));

		// === dtc non modifié
		assertEquals(indexDtc, extractLabels(dtc));

	}

	private DtList<SmartItem> subList(final DtList<SmartItem> dtc, final int start, final int end) {
		return dtc
				.stream()
				.skip(start)
				.limit(end - start)
				.collect(VCollectors.toDtList(dtc.getDefinition()));
	}

	/**
	 * Asserts that two booleans are equal.
	 *
	 */
	private static void assertEquals(final String[] expected, final String[] actual) {
		Assertions.assertEquals(Arrays.toString(expected), Arrays.toString(actual));
	}

	private static String[] extractLabels(final DtList<SmartItem> dtc) {
		final String[] index = new String[dtc.size()];
		for (int i = 0; i < dtc.size(); i++) {
			index[i] = dtc.get(i).getLabel();
		}
		return index;
	}

	private static long seqId = 100;

	private static DtList<SmartItem> createItems() {
		final DtList<SmartItem> dtc = new DtList<>(SmartItem.class);
		// les index sont données par ordre alpha > null à la fin >
		final SmartItem mockB = new SmartItem();
		mockB.setId(seqId++);
		mockB.setLabel(Ba_aa);
		dtc.add(mockB);

		final SmartItem mockNull = new SmartItem();
		mockNull.setId(seqId++);
		// On ne renseigne pas le Label > null
		dtc.add(mockNull);

		final SmartItem mocka = new SmartItem();
		mocka.setId(seqId++);
		mocka.setLabel(aaa_ba);
		dtc.add(mocka);

		final SmartItem mockb = new SmartItem();
		mockb.setId(seqId++);
		mockb.setLabel(bb_aa);
		dtc.add(mockb);

		// On crée et on supprimme un élément dans la liste pour vérifier
		// l'intégrité de la liste (Par rapport aux null).
		final SmartItem mockRemoved = new SmartItem();
		mockRemoved.setId(seqId++);
		mockRemoved.setLabel("mockRemoved");
		dtc.add(mockRemoved);

		dtc.remove(mockRemoved);
		return dtc;
	}

}
