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
package io.vertigo.datafactory.search;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.lang.VUserException;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.datafactory.collections.ListFilter;
import io.vertigo.datafactory.collections.definitions.FacetDefinition;
import io.vertigo.datafactory.collections.model.Facet;
import io.vertigo.datafactory.collections.model.FacetValue;
import io.vertigo.datafactory.collections.model.FacetedQuery;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datafactory.impl.search.dsl.DslListFilterBuilder;
import io.vertigo.datafactory.search.data.domain.GeoPoint;
import io.vertigo.datafactory.search.data.domain.Item;
import io.vertigo.datafactory.search.data.domain.ItemDataBase;
import io.vertigo.datafactory.search.data.domain.ItemSearchLoader;
import io.vertigo.datafactory.search.definitions.SearchIndexDefinition;
import io.vertigo.datafactory.search.model.SearchIndex;
import io.vertigo.datafactory.search.model.SearchQuery;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.UID;

/**
 * @author  npiedeloup
 */
public abstract class AbstractSearchManagerTest {
	private static final SelectedFacetValues EMPTY_SELECTED_FACET_VALUES = SelectedFacetValues.empty().build();

	/** Logger. */
	private final Logger log = LogManager.getLogger(getClass());

	/** Manager de recherche. */
	@Inject
	private SearchManager searchManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//--
		doSetUp();
		removeAll();
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	protected abstract void doSetUp();

	protected abstract NodeConfig buildNodeConfig();

	/** IndexDefinition. */
	private SearchIndexDefinition itemIndexDefinition;
	private FacetDefinition manufacturerFacetDefinition;
	private FacetDefinition yearFacetDefinition;
	private FacetDefinition geoFacetDefinition;
	private FacetDefinition geoCircleFacetDefinition;
	private FacetDefinition geoHashClusterFacetDefinition;
	private ItemDataBase itemDataBase;

	/**
	 * Initialise l'index.
	 * @param indexName Nom de l'index
	 */
	protected final void init(final String indexName) {
		final DefinitionSpace definitionSpace = node.getDefinitionSpace();
		//On construit la BDD des voitures
		itemDataBase = new ItemDataBase();
		final ItemSearchLoader itemSearchLoader = node.getComponentSpace().resolve(ItemSearchLoader.class);
		itemSearchLoader.bindDataBase(itemDataBase);

		manufacturerFacetDefinition = definitionSpace.resolve("FctManufacturerItem", FacetDefinition.class);
		yearFacetDefinition = definitionSpace.resolve("FctYearItem", FacetDefinition.class);
		geoFacetDefinition = definitionSpace.resolve("FctLocalisationItem", FacetDefinition.class);
		geoCircleFacetDefinition = definitionSpace.resolve("FctLocalisationCircleItem", FacetDefinition.class);
		geoHashClusterFacetDefinition = definitionSpace.resolve("FctLocalisationHashItem", FacetDefinition.class);
		itemIndexDefinition = definitionSpace.resolve(indexName, SearchIndexDefinition.class);
		removeAll();
	}

	@BeforeAll
	public static void doBeforeClass() throws Exception {
		//We must remove data dir in index, in order to support versions updates when testing on PIC
		final URL esDataURL = Thread.currentThread().getContextClassLoader().getResource("io/vertigo/datafactory/search/indexconfig");
		final File esData = new File(URLDecoder.decode(esDataURL.getFile() + "/data", "UTF-8"));
		if (esData.exists() && esData.isDirectory()) {
			recursiveDelete(esData);
		}
	}

	private static void recursiveDelete(final File file) {

		if (file.isDirectory()) {
			//list all the directory contents
			for (final File subFile : file.listFiles()) {
				//recursive delete
				recursiveDelete(subFile);
			}
			if (!file.delete()) {
				System.err.println("Can't delete directory : " + file.getAbsolutePath());
			}
		} else {
			//if file, then delete it
			if (!file.delete()) {
				System.err.println("Can't delete file : " + file.getAbsolutePath());
			}
		}
	}

	/**
	 * Test de création nettoyage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testClean() {
		final ListFilter removeQuery = ListFilter.of("*:*");
		searchManager.removeAll(itemIndexDefinition, removeQuery);
		waitAndExpectIndexation(0);
	}

	/**
	 * Test de création de n enregistrements dans l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndex() {
		index(false);
		index(true);
		waitAndExpectIndexation(itemDataBase.size());
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexQuery() {
		index(false);
		final long size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);
	}

	/**
	 * Test de reindexation de l'index.
	 * La création s'effectue dans une seule transaction.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	@Test
	public void testReIndex() throws InterruptedException, ExecutionException, TimeoutException {
		index(true);
		long size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(itemDataBase.size(), size);

		//On supprime tout
		removeAll();
		waitAndExpectIndexation(0);
		size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(0L, size);

		//on reindex
		size = searchManager.reindexAll(itemIndexDefinition)
				.get(10, TimeUnit.SECONDS);
		//on attend 5s + le temps de reindexation
		Assertions.assertEquals(itemDataBase.size(), size);
		waitAndExpectIndexation(itemDataBase.size());

		size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(itemDataBase.size(), size);
	}

	@Test
	public void testIndexTwice() {
		index(true);
		long size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(itemDataBase.size(), size);

		//On supprime tout
		doRemove("*:*");
		removeAll();
		size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(0L, size);

		//on reindex
		doIndex(false);
		index(false);
		size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(itemDataBase.size(), size);

		doIndex(true);
		index(true);

		size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(itemDataBase.size(), size);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexCount() {
		index(false);
		final long size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(itemDataBase.size(), size);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexAllQuery() {
		index(true);
		final long size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testQuery() {
		index(false);
		long size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);

		size = query("manufacturer:Peugeot"); //Les constructeur sont des mots clés donc sensible à la casse
		Assertions.assertEquals(itemDataBase.getItemsByManufacturer("peugeot").size(), (int) size);

		size = query("manufacturer:peugeot"); //Les constructeur sont des mots clés donc sensible à la casse
		Assertions.assertEquals(0L, size);

		size = query("manufacturer:Vol*"); //On compte les volkswagen
		Assertions.assertEquals(itemDataBase.getItemsByManufacturer("volkswagen").size(), (int) size);

		size = query("manufacturer:vol*"); //On compte les volkswagen
		Assertions.assertEquals(0L, (int) size); //Les constructeur sont des mots clés donc sensible à la casse (y compris en wildcard)

		size = query("year:[* TO 2005]"); //On compte les véhicules avant 2005
		Assertions.assertEquals(itemDataBase.before(2005), size);

		size = query("description:panoRAmique");//La description est un text insenssible à la casse
		Assertions.assertEquals(itemDataBase.containsDescription("panoramique"), size);

		size = query("description:panoRAmi*");//La description est un text insenssible à la casse (y compris en wildcard)
		Assertions.assertEquals(itemDataBase.containsDescription("panoramique"), size);

		size = query("description:clim");
		Assertions.assertEquals(itemDataBase.containsDescription("clim"), size);

		size = query("description:avenir");
		Assertions.assertEquals(itemDataBase.containsDescription("avenir"), size);

		size = query("description:l'avenir");
		Assertions.assertEquals(itemDataBase.containsDescription("l'avenir"), size);
	}

	/**
	 * Test de requétage de l'index.
	 */
	@Test
	public void testGeoDistanceQuery() {
		index(false);
		long size;
		size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);

