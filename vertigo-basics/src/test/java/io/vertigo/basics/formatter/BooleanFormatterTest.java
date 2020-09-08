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
import io.vertigo.datamodel.structure.definitions.FormatterException;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public final class BooleanFormatterTest {
	private final Formatter formatterBoolean = new FormatterBoolean("YES;NO");

	/**
	 * Test du formatter de booléen.
	 * @throws FormatterException
	*/
	@Test
	public void testFormatter() throws FormatterException {
		Assertions.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("YES", BasicType.Boolean));
		Assertions.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("YES ", BasicType.Boolean));
		Assertions.assertEquals(Boolean.FALSE, formatterBoolean.stringToValue("NO", BasicType.Boolean));
		Assertions.assertEquals(Boolean.FALSE, formatterBoolean.stringToValue("NO ", BasicType.Boolean));
		Assertions.assertEquals(null, formatterBoolean.stringToValue(null, BasicType.Boolean));
		Assertions.assertEquals(null, formatterBoolean.stringToValue("", BasicType.Boolean));
		Assertions.assertEquals(null, formatterBoolean.stringToValue(" ", BasicType.Boolean));

		Assertions.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue(" YES", BasicType.Boolean));
		Assertions.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("YES ", BasicType.Boolean));

		Assertions.assertEquals("YES", formatterBoolean.valueToString(Boolean.TRUE, BasicType.Boolean));
		Assertions.assertEquals("NO", formatterBoolean.valueToString(Boolean.FALSE, BasicType.Boolean));
		Assertions.assertEquals(null, formatterBoolean.valueToString(null, BasicType.Boolean));
	}

	@Test
	public void testFormatter1() {
		Assertions.assertThrows(FormatterException.class, () -> formatterBoolean.stringToValue("abc ", BasicType.Boolean));
	}

	@Test
	public void testFormatter2() {
		Assertions.assertThrows(Exception.class, () -> formatterBoolean.valueToString("", BasicType.Boolean));
	}

	@Test
	public void testFormatter3() {
		Assertions.assertThrows(Exception.class, () -> formatterBoolean.valueToString(" ", BasicType.Boolean));
	}
}
