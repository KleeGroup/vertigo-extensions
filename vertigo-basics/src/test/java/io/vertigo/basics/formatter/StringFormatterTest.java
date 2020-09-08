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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.lang.BasicType;
import io.vertigo.datamodel.structure.definitions.Formatter;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class StringFormatterTest {

	@Test
	public void testUpper() {
		final Formatter formatterString = new FormatterString("UPPER");
		Assertions.assertEquals("AA", formatterString.valueToString("aa", BasicType.String));
		Assertions.assertEquals("AA", formatterString.valueToString("AA", BasicType.String));
		Assertions.assertEquals("AA", formatterString.valueToString("Aa", BasicType.String));
		Assertions.assertEquals("AA", formatterString.valueToString("aA", BasicType.String));
	}

	@Test
	public void testLower() {
		final Formatter formatterString = new FormatterString("LOWER");
		Assertions.assertEquals("aa", formatterString.valueToString("aa", BasicType.String));
		Assertions.assertEquals("aa", formatterString.valueToString("AA", BasicType.String));
		Assertions.assertEquals("aa", formatterString.valueToString("Aa", BasicType.String));
		Assertions.assertEquals("aa", formatterString.valueToString("aA", BasicType.String));
	}

	@Test
	public void testUpperFirst() {
		final Formatter formatterString = new FormatterString("UPPER_FIRST");
		Assertions.assertEquals("Aa", formatterString.valueToString("aa", BasicType.String));
		Assertions.assertEquals("Aa", formatterString.valueToString("AA", BasicType.String));
		Assertions.assertEquals("Aa", formatterString.valueToString("Aa", BasicType.String));
		Assertions.assertEquals("Aa", formatterString.valueToString("aA", BasicType.String));
	}
}