		SearchQuery searchQuery = SearchQuery.builder("*:*", DslListFilterBuilder.class)
				.withGeoSearchQuery("localisation:#query#~7km")
				.withCriteria("48.80, 2.36")
				.build();
		int size10Km = (int) doQuery(searchQuery, null).getCount();
		Assertions.assertEquals(3, size10Km);

		searchQuery = SearchQuery.builder("*:*", DslListFilterBuilder.class)
				.withGeoSearchQuery("localisation:#query#~15km")
				.withCriteria("48.80, 2.36")
				.build();
		int size15Km = (int) doQuery(searchQuery, null).getCount();
		Assertions.assertEquals(8, size15Km);

		searchQuery = SearchQuery.builder("manufacturer:Vol*", DslListFilterBuilder.class)
				.withGeoSearchQuery("localisation:#query#~7km")
				.withCriteria("48.80, 2.36")
				.build();
		size10Km = (int) doQuery(searchQuery, null).getCount();
		Assertions.assertEquals(1, size10Km);

		searchQuery = SearchQuery.builder("manufacturer:Vol*", DslListFilterBuilder.class)
				.withGeoSearchQuery("localisation:#query#~15km")
				.withCriteria("48.80, 2.36")
				.build();
		size15Km = (int) doQuery(searchQuery, null).getCount();
		Assertions.assertEquals(2, size15Km);
	}

	/**
	 * Test de requétage de l'index.
	 */
	@Test
	public void testGeoRangeQuery() {
		index(false);
		long size;
		size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);

		SearchQuery searchQuery = SearchQuery.builder("*:*", DslListFilterBuilder.class)
				.withGeoSearchQuery("localisation:[#query# TO \"47.0, 3.0\"]")
				.withCriteria("49.0, 2.0")
				.build();
		int sizeRange = (int) doQuery(searchQuery, null).getCount();
		Assertions.assertEquals(8, sizeRange);

		searchQuery = SearchQuery.builder("*:*", DslListFilterBuilder.class)
				.withGeoSearchQuery("localisation:[#query# TO \"48.77, 3.0\"]")
				.withCriteria("49.0, 2.23")
				.build();
		sizeRange = (int) doQuery(searchQuery, null).getCount();
		Assertions.assertEquals(5, sizeRange);

		searchQuery = SearchQuery.builder("manufacturer:Vol*", DslListFilterBuilder.class)
				.withGeoSearchQuery("localisation:[#query# TO \"47.0, 3.0\"]")
				.withCriteria("49.0, 2.0")
				.build();
		sizeRange = (int) doQuery(searchQuery, null).getCount();
		Assertions.assertEquals(2, sizeRange);

		searchQuery = SearchQuery.builder("manufacturer:Vol*", DslListFilterBuilder.class)
				.withGeoSearchQuery("localisation:[#query# TO \"48.77, 3.0\"]")
				.withCriteria("49.0, 2.23")
				.build();
		sizeRange = (int) doQuery(searchQuery, null).getCount();
		Assertions.assertEquals(1, sizeRange);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testCopyFieldsQuery() {
		index(false);
		long size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);

		/* _all deprecated in 6.x
		 * size = query("_all:(+peugeot +diesel)");
		 * Assertions.assertEquals(3L, size); */

		size = query("allText:(+peugeot +diesel)");
		Assertions.assertEquals(3L, size);

		size = query("model.keyword:(806)");//MODEL est tokenize, MODEL_SORT ne l'est pas (ici on test le match avec le model : "806 final ST PACK")
		Assertions.assertEquals(0L, size);

		size = query("model.keyword:(806*)");
		Assertions.assertEquals(1L, size);

		size = query("allText:(+peugeot +diesel +2001)"); //2001 est l'année en number
		Assertions.assertEquals(1L, size);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testBadSyntaxQuery() {
		index(false);
		long size;
		size = query("description:(error or)");
		Assertions.assertEquals(0L, size);

		size = query("description:or");
		Assertions.assertEquals(0L, size);

		try {
			size = query(" OR ");
			Assertions.fail("WrappedException expected");
		} catch (final WrappedException e) {
			//ok
		}

		try {
			size = query("description: OR ");
			Assertions.fail("VUserException expected");
		} catch (final VUserException e) {
			//ok
		}

		try {
			size = query("description:(error");
			Assertions.fail("VUserException expected");
		} catch (final VUserException e) {
			//ok
		}
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testUserBadSyntaxQuery() {
		index(false);
		long size;

		size = queryWithCriteria("description:#query#", "(error or)");
		Assertions.assertEquals(0L, size);

		size = queryWithCriteria("description:#query#", "or");
		Assertions.assertEquals(0L, size);

		try {
			size = queryWithCriteria("description:#query#", ": OR ");
			Assertions.fail("VUserException expected");
		} catch (final VUserException e) {
			//ok
		}

		//common bad user input are escaped
		size = queryWithCriteria("description:#query#", " OR ");
		Assertions.assertEquals(0L, size);

		//common bad user input are escaped
		size = queryWithCriteria("description:#query#", "(error");
		Assertions.assertEquals(0L, size);

		//common bad user input are escaped
		size = queryWithCriteria("description:#query#", "error) OR *:*");
		Assertions.assertEquals(itemDataBase.size(), size); //*:* passe
	}

	/**
	 * Test de requétage de l'index description : insenssible à la casse et aux accents.
	 */
	@Test
	public void testInsensitivityQuery() {
		index(false);

		final long databaseResult = itemDataBase.containsDescription("sieges") + itemDataBase.containsDescription("sièges");
		long size;
		size = query("description:sieges");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:Sieges");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:sièges");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:Sièges");
		Assertions.assertEquals(databaseResult, size);

		//y compris en wildcard
		size = query("description:sièg*"); //attention snowball retire la terminaison, ca fausse cette requete prefix
		Assertions.assertEquals(databaseResult, size);
		size = query("description:Sièg*");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:sieg*");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:Sieg*");
		Assertions.assertEquals(databaseResult, size);

		//y compris en wildcard mandatory
		size = query("description:(+sièg*)");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:(+Sièg*)");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:(+sieg*)");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:(+Sieg*)");
		Assertions.assertEquals(databaseResult, size);

	}

	/**
	 * Test de requétage de l'index description : insenssible à la casse et aux accents.
	 */
	@Test
	public void testInsensitivity2Query() {
		index(false);

		final long databaseResult = itemDataBase.containsDescription("sieges") + itemDataBase.containsDescription("sièges");
		long size;
		//y compris en wildcard
		size = query("description:siè*");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:Siè*");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:sie*");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:Sie*");
		Assertions.assertEquals(databaseResult, size);

		//y compris en wildcard mandatory
		size = query("description:(+siè*)");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:(+Siè*)");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:(+sie*)");
		Assertions.assertEquals(databaseResult, size);
		size = query("description:(+Sie*)");
		Assertions.assertEquals(databaseResult, size);

	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testSortedQuery() {
		index(false);
		Item firstItem;

		firstItem = doQueryAllAndGetFirst("manufacturer", false);
		Assertions.assertEquals("Audi", firstItem.getManufacturer());

		firstItem = doQueryAllAndGetFirst("manufacturer", true);
		Assertions.assertEquals("Volkswagen", firstItem.getManufacturer());

		firstItem = doQueryAllAndGetFirst("year", false);
		Assertions.assertEquals(1998, firstItem.getYear().intValue());

		firstItem = doQueryAllAndGetFirst("year", true);
		Assertions.assertEquals(2010, firstItem.getYear().intValue());

		final DtListState listState = DtListState.of(null, 0, itemIndexDefinition.getIndexDtDefinition().getField("model").getName(), true);
		final DtList<Item> dtList = doQuery(SearchQuery
				.builder("QryItemFacet")
				.withCriteria("")
				.build(), listState).getDtList();

		Assertions.assertEquals("Tucson 2.0 CRDi Pack Luxe BA", dtList.get(0).getModel());
		Assertions.assertEquals("passat", dtList.get(1).getModel());
		Assertions.assertEquals("Eos TDI 140 CARAT DSG", dtList.get(2).getModel());

		firstItem = doQueryAllAndGetFirst("model", true);
		Assertions.assertEquals("Tucson 2.0 CRDi Pack Luxe BA", firstItem.getModel());
	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testSortByOptionalFieldQuery() {
		index(false);
		Item firstItem;

		firstItem = doQueryAllAndGetFirst("optionalNumber", false);
		Assertions.assertEquals("Audi", firstItem.getManufacturer());

		firstItem = doQueryAllAndGetFirst("optionalString", false);
		Assertions.assertEquals("Peugeot", firstItem.getManufacturer());
	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testSortByUnknownFieldQuery() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			index(false);

			doQueryAllAndGetFirst("unknownField", false);
			Assertions.fail();
		});
	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testPaginatedQuery() {
		index(false);
		final DtList<Item> dtListFull = doQueryAll(null).getDtList();
		final DtList<Item> dtList1 = doQueryAll(DtListState.of(4, 0, null, null)).getDtList();
		final DtList<Item> dtList2 = doQueryAll(DtListState.of(4, 4, null, null)).getDtList();
		final DtList<Item> dtList3 = doQueryAll(DtListState.of(4, 2 * 4, null, null)).getDtList();

		Assertions.assertEquals(4, dtList1.size());
		Assertions.assertEquals(4, dtList2.size());
		Assertions.assertEquals(itemDataBase.size() - 2 * 4, dtList3.size()); //9 elements

		Assertions.assertEquals(dtListFull.get(0).getId(), dtList1.get(0).getId());
		Assertions.assertEquals(dtListFull.get(3).getId(), dtList1.get(dtList1.size() - 1).getId());
		Assertions.assertEquals(dtListFull.get(4).getId(), dtList2.get(0).getId());
		Assertions.assertEquals(dtListFull.get(7).getId(), dtList2.get(dtList2.size() - 1).getId());
		Assertions.assertEquals(dtListFull.get(8).getId(), dtList3.get(0).getId());
		Assertions.assertEquals(dtListFull.get(dtListFull.size() - 1).getId(), dtList3.get(dtList3.size() - 1).getId());

	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testFacetQueryByRange() {
		index(false);
		final FacetedQueryResult<Item, SearchQuery> result = facetQuery("*:*");
		testFacetResultByRange(result);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testEmptyIndexQuery() {
		//On supprime tout
		removeAll();
		long size = searchManager.count(itemIndexDefinition);
		Assertions.assertEquals(0L, size);

		size = query("*:*");
		Assertions.assertEquals(0, size);
	}

	private void testFacetResultByRange(final FacetedQueryResult<Item, ?> result) {
		Assertions.assertEquals(itemDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assertions.assertEquals(4, result.getFacets().size());

		//On recherche la facette date
		final Facet yearFacet = getFacetByName(result, "FctYearItem");
		Assertions.assertTrue(yearFacet.getDefinition().isRangeFacet());

		boolean found = false;
		for (final Entry<FacetValue, Long> entry : yearFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).contains("avant")) {
				found = true;
				Assertions.assertEquals(itemDataBase.before(2000), entry.getValue().longValue());
			}
		}
		Assertions.assertTrue(found);

		//on vérifie l'ordre
		final List<FacetValue> facetValueDefinition = yearFacet.getDefinition().getFacetRanges();
		final List<FacetValue> facetValueResult = new ArrayList<>(yearFacet.getFacetValues().keySet());
		Assertions.assertEquals(facetValueDefinition, facetValueResult); //equals vérifie aussi l'ordre
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testFacetQueryByTerm() {
		index(false);
		final FacetedQueryResult<Item, SearchQuery> result = facetQuery("*:*");
		testFacetResultByTerm(result);
	}

	@Test
	public void testFacetOptionalFieldByTerm() {
		index(false);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemOptionalFacet")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);

		Assertions.assertEquals(itemDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assertions.assertEquals(1, result.getFacets().size());

		//On recherche la facette constructeur
		final Facet optionalStringFacet = getFacetByName(result, "FctOptionalStringItem");
		//On vérifie que l'on est sur le champ Manufacturer
		Assertions.assertEquals("optionalString", optionalStringFacet.getDefinition().getDtField().getName());
		Assertions.assertFalse(optionalStringFacet.getDefinition().isRangeFacet());

		//On vérifie qu'il existe une valeur pour empty et que le nombre d'occurrences est correct
		boolean found = false;
		final String value = "_empty_";
		for (final Entry<FacetValue, Long> entry : optionalStringFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).equals(value)) {
				found = true;
				Assertions.assertEquals(1, entry.getValue().intValue()); //only one empty string (other are null)
			}
		}
		Assertions.assertTrue(found);

		checkOrderByCount(optionalStringFacet);
	}

	private void testFacetResultByTerm(final FacetedQueryResult<Item, ?> result) {
		Assertions.assertEquals(itemDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assertions.assertEquals(4, result.getFacets().size());

		//On recherche la facette constructeur
		final Facet manufacturerFacet = getFacetByName(result, "FctManufacturerItem");
		//On vérifie que l'on est sur le champ Manufacturer
		Assertions.assertEquals("manufacturer", manufacturerFacet.getDefinition().getDtField().getName());
		Assertions.assertFalse(manufacturerFacet.getDefinition().isRangeFacet());

		//On vérifie qu'il existe une valeur pour peugeot et que le nombre d'occurrences est correct
		boolean found = false;
		final String manufacturer = "peugeot";
		for (final Entry<FacetValue, Long> entry : manufacturerFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).equals(manufacturer)) {
				found = true;
				Assertions.assertEquals(itemDataBase.getItemsByManufacturer(manufacturer).size(), entry.getValue().intValue());
			}
		}
		Assertions.assertTrue(found);

		checkOrderByCount(manufacturerFacet);
		checkOrderByAlpha(getFacetByName(result, "FctManufacturerItemAlpha"));
		checkOrderByCount(getFacetByName(result, "FctDescriptionItem"));
	}

	private void testFacetResultByGeo(final FacetedQueryResult<Item, ?> result, final GeoPoint origin) {
		Assertions.assertEquals(itemDataBase.size() - 1, result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assertions.assertEquals(2, result.getFacets().size());

		//On recherche la facette constructeur
		final Facet localisationFacet = getFacetByName(result, "FctLocalisationItem");
		//On vérifie que l'on est sur le champ Manufacturer
		Assertions.assertEquals("localisation", localisationFacet.getDefinition().getDtField().getName());
		Assertions.assertTrue(localisationFacet.getDefinition().isRangeFacet());

		Assertions.assertEquals(5, localisationFacet.getFacetValues().size());
		for (final Entry<FacetValue, Long> entry : localisationFacet.getFacetValues().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH);
			final long searchFacetCount = entry.getValue();
			if ("< 5km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(result.getDtList(), origin, 5000), searchFacetCount);
			} else if ("< 7km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(result.getDtList(), origin, 7000), searchFacetCount);
			} else if ("< 8.5km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(result.getDtList(), origin, 8500), searchFacetCount);
			} else if ("< 10km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(result.getDtList(), origin, 10000), searchFacetCount);
			} else if ("< 20km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(result.getDtList(), origin, 20000), searchFacetCount);
			} else {
				Assertions.fail("Unexpected facet " + searchFacetLabel);
			}
		}
	}

	private void checkOrderByCount(final Facet facet) {
		//on vérifie l'ordre
		int lastCount = Integer.MAX_VALUE;
		for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
			Assertions.assertTrue(entry.getValue().intValue() <= lastCount, "Ordre des facettes par 'count' non respecté");
			lastCount = entry.getValue().intValue();
		}
	}

	private void checkOrderByAlpha(final Facet facet) {
		//on vérifie l'ordre
		String lastLabel = "";
		for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
			final String label = entry.getKey().getLabel().getDisplay();
			Assertions.assertTrue(label.compareTo(lastLabel) >= 0, "Ordre des facettes par 'alpha' non respecté");
			lastLabel = label;
		}
	}

	private static Facet getFacetByName(final FacetedQueryResult<Item, ?> result, final String facetName) {
		return result.getFacets()
				.stream()
				.filter(facet -> facetName.equals(facet.getDefinition().getName()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException());
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testGeoFacetQueryByTerm() {
		index(false);
		final GeoPoint origin = new GeoPoint(48.80f, 2.36f);
		final Item criteria = new Item();
		criteria.setLocalisation(origin);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacetGeo")
				.withCriteria(criteria)
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		testFacetResultByGeo(result, origin);

		//on applique une facette
		final SearchQuery searchQuery2 = SearchQuery.builder("QryItemFacetGeo")
				.withCriteria(criteria)
				.withFacet(createFacetQuery("FctLocalisationItem", "< 7km", result))
				.build();
		final FacetedQueryResult<Item, SearchQuery> resultFiltered = searchManager.loadList(itemIndexDefinition, searchQuery2, null);
		Assertions.assertEquals(itemDataBase.near(origin, 7000), resultFiltered.getCount());

	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testGeoCircleFacetQueryByTerm() {
		index(false);
		final GeoPoint origin = new GeoPoint(48.80f, 2.36f);
		final Item criteria = new Item();
		criteria.setLocalisation(origin);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacetGeo")
				.withCriteria(criteria)
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		testFacetResultByGeo(result, origin);

		//on applique une facette
		final SearchQuery searchQuery2 = SearchQuery.builder("QryItemFacetGeo")
				.withCriteria(criteria)
				.withFacet(createFacetQuery("FctLocalisationCircleItem", "7 à 8.5km", result))
				.build();
		final FacetedQueryResult<Item, SearchQuery> resultFiltered = searchManager.loadList(itemIndexDefinition, searchQuery2, null);
		Assertions.assertEquals(itemDataBase.near(origin, 8500) - itemDataBase.near(origin, 7000), resultFiltered.getCount());

	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testSecurityQuery() {
		index(false);
		long size;
		size = queryWithSecurityFilter("*:*", "+year:[ 2005 TO * ]");
		Assertions.assertEquals(itemDataBase.size() - itemDataBase.before(2005), size);

		size = queryWithSecurityFilter("manufacturer:Peugeot", "+year:[2005 TO * ]"); //Les constructeur sont des mots clés donc sensible à la casse
		Assertions.assertEquals(0L, (int) size);

		size = queryWithSecurityFilter("manufacturer:Vol*", "+year:[2005 TO *]"); //On compte les volkswagen
		Assertions.assertEquals(itemDataBase.getItemsByManufacturer("volkswagen").size(), (int) size);

		size = queryWithSecurityFilter("year:[* TO 2005]", "+year:[2005 TO *]"); //On compte les véhicules avant 2005
		Assertions.assertEquals(0L, size);

		size = queryWithSecurityFilter("description:siège", "+year:[2005 TO *]");//La description est un text insenssible à la casse
		Assertions.assertEquals(2L, size);

	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemove() {
		index(false);
		final long size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);
		//On en supprime 2
		doRemove(2);
		waitAndExpectIndexation(itemDataBase.size() - 2);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveByQuery() {
		index(false);
		final long size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);
		//on compte les Peugeots
		final int nbPeugeot = itemDataBase.getItemsByManufacturer("Peugeot").size();
		//On supprime toute les Peugeots
		doRemove("manufacturer:Peugeot");
		waitAndExpectIndexation(itemDataBase.size() - nbPeugeot);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveAll() {
		index(false);
		final long size = query("*:*");
		Assertions.assertEquals(itemDataBase.size(), size);
		//On supprime tout
		removeAll();
		final long resize = query("*:*");
		Assertions.assertEquals(0L, resize);
	}

	/**
	 * Test le facettage par range d'une liste.
	 */
	@Test
	public void testFacetListByRange() {
		index(true);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		testFacetResultByRange(result);
	}

	/**
	 * Test le facettage par range d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByRange() {
		index(true);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);

		//on applique une facette
		final SearchQuery searchQuery2 = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(createFacetQuery("FctYearItem", "avant", result))
				.build();
		final FacetedQueryResult<Item, SearchQuery> resultFiltered = searchManager.loadList(itemIndexDefinition, searchQuery2, null);
		Assertions.assertEquals(itemDataBase.before(2000), resultFiltered.getCount());
	}

	private static FacetedQuery createFacetQuery(final String facetName, final String facetValueLabel, final FacetedQueryResult<Item, ?> result) {
		FacetValue facetValue = null; //pb d'initialisation, et Assertions.notNull ne suffit pas
		final Facet facet = getFacetByName(result, facetName);
		for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).contains(facetValueLabel)) {
				facetValue = entry.getKey();
				break;
			}
		}
		if (facetValue == null) {
			throw new IllegalArgumentException("Pas de FacetValue contenant " + facetValueLabel + " dans la facette " + facetName);
		}
		final FacetedQuery previousQuery = result.getFacetedQuery().get();
		final SelectedFacetValues queryFilters = SelectedFacetValues
				.of(previousQuery.getSelectedFacetValues())
				.add(facet.getDefinition(), facetValue)
				.build();
		return new FacetedQuery(previousQuery.getDefinition(), queryFilters);
	}

	private static long getFacetValueCount(final String facetName, final String facetValueLabel, final FacetedQueryResult<Item, ?> result) {
		final Facet facet = getFacetByName(result, facetName);
		for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH).contains(facetValueLabel)) {
				return entry.getValue();
			}
		}
		return 0;
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testFacetListByTerm() {
		index(true);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		testFacetResultByTerm(result);
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByTerm() {
		index(true);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		Assertions.assertEquals(itemDataBase.getItemsByManufacturer("peugeot").size(), getFacetValueCount("FctManufacturerItem", "peugeot", result));
		//on applique une facette
		final SearchQuery searchQuery2 = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(createFacetQuery("FctManufacturerItem", "peugeot", result))
				.build();
		final FacetedQueryResult<Item, SearchQuery> resultFiltered = searchManager.loadList(itemIndexDefinition, searchQuery2, null);
		Assertions.assertEquals(itemDataBase.getItemsByManufacturer("peugeot").size(), (int) resultFiltered.getCount());
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par deux facettes.
	 */
	@Test
	public void testFilterFacetListByTwoTerms() {
		index(true);
		final List<Item> peugeotItems = itemDataBase.getItemsByManufacturer("peugeot");
		final long peugeotContainsCuirCount = ItemDataBase.containsDescription(peugeotItems, "cuir");

		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		//logResult(result);
		//on applique une facette
		Assertions.assertEquals(peugeotItems.size(), getFacetValueCount("FctManufacturerItem", "peugeot", result));
		final SearchQuery searchQuery2 = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(createFacetQuery("FctManufacturerItem", "peugeot", result))
				.build();
		final FacetedQueryResult<Item, SearchQuery> result1 = searchManager.loadList(itemIndexDefinition, searchQuery2, null);
		Assertions.assertEquals(peugeotItems.size(), (int) result1.getCount());
		logResult(result1);
		//on applique une autre facette
		Assertions.assertEquals(peugeotContainsCuirCount, getFacetValueCount("FctDescriptionItem", "cuir", result1));
		final SearchQuery searchQuery3 = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(createFacetQuery("FctDescriptionItem", "cuir", result1))
				.build();
		final FacetedQueryResult<Item, SearchQuery> result2 = searchManager.loadList(itemIndexDefinition, searchQuery3, null);
		Assertions.assertEquals(peugeotContainsCuirCount, (int) result2.getCount());
		logResult(result2);
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par deux facettes.
	 */
	@Test
	public void testFilterMultiSelectableFacetListByTwoTerms() {
		index(true);
		final List<Item> peugeotItems = itemDataBase.getItemsByManufacturer("peugeot");
		final List<Item> volkswagenItems = itemDataBase.getItemsByManufacturer("volkswagen");
		final List<Item> peugeotVolkswagenItems = itemDataBase.getItemsByManufacturers("peugeot", "volkswagen");

		final int audiItemsSize = itemDataBase.getItemsByManufacturer("audit").size();

		final long peugeot2000To2005Count = ItemDataBase.between(peugeotItems, 2000, 2005);
		final long peugeotVolkswagen2000To2005Count = ItemDataBase.between(peugeotVolkswagenItems, 2000, 2005);

		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacetMulti")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		//logResult(result);
		//on applique une facette
		Assertions.assertEquals(peugeotItems.size(), getFacetValueCount("FctManufacturerItemMulti", "peugeot", result));
		final SearchQuery searchQuery2 = SearchQuery.builder("QryItemFacetMulti")
				.withCriteria("")
				.withFacet(createFacetQuery("FctManufacturerItemMulti", "peugeot", result))
				.build();
		final FacetedQueryResult<Item, SearchQuery> result1 = searchManager.loadList(itemIndexDefinition, searchQuery2, null);
		logResult(result1);
		//on vérifie qu'il y a bien que des Peugeots
		Assertions.assertEquals(peugeotItems.size(), (int) result1.getCount());
		//on vérifie qu'il y a bien que la facette Manufacturer_ITEM à bien les autres constructeurs
		Assertions.assertEquals(peugeotItems.size(), getFacetValueCount("FctManufacturerItemMulti", "peugeot", result1));
		Assertions.assertEquals(volkswagenItems.size(), getFacetValueCount("FctManufacturerItemMulti", "volkswagen", result1));
		Assertions.assertEquals(audiItemsSize, getFacetValueCount("FctManufacturerItemMulti", "Audi", result1));
		//on vérifie que les autres facettes ont bien que des Peugeots
		Assertions.assertEquals(peugeot2000To2005Count, getFacetValueCount("FctYearItem", "2000-2005", result1));

		//on applique une autre facette
		final SearchQuery searchQuery3 = SearchQuery.builder("QryItemFacetMulti")
				.withCriteria("")
				.withFacet(createFacetQuery("FctManufacturerItemMulti", "volkswagen", result1)) //on ajoute cette selection facette (l'ancienne est reprise)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result2 = searchManager.loadList(itemIndexDefinition, searchQuery3, null);
		logResult(result2);

		//on vérifie qu'il y a bien des Peugeots et des Volkswagens
		Assertions.assertEquals(peugeotItems.size() + volkswagenItems.size(), (int) result2.getCount());
		//on vérifie qu'il y a bien que la facette Manufacturer_ITEM à bien les autres constructeurs
		Assertions.assertEquals(peugeotItems.size(), getFacetValueCount("FctManufacturerItemMulti", "peugeot", result2));
		Assertions.assertEquals(volkswagenItems.size(), getFacetValueCount("FctManufacturerItemMulti", "volkswagen", result2));
		Assertions.assertEquals(audiItemsSize, getFacetValueCount("FctManufacturerItemMulti", "Audi", result2));
		//on vérifie que les autres facettes ont bien que des Peugeots et des Volkswagens
		Assertions.assertEquals(peugeotVolkswagen2000To2005Count, getFacetValueCount("FctYearItem", "2000-2005", result2));
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par deux facettes.
	 */
	@Test
	public void testFilterMultiSelectableFacetListByTwoTermsAndRange() {
		index(true);
		final List<Item> peugeotItems = itemDataBase.getItemsByManufacturer("peugeot");
		final List<Item> volkswagenItems = itemDataBase.getItemsByManufacturer("volkswagen");
		final List<Item> audiItems = itemDataBase.getItemsByManufacturer("audi");
		final List<Item> peugeotVolkswagenItems = itemDataBase.getItemsByManufacturers("peugeot", "volkswagen");

		final int audiItemsSize = itemDataBase.getItemsByManufacturer("audit").size();

		final long peugeot2000To2005Count = ItemDataBase.between(peugeotItems, 2000, 2005);
		final long volkswagen2000To2005Count = ItemDataBase.between(volkswagenItems, 2000, 2005);
		final long audi2000To2005Count = ItemDataBase.between(audiItems, 2000, 2005);
		final long peugeotVolkswagen2000To2005Count = ItemDataBase.between(peugeotVolkswagenItems, 2000, 2005);

		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacetMulti")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result1 = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		//logResult(result);
		//on applique une facette
		Assertions.assertEquals(peugeotItems.size(), getFacetValueCount("FctManufacturerItemMulti", "peugeot", result1));
		final SearchQuery searchQuery2 = SearchQuery.builder("QryItemFacetMulti")
				.withCriteria("")
				.withFacet(createFacetQuery("FctManufacturerItemMulti", "peugeot", result1))
				.build();
		final FacetedQueryResult<Item, SearchQuery> result2 = searchManager.loadList(itemIndexDefinition, searchQuery2, null);
		logResult(result2);
		//on vérifie qu'il y a bien que des Peugeots
		Assertions.assertEquals(peugeotItems.size(), (int) result2.getCount());
		//on vérifie qu'il y a bien que la facette Manufacturer_ITEM à bien les autres constructeurs
		Assertions.assertEquals(peugeotItems.size(), getFacetValueCount("FctManufacturerItemMulti", "peugeot", result2));
		Assertions.assertEquals(volkswagenItems.size(), getFacetValueCount("FctManufacturerItemMulti", "volkswagen", result2));
		Assertions.assertEquals(audiItemsSize, getFacetValueCount("FctManufacturerItemMulti", "Audi", result2));
		//on vérifie que les autres facettes ont bien que des Peugeots
		Assertions.assertEquals(peugeot2000To2005Count, getFacetValueCount("FctYearItem", "2000-2005", result2));

		//on applique une autre facette
		final SearchQuery searchQuery3 = SearchQuery.builder("QryItemFacetMulti")
				.withCriteria("")
				.withFacet(createFacetQuery("FctManufacturerItemMulti", "volkswagen", result2)) //on ajoute cette selection facette (l'ancienne est reprise)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result3 = searchManager.loadList(itemIndexDefinition, searchQuery3, null);
		logResult(result3);

		//on vérifie qu'il y a bien des Peugeots et des Volkswagens
		Assertions.assertEquals(peugeotItems.size() + volkswagenItems.size(), (int) result3.getCount());
		//on vérifie qu'il y a bien que la facette Manufacturer_ITEM à bien les autres constructeurs
		Assertions.assertEquals(peugeotItems.size(), getFacetValueCount("FctManufacturerItemMulti", "peugeot", result3));
		Assertions.assertEquals(volkswagenItems.size(), getFacetValueCount("FctManufacturerItemMulti", "volkswagen", result3));
		Assertions.assertEquals(audiItemsSize, getFacetValueCount("FctManufacturerItemMulti", "Audi", result3));
		//on vérifie que les autres facettes ont bien que des Peugeots et des Volkswagens
		Assertions.assertEquals(peugeotVolkswagen2000To2005Count, getFacetValueCount("FctYearItem", "2000-2005", result3));

		//on applique une facette sur le range de date
		final SearchQuery searchQuery4 = SearchQuery.builder("QryItemFacetMulti")
				.withCriteria("")
				.withFacet(createFacetQuery("FctYearItem", "2000-2005", result3))
				.build();
		final FacetedQueryResult<Item, SearchQuery> result4 = searchManager.loadList(itemIndexDefinition, searchQuery4, null);
		logResult(result4);

		//on vérifie qu'il y a bien des Peugeots et des Volkswagens
		Assertions.assertEquals(peugeotVolkswagen2000To2005Count, (int) result4.getCount());
		//on vérifie qu'il y a bien que la facette MANUFACTURER_ITEM à bien les autres constructeurs
		Assertions.assertEquals(peugeot2000To2005Count, getFacetValueCount("FctManufacturerItemMulti", "peugeot", result4));
		Assertions.assertEquals(volkswagen2000To2005Count, getFacetValueCount("FctManufacturerItemMulti", "volkswagen", result4));
		Assertions.assertEquals(audi2000To2005Count, getFacetValueCount("FctManufacturerItemMulti", "Audi", result4));
		//on vérifie que les autres facettes ont bien que des Peugeots et des Volkswagens
		Assertions.assertEquals(peugeotVolkswagen2000To2005Count, getFacetValueCount("FctYearItem", "2000-2005", result4));
	}

	/**
	 * Test le facettage par range d'une liste.
	 * Et le filtrage par deux facettes term et range.
	 */
	@Test
	public void testFilterFacetListByRangeAndTerm() {
		index(true);
		final long item2000To2005Count = itemDataBase.before(2005) - itemDataBase.before(2000);
		final List<Item> peugeotItems = itemDataBase.getItemsByManufacturer("peugeot");
		final long peugeot2000To2005Count = ItemDataBase.between(peugeotItems, 2000, 2005);

		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);
		logResult(result);
		//on applique une facette
		Assertions.assertEquals(item2000To2005Count, getFacetValueCount("FctYearItem", "2000-2005", result));

		final SearchQuery searchQuery2 = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(createFacetQuery("FctYearItem", "2000-2005", result))
				.build();
		final FacetedQueryResult<Item, SearchQuery> result2 = searchManager.loadList(itemIndexDefinition, searchQuery2, null);

		Assertions.assertEquals(item2000To2005Count, result2.getCount());
		logResult(result2);
		//on applique une autre facette
		Assertions.assertEquals(peugeot2000To2005Count, getFacetValueCount("FctManufacturerItem", "peugeot", result2));

		final SearchQuery searchQuery3 = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(createFacetQuery("FctManufacturerItem", "peugeot", result2))
				.build();
		final FacetedQueryResult<Item, SearchQuery> result1 = searchManager.loadList(itemIndexDefinition, searchQuery3, null);
		Assertions.assertEquals(peugeot2000To2005Count, (int) result1.getCount());
		logResult(result1);
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testClusterByFacetTerm() {
		index(true);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacetClustering(manufacturerFacetDefinition)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);

		//On vérifie qu'il existe une valeur pour chaque marques et que le nombre d'occurrences est correct
		final Map<String, List<Item>> databaseCluster = new HashMap<>();
		for (final Item item : itemDataBase.getAllItems()) {
			databaseCluster.computeIfAbsent(item.getManufacturer().toLowerCase(Locale.FRENCH),
					k -> new ArrayList<>())
					.add(item);
		}
		int previousCount = Integer.MAX_VALUE;
		Assertions.assertEquals(databaseCluster.size(), result.getClusters().size());
		for (final Entry<FacetValue, DtList<Item>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH);
			final int searchFacetCount = entry.getValue().size();
			final List<Item> itemsByManufacturer = databaseCluster.get(searchFacetLabel);
			Assertions.assertEquals(itemsByManufacturer.size(), searchFacetCount);
			Assertions.assertTrue(previousCount >= searchFacetCount, "Group order invalid");
			previousCount = searchFacetCount;
			for (final Item item : entry.getValue()) {
				Assertions.assertEquals(searchFacetLabel, item.getManufacturer().toLowerCase(Locale.FRENCH));
			}
		}
	}

	private enum YearCluster {
		before2000("avant 2000"), between2000and2005("2000-2005"), after2005("apres 2005");

		private final String label;

		YearCluster(final String label) {
			this.label = label;
		}

		String getLabel() {
			return label;
		}
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testClusterByFacetRange() {
		index(true);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacetClustering(yearFacetDefinition) // "avant 2000", "2000-2005", "après 2005"
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);

		//On vérifie qu'il existe une valeur pour chaque marques et que le nombre d'occurrences est correct
		final Map<String, List<Item>> databaseCluster = new HashMap<>();
		databaseCluster.put(YearCluster.before2000.getLabel(), new ArrayList<>());
		databaseCluster.put(YearCluster.between2000and2005.getLabel(), new ArrayList<>());
		databaseCluster.put(YearCluster.after2005.getLabel(), new ArrayList<>());
		for (final Item item : itemDataBase.getAllItems()) {
			if (item.getYear() < 2000) {
				databaseCluster.get(YearCluster.before2000.getLabel()).add(item);
			} else if (item.getYear() < 2005) {
				databaseCluster.get(YearCluster.between2000and2005.getLabel()).add(item);
			} else {
				databaseCluster.get(YearCluster.after2005.getLabel()).add(item);
			}
		}
		Assertions.assertEquals(databaseCluster.size(), result.getClusters().size());
		for (final Entry<FacetValue, DtList<Item>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH);
			final int searchFacetCount = entry.getValue().size();
			final List<Item> itemsByYear = databaseCluster.get(searchFacetLabel);
			Assertions.assertEquals(itemsByYear.size(), searchFacetCount);
			for (final Item item : entry.getValue()) {
				if (item.getYear() < 2000) {
					Assertions.assertEquals(searchFacetLabel, YearCluster.before2000.getLabel());
				} else if (item.getYear() < 2005) {
					Assertions.assertEquals(searchFacetLabel, YearCluster.between2000and2005.getLabel());
				} else {
					Assertions.assertEquals(searchFacetLabel, YearCluster.after2005.getLabel());
				}
			}
		}
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testSortedClusterByFacetTerm() {
		index(true);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacetClustering(manufacturerFacetDefinition)
				.build();

		final DtListState listState = DtListState.of(null, 0, itemIndexDefinition.getIndexDtDefinition().getField("year").getName(), true);
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, listState);

		//On vérifie qu'il existe une valeur pour chaque marques et que la première est bien la plus ancienne
		final Map<String, Set<Item>> databaseCluster = new HashMap<>();
		for (final Item item : itemDataBase.getAllItems()) {
			databaseCluster.computeIfAbsent(item.getManufacturer().toLowerCase(Locale.FRENCH),
					k -> new TreeSet<>((e1, e2) -> e2.getYear().compareTo(e1.getYear())))
					.add(item);
		}
		Assertions.assertEquals(databaseCluster.size(), result.getClusters().size());
		for (final Entry<FacetValue, DtList<Item>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH);
			final Item firstClusterItem = entry.getValue().get(0);
			final Set<Item> itemsByManufacturer = databaseCluster.get(searchFacetLabel);
			Assertions.assertEquals(itemsByManufacturer.iterator().next().getId(), firstClusterItem.getId());
			for (final Item item : entry.getValue()) {
				Assertions.assertEquals(searchFacetLabel, item.getManufacturer().toLowerCase(Locale.FRENCH));
			}
		}
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testClusterByFacetRangeVerySmallMaxRows() {
		index(true);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacetClustering(yearFacetDefinition) // "avant 2000", "2000-2005", "après 2005"
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, DtListState.of(1));

		//On vérifie qu'il existe une valeur pour chaque marques et que le nombre d'occurrences est correct
		final Map<String, List<Item>> databaseCluster = new HashMap<>();
		databaseCluster.put(YearCluster.before2000.getLabel(), new ArrayList<>());
		databaseCluster.put(YearCluster.between2000and2005.getLabel(), new ArrayList<>());
		databaseCluster.put(YearCluster.after2005.getLabel(), new ArrayList<>());
		for (final Item item : itemDataBase.getAllItems()) {
			if (item.getYear() < 2000) {
				databaseCluster.get(YearCluster.before2000.getLabel()).add(item);
			} else if (item.getYear() < 2005) {
				databaseCluster.get(YearCluster.between2000and2005.getLabel()).add(item);
			} else {
				databaseCluster.get(YearCluster.after2005.getLabel()).add(item);
			}
		}
		Assertions.assertEquals(databaseCluster.size(), result.getClusters().size());
		for (final Entry<FacetValue, DtList<Item>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH);
			final int searchFacetCount = entry.getValue().size();
			Assertions.assertEquals(1, searchFacetCount); //result == listState.top (=1)
			for (final Item item : entry.getValue()) {
				if (item.getYear() < 2000) {
					Assertions.assertEquals(searchFacetLabel, YearCluster.before2000.getLabel());
				} else if (item.getYear() < 2005) {
					Assertions.assertEquals(searchFacetLabel, YearCluster.between2000and2005.getLabel());
				} else {
					Assertions.assertEquals(searchFacetLabel, YearCluster.after2005.getLabel());
				}
			}
		}
	}

	/**
	 * Test le facettage par geo range d'une liste.
	 */
	@Test
	public void testClusterByFacetGeoRangeDistance() {
		index(true);
		final GeoPoint origin = new GeoPoint(48.80f, 2.36f);
		final Item criteria = new Item();
		criteria.setLocalisation(origin);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacetGeo")
				.withCriteria(criteria)
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.withFacetClustering(geoFacetDefinition)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);

		//On vérifie qu'il existe une valeur pour chaque range et que le nombre d'occurrences est correct
		for (final Entry<FacetValue, DtList<Item>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH);
			final int searchFacetCount = entry.getValue().size();

			if ("< 5km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 5000), searchFacetCount);
			} else if ("< 7km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 7000), searchFacetCount);
			} else if ("< 8.5km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 8500), searchFacetCount);
			} else if ("< 10km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 10000), searchFacetCount);
			} else if ("< 20km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 20000), searchFacetCount);
			} else {
				Assertions.fail("Unexpected facet " + searchFacetLabel);
			}
			for (final Item item : entry.getValue()) {
				if ("< 5km".equals(searchFacetLabel)) {
					Assertions.assertTrue(ItemDataBase.distance(origin, item.getLocalisation()) < 5000);
				} else if ("< 7km".equals(searchFacetLabel)) {
					Assertions.assertTrue(ItemDataBase.distance(origin, item.getLocalisation()) < 7000);
				} else if ("< 8.5km".equals(searchFacetLabel)) {
					Assertions.assertTrue(ItemDataBase.distance(origin, item.getLocalisation()) < 8500);
				} else if ("< 10km".equals(searchFacetLabel)) {
					Assertions.assertTrue(ItemDataBase.distance(origin, item.getLocalisation()) < 10000);
				} else if ("< 20km".equals(searchFacetLabel)) {
					Assertions.assertTrue(ItemDataBase.distance(origin, item.getLocalisation()) < 20000);
				} else {
					Assertions.fail("Unexpected facet " + searchFacetLabel);
				}
			}
		}
	}

	/**
	 * Test le facettage par geo range d'une liste.
	 */
	@Test
	public void testClusterByFacetGeoRangeCircle() {
		index(true);
		final GeoPoint origin = new GeoPoint(48.80f, 2.36f);
		final Item criteria = new Item();
		criteria.setLocalisation(origin);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacetGeo")
				.withCriteria(criteria)
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.withFacetClustering(geoCircleFacetDefinition)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);

		//On vérifie qu'il existe une valeur pour chaque range et que le nombre d'occurrences est correct
		for (final Entry<FacetValue, DtList<Item>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH);
			final int searchFacetCount = entry.getValue().size();

			if ("< 5km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 5000), searchFacetCount);
			} else if ("5 à 7km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 7000), searchFacetCount);
			} else if ("7 à 8.5km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 8500), searchFacetCount);
			} else if ("8.5 à 10km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 10000), searchFacetCount);
			} else if ("10 à 20km".equals(searchFacetLabel)) {
				Assertions.assertEquals(ItemDataBase.near(entry.getValue(), origin, 20000), searchFacetCount);
			} else if ("> 20km".equals(searchFacetLabel)) {
				//nothing
			} else {
				Assertions.fail("Unexpected facet " + searchFacetLabel);
			}
			for (final Item item : entry.getValue()) {
				if (ItemDataBase.distance(origin, item.getLocalisation()) <= 5000) {
					Assertions.assertEquals("< 5km", searchFacetLabel);
				} else if (ItemDataBase.distance(origin, item.getLocalisation()) <= 7000) {
					Assertions.assertEquals("5 à 7km", searchFacetLabel);
				} else if (ItemDataBase.distance(origin, item.getLocalisation()) <= 8500) {
					Assertions.assertEquals("7 à 8.5km", searchFacetLabel);
				} else if (ItemDataBase.distance(origin, item.getLocalisation()) <= 10000) {
					Assertions.assertEquals("8.5 à 10km", searchFacetLabel);
				} else if (ItemDataBase.distance(origin, item.getLocalisation()) <= 20000) {
					Assertions.assertEquals("10 à 20km", searchFacetLabel);
				} else if (ItemDataBase.distance(origin, item.getLocalisation()) > 20000) {
					Assertions.assertEquals("> 20km", searchFacetLabel);
				} else {
					Assertions.fail("Unexpected facet " + searchFacetLabel);
				}
			}
		}
	}

	/**
	 * Test le facettage par geo range d'une liste.
	 */
	@Test
	public void testClusterByFacetGeoHash() {
		index(true);
		final GeoPoint origin = new GeoPoint(48.80f, 2.36f);
		final Item criteria = new Item();
		criteria.setLocalisation(origin);
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacetGeo")
				.withCriteria(criteria)
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.withFacetClustering(geoHashClusterFacetDefinition)
				.build();
		final FacetedQueryResult<Item, SearchQuery> result = searchManager.loadList(itemIndexDefinition, searchQuery, null);

		//On vérifie qu'il existe une valeur pour chaque range et que le nombre d'occurrences est correct
		for (final Entry<FacetValue, DtList<Item>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase(Locale.FRENCH);
			final int searchFacetCount = entry.getValue().size();

			if ("u09t6".equals(searchFacetLabel)) {
				Assertions.assertEquals(3, searchFacetCount);
			} else if ("u09te".equals(searchFacetLabel)) {
				Assertions.assertEquals(2, searchFacetCount);
			} else if ("u09tt".equals(searchFacetLabel)) {
				Assertions.assertEquals(1, searchFacetCount);
			} else if ("u09td".equals(searchFacetLabel)) {
				Assertions.assertEquals(1, searchFacetCount);
			} else if ("u09t7".equals(searchFacetLabel)) {
				Assertions.assertEquals(1, searchFacetCount);
			} else {
				Assertions.fail("Unexpected facet " + searchFacetLabel);
			}
		}
	}

	private void logResult(final FacetedQueryResult<Item, SearchQuery> result) {
		log.info("====== " + result.getCount() + " Results");
		for (final Facet facet : result.getFacets()) {
			log.info("\tFacet " + facet.getDefinition().getLabel().getDisplay());
			for (final Entry<FacetValue, Long> facetValue : facet.getFacetValues().entrySet()) {
				log.info("\t\t+ " + facetValue.getKey().getLabel().getDisplay() + " : " + facetValue.getValue());
			}
		}
	}

	private FacetedQueryResult<Item, SearchQuery> facetQuery(final String query) {
		final SearchQuery searchQuery = SearchQuery.builder("QryItemFacet")
				.withCriteria("")
				.withFacet(EMPTY_SELECTED_FACET_VALUES)
				.build();
		return searchManager.loadList(itemIndexDefinition, searchQuery, null);
	}

	private void index(final boolean all) {
		doIndex(all);
		waitAndExpectIndexation(itemDataBase.size());
	}

	private void removeAll() {
		doRemove("*:*");
		waitAndExpectIndexation(0);
	}

	private void doIndex(final boolean all) {
		if (all) {
			final List<SearchIndex<Item, Item>> indexes = itemDataBase.getAllItems()
					.stream()
					.map(item -> SearchIndex.createIndex(itemIndexDefinition, item.getUID(), item))
					.collect(Collectors.toList());
			searchManager.putAll(itemIndexDefinition, indexes);
		} else {
			//Indexation unitaire
			//Indexation des items de la base
			itemDataBase.getAllItems().forEach(
					item -> {
						final SearchIndex<Item, Item> index = SearchIndex.createIndex(itemIndexDefinition, item.getUID(), item);
						searchManager.put(itemIndexDefinition, index);
					});
		}
	}

	private void doRemove(final int count) {
		//Suppression de n voitures
		final List<Long> ids = itemDataBase.getAllIds();
		for (int i = 0; i < count; i++) {
			searchManager.remove(itemIndexDefinition, createURI(ids.get(i)));
		}
	}

	private void doRemove(final String query) {
		final ListFilter removeQuery = ListFilter.of(query);
		searchManager.removeAll(itemIndexDefinition, removeQuery);
	}

	private long query(final String query) {
		//recherche
		final SearchQuery searchQuery = SearchQuery.builder(query, DslListFilterBuilder.class)
				.withCriteria("")
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private long queryWithCriteria(final String query, final String criteria) {
		//recherche
		final SearchQuery searchQuery = SearchQuery.builder(query, DslListFilterBuilder.class)
				.withCriteria(criteria)
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private long queryWithSecurityFilter(final String query, final String securityFilter) {
		//recherche
		final SearchQuery searchQuery = SearchQuery.builder(query, DslListFilterBuilder.class)
				.withSecurityFilter(ListFilter.of(securityFilter))
				.withCriteria("")
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private Item doQueryAllAndGetFirst(final String sortField, final boolean sortDesc) {
		//recherche
		final SearchQuery searchQuery = SearchQuery
				.builder("QryItemFacet")
				.withCriteria("")
				.build();
		final DtListState listState = DtListState.of(null, 0, itemIndexDefinition.getIndexDtDefinition().getField(sortField).getName(), sortDesc);
		final DtList<Item> dtList = doQuery(searchQuery, listState).getDtList();
		Assertions.assertFalse(dtList.isEmpty(), "Result list was empty");
		return dtList.get(0);
	}

	private FacetedQueryResult<Item, SearchQuery> doQueryAll(final DtListState listState) {
		final SearchQuery searchQuery = SearchQuery
				.builder("QryItemFacet")
				.withCriteria("")
				.build();
		return searchManager.loadList(itemIndexDefinition, searchQuery, listState);
	}

	private FacetedQueryResult<Item, SearchQuery> doQuery(final SearchQuery searchQuery, final DtListState listState) {
		return searchManager.loadList(itemIndexDefinition, searchQuery, listState);
	}

	private static UID<Item> createURI(final long id) {
		return UID.of(Item.class, id);
	}

	private void waitAndExpectIndexation(final long expectedCount) {
		waitAndExpectIndexation(expectedCount, "*:*");
	}

	private void waitAndExpectIndexation(final long expectedCount, final String queryStr) {
		final long time = System.currentTimeMillis();
		long size = -1;
		try {
			do {
				Thread.sleep(250); //wait index was done

				size = query(queryStr);
				if (size == expectedCount) {
					break; //si le nombre est atteint on sort.
				}

			} while (System.currentTimeMillis() - time < 5000);//timeout 5s
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt(); //si interrupt on relance
		}
		Assertions.assertEquals(expectedCount, size);
	}
}
