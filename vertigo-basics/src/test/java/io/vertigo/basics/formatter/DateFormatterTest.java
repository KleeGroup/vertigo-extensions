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
package io.vertigo.basics.formatter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.lang.BasicType;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.datamodel.structure.definitions.FormatterException;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class DateFormatterTest {
	private final FormatterDate formatterDate = new FormatterDate("yyyy-MM-dd");
	private final FormatterDate formatterDateTime = new FormatterDate("yyyy-MM-dd' 'HH:mm:ss");

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
						.withLocalesAndDefaultZoneId("fr_FR", "UTC")
						.build())
				.build();
	}

	@Test
	public void testLocalDateFormatter() throws FormatterException {
		final LocalDate localDate = LocalDate.of(2000, 12, 25);
		Assertions.assertEquals("2000-12-25", formatterDate.valueToString(localDate, BasicType.LocalDate));
		Assertions.assertEquals(localDate, formatterDate.stringToValue("2000-12-25", BasicType.LocalDate));
	}

	@Test
	public void testInstantFormatter() throws FormatterException {
		final Instant instant = LocalDateTime.of(2009, 2, 23, 16, 30).toInstant(ZoneOffset.UTC);
		Assertions.assertEquals("2009-02-23 16:30:00", formatterDateTime.valueToString(instant, BasicType.Instant));
		Assertions.assertEquals(instant, formatterDateTime.stringToValue("2009-02-23 16:30:00", BasicType.Instant));
	}

	@Test
	public void testFormatterErrorLocalDate() {
		Assertions.assertThrows(FormatterException.class, () -> {
			final LocalDate localDate = LocalDate.of(2000, 12, 25);
			Assertions.assertEquals(localDate, formatterDate.stringToValue("2003/09/15", BasicType.LocalDate));
		});
	}

	@Test
	public void testFormatterErrorInstant() {
		Assertions.assertThrows(FormatterException.class, () -> {
			final Instant instant = LocalDateTime.of(2009, 2, 23, 16, 30).toInstant(ZoneOffset.UTC);
			Assertions.assertEquals(instant, formatterDate.stringToValue("2003/09/15 16:30:00", BasicType.Instant));
		});
	}

}
