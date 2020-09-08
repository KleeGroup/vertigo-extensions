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
package io.vertigo.datamodel.criteria.predicate;

import static io.vertigo.datamodel.criteria.CriterionLimit.ofExcluded;
import static io.vertigo.datamodel.criteria.CriterionLimit.ofIncluded;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.criteria.Criteria;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.criteria.data.CriteriaTestSmartTypes;
import io.vertigo.datamodel.criteria.data.DtDefinitions;
import io.vertigo.datamodel.criteria.data.movies.Movie2;
import io.vertigo.datamodel.criteria.data.movies.Movie2DataBase;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.structure.definitions.DtFieldName;

/**
 *
 */
public final class PredicateCriteriaTest {

	private static final DtFieldName<Movie2> year = () -> "year";
	private static final DtFieldName<Movie2> title = () -> "title";
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
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", CriteriaTestSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
								.build())
						.build())
				.build();
	}

	@Test
	public final void testIsEqualTo() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984);
		assertCriteria(3, criteriaBool);
	}

	@Test
	public final void testIsEqualToNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, null);
		assertCriteria(1, criteriaBool);
	}

	@Test
	public final void testAnd() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.and(Criterions.isEqualTo(title, "1984"));
		assertCriteria(1, criteriaBool);
	}

	@Test
	public final void testOr() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.or(Criterions.isEqualTo(year, 2014));
		assertCriteria(5, criteriaBool);
	}

	@Test
	public final void testAndOr() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.and(Criterions.startsWith(title, "a"))
				.or(Criterions.isEqualTo(year, 2014));
		assertCriteria(3 - 2 + 2, criteriaBool);
	}

	@Test
	public final void testOrAnd() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.or(Criterions.isGreaterThanOrEqualTo(year, 2000))
				.and(Criterions.startsWith(title, "m"));
		assertCriteria(3 + 3 - 2, criteriaBool);
	}

	@Test
	public final void testAndOrAnd() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.and(Criterions.startsWith(title, "a"))
				.or(
						Criterions.isGreaterThan(year, 2000)
								.and(Criterions.isEqualTo(title, "mommy")));
		assertCriteria(1 + 1, criteriaBool);
	}

	@Test
	public final void testOrAndOr() {
		final Criteria<Movie2> criteriaBool = Criterions.isEqualTo(year, 1984)
				.or(Criterions.isGreaterThan(year, 2000)
						.and(Criterions.isEqualTo(year, 2014)));
		assertCriteria(3 + 2, criteriaBool);
	}

	@Test
	public final void testIsNotEqualTo() {
		final Criteria<Movie2> criteriaBool = Criterions.isNotEqualTo(year, 1984);
		assertCriteria(13 - 3, criteriaBool);
	}

	@Test
	public final void testIsNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isNull(year);
		assertCriteria(1, criteriaBool);
	}

	@Test
	public final void testIsNotNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isNotNull(year);
		assertCriteria(12, criteriaBool);
	}

	@Test
	public final void testIsGreaterThan() {
		final Criteria<Movie2> criteriaBool = Criterions.isGreaterThan(year, 2000);
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testIsGreaterThanNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isGreaterThan(year, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testIsGreaterThanOrEqualTo() {
		final Criteria<Movie2> criteriaBool = Criterions.isGreaterThanOrEqualTo(year, 2000);
		assertCriteria(3, criteriaBool);
	}

	@Test
	public final void testIsGreaterThanOrEqualToNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isGreaterThanOrEqualTo(year, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testIsLessThan() {
		final Criteria<Movie2> criteriaBool = Criterions.isLessThan(year, 2000);
		assertCriteria(9, criteriaBool);
	}

	@Test
	public final void testIsLessThanNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isLessThan(year, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testIsLessThanOrEqualTo() {
		final Criteria<Movie2> criteriaBool = Criterions.isLessThanOrEqualTo(year, 2000);
		assertCriteria(10, criteriaBool);
	}

	@Test
	public final void testIsLessThanOrEqualToNull() {
		final Criteria<Movie2> criteriaBool = Criterions.isLessThanOrEqualTo(year, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testIsBetween() {
		final Criteria<Movie2> criteriaBool = Criterions.isBetween(year, ofIncluded(1980), ofExcluded(2000));
		assertCriteria(5, criteriaBool);
	}

	@Test
	public final void testIsBetweenWithNull1() {
		final Criteria<Movie2> criteriaBool = Criterions.isBetween(year, ofIncluded(null), ofExcluded(2000));
		assertCriteria(9, criteriaBool);
	}

	@Test
	public final void testIsBetweenWithNull2() {
		final Criteria<Movie2> criteriaBool = Criterions.isBetween(year, ofIncluded(1980), ofExcluded(null));
		assertCriteria(8, criteriaBool);
	}

	@Test
	public final void testStartsWith() {
		final Criteria<Movie2> criteriaBool = Criterions.startsWith(title, "a");
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testStartsWithNull() {
		final Criteria<Movie2> criteriaBool = Criterions.startsWith(title, null);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testInNumber() {
		final Criteria<Movie2> criteriaBool = Criterions.in(year, 1984, 1933);
		assertCriteria(3 + 1, criteriaBool);
	}

	@Test
	public final void testInNumberEmpty() {
		final Criteria<Movie2> criteriaBool = Criterions.in(year);
		assertCriteria(0, criteriaBool);
	}

	@Test
	public final void testInString() {
		final Criteria<Movie2> criteriaBool = Criterions.in(title, "terminator", "amadeus");
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testInStringEmpty() {
		final Criteria<Movie2> criteriaBool = Criterions.in(title);
		assertCriteria(0, criteriaBool);

		final Criteria<Movie2> criteriaBool2 = Criterions.in(title, "terminator", "amadeus").and(Criterions.in(title, (Serializable[]) new String[0]));
		assertCriteria(0, criteriaBool2);
	}

	private final Movie2DataBase movie2DataBase = new Movie2DataBase();

	private static Predicate<Movie2> predicate(final Criteria<Movie2> criteria) {
		return criteria.toPredicate();
	}

	public void assertCriteria(final long expected, final Criteria<Movie2> criteria) {
		final long count = movie2DataBase.getAllMovies()
				.stream()
				.filter(predicate(criteria))
				.count();
		assertEquals(expected, count);
	}

	@Test
	public void test() {
		final long count = movie2DataBase.getAllMovies()
				.stream()
				.filter(movie -> true)
				.count();
		assertEquals(13, count);
	}
}
