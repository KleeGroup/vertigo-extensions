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
package io.vertigo.audit.trace;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;

/**
 * Junit for audit manager
 * @author xdurand
 *
 */
public class TraceManagerTest {
	@Inject
	private TraceManager auditManager;

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
		return MyNodeConfig.config();
	}

	/**
	 * Add/Get Test for audit trace
	 */
	@Test
	public void testAddAuditTrace() {
		final Trace auditTrace = new TraceBuilder("CAT1", "USER1", 1L, "My message 1").build();

		auditManager.addTrace(auditTrace);
		final Trace auditFetch = auditManager.getTrace(auditTrace.getId());

		assertThat(auditFetch).isEqualToIgnoringGivenFields(auditTrace, "id");
	}

	/**
	 * Find Test for audit trace
	 */
	@Test
	public void testFindAuditTrace() {
		final Trace auditTrace1 = new TraceBuilder("CAT2", "USER2", 2L, "My message 2").build();
		auditManager.addTrace(auditTrace1);

		final Trace auditTrace2 = new TraceBuilder("CAT3", "USER3", 3L, "My message 3")
				.withDateBusiness(Instant.now())
				.withContext(List.of("Context 3"))
				.build();

		auditManager.addTrace(auditTrace2);

		//Criteria Category
		final TraceCriteria atc1 = TraceCriteria.builder().withCategory("CAT2").build();
		final List<Trace> auditTraceFetch1 = auditManager.findTrace(atc1);

		assertThat(auditTraceFetch1).hasSize(1);
		assertThat(auditTraceFetch1).usingFieldByFieldElementComparator().contains(auditTrace1);

		final Instant dateJMinus1 = Instant.now().minus(1, ChronoUnit.DAYS);
		final Instant dateJPlus1 = Instant.now().plus(1, ChronoUnit.DAYS);

		//Criteria Business Date
		final TraceCriteria auditTraceCriteria2 = TraceCriteria.builder()
				.withDateBusinessStart(dateJMinus1)
				.withDateBusinessEnd(dateJPlus1)
				.build();

		final List<Trace> auditTraceFetch2 = auditManager.findTrace(auditTraceCriteria2);

		assertThat(auditTraceFetch2).hasSize(1);
		assertThat(auditTraceFetch2).usingFieldByFieldElementComparator().contains(auditTrace2);

		//Criteria Exec Date
		final TraceCriteria auditTraceCriteria3 = TraceCriteria.builder()
				.withDateExecutionStart(dateJMinus1)
				.withDateExecutionEnd(dateJPlus1)
				.build();
		final List<Trace> auditTraceFetch3 = auditManager.findTrace(auditTraceCriteria3);

		assertThat(auditTraceFetch3).hasSize(2);
		assertThat(auditTraceFetch3).usingFieldByFieldElementComparator().contains(auditTrace1, auditTrace2);

		//Criteria Item
		final TraceCriteria auditTraceCriteria4 = TraceCriteria.builder().withItem(2L).build();
		final List<Trace> auditTraceFetch4 = auditManager.findTrace(auditTraceCriteria4);

		assertThat(auditTraceFetch4).hasSize(1);
		assertThat(auditTraceFetch4).usingFieldByFieldElementComparator().contains(auditTrace1);

		//Criteria User
		final TraceCriteria auditTraceCriteria5 = TraceCriteria.builder().withUsername("USER3").build();
		final List<Trace> auditTraceFetch5 = auditManager.findTrace(auditTraceCriteria5);

		assertThat(auditTraceFetch5).hasSize(1);
		assertThat(auditTraceFetch5).usingFieldByFieldElementComparator().contains(auditTrace2);
	}

}
