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

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.lang.BasicType;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.datamodel.structure.definitions.Formatter;
import io.vertigo.datamodel.structure.definitions.FormatterException;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class NumberFormatterTest {
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

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumber() throws FormatterException {
		final Formatter formatterNumber = new FormatterNumber("#,###,##0.00");

		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assertions.assertEquals(pi, formatterNumber.stringToValue("3.14", BasicType.BigDecimal));
		Assertions.assertEquals(pi, formatterNumber.stringToValue("3,14", BasicType.BigDecimal));
		Assertions.assertEquals("3,14", formatterNumber.valueToString(pi, BasicType.BigDecimal));
		Assertions.assertEquals(new BigDecimal("0.14"), formatterNumber.stringToValue("0.14", BasicType.BigDecimal));
		//Integer
		Assertions.assertEquals(1492, formatterNumber.stringToValue("1492", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumber.stringToValue("1 492", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumber.stringToValue("1492  ", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumber.stringToValue("01492  ", BasicType.Integer));
		//Long
		Assertions.assertEquals(1492L, formatterNumber.stringToValue("1492", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumber.stringToValue("1 492", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumber.stringToValue("1492  ", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumber.stringToValue("01492  ", BasicType.Long));
		//Double
		Assertions.assertEquals(3.14D, formatterNumber.stringToValue("3.14", BasicType.Double));
		Assertions.assertEquals(3.14D, formatterNumber.stringToValue("3,14", BasicType.Double));
		Assertions.assertEquals(.14D, formatterNumber.stringToValue("0.14", BasicType.Double));
		Assertions.assertEquals("3,14", formatterNumber.valueToString(3.14D, BasicType.Double));
	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumberMLWithDecimal() throws FormatterException {
		//séparateur décimal , et accepte .
		//séparateur milliers '\u00A0' => espace insécable + espace (implicite)
		final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("#,##0.00|,.|\u00A0 ");

		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assertions.assertEquals(pi, formatterNumberLocalized.stringToValue("3.14", BasicType.BigDecimal));
		Assertions.assertEquals(pi, formatterNumberLocalized.stringToValue("3,14", BasicType.BigDecimal));
		Assertions.assertEquals(new BigDecimal("0.14"), formatterNumberLocalized.stringToValue("0.14", BasicType.BigDecimal));

		Assertions.assertEquals("3,14", formatterNumberLocalized.valueToString(pi, BasicType.BigDecimal));
		Assertions.assertEquals("1" + (char) 160 + "495,00", formatterNumberLocalized.valueToString(1495, BasicType.BigDecimal));
		Assertions.assertEquals("1\u00A0495,00", formatterNumberLocalized.valueToString(1495, BasicType.BigDecimal));
		Assertions.assertEquals("1\u00A0495,52", formatterNumberLocalized.valueToString(1495.52, BasicType.BigDecimal));

		//Integer
		Assertions.assertEquals(1492, formatterNumberLocalized.stringToValue("1492", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumberLocalized.stringToValue("1 492", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumberLocalized.stringToValue("1492  ", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumberLocalized.stringToValue("01492  ", BasicType.Integer));
		Assertions.assertEquals("1\u00A0492,00", formatterNumberLocalized.valueToString(1492, BasicType.Integer));

		//Long
		Assertions.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumberLocalized.stringToValue("1 492", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492  ", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumberLocalized.stringToValue("01492  ", BasicType.Long));
		Assertions.assertEquals("1\u00A0492,00", formatterNumberLocalized.valueToString(1492L, BasicType.Long));
	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumberMLNoDecimal() throws FormatterException {
		//séparateur décimal . et accepte ,
		//séparateur milliers NO précisé => par défaut sep \u00A0
		final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("#,##0.##|.,|");

		//séparateur milliers ' '
		final FormatterNumber formatterNumberLocalizedSpace = new FormatterNumberLocalized("#,##0.##|.,| \u00A0");

		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assertions.assertEquals(pi, formatterNumberLocalized.stringToValue("3.14", BasicType.BigDecimal));
		Assertions.assertEquals(pi, formatterNumberLocalized.stringToValue("3,14", BasicType.BigDecimal));
		Assertions.assertEquals(new BigDecimal("0.14"), formatterNumberLocalized.stringToValue("0.14", BasicType.BigDecimal));

		Assertions.assertEquals("3.14", formatterNumberLocalized.valueToString(pi, BasicType.BigDecimal));
		Assertions.assertEquals("1\u00A0495", formatterNumberLocalized.valueToString(1495, BasicType.BigDecimal));
		Assertions.assertEquals("1 495.52", formatterNumberLocalizedSpace.valueToString(1495.52, BasicType.BigDecimal));

		//Integer
		Assertions.assertEquals(1492, formatterNumberLocalized.stringToValue("1492", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumberLocalized.stringToValue("1\u00A0492", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumberLocalized.stringToValue("1492  ", BasicType.Integer));
		Assertions.assertEquals(1492, formatterNumberLocalized.stringToValue("01492  ", BasicType.Integer));
		Assertions.assertEquals("1\u00A0492", formatterNumberLocalized.valueToString(1492, BasicType.Integer));

		//Long
		Assertions.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumberLocalizedSpace.stringToValue("1\u00A0492", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492  ", BasicType.Long));
		Assertions.assertEquals(1492L, formatterNumberLocalized.stringToValue("01492  ", BasicType.Long));
		Assertions.assertEquals("1 492", formatterNumberLocalizedSpace.valueToString(1492L, BasicType.Long));

	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumberMLConflit() throws FormatterException {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("#,##0.##|.,|.");
			formatterNumberLocalized.stringToValue("3.14", BasicType.BigDecimal);
			//Détection du conflit entre séparateur décimal et de millier
		});
	}
}
