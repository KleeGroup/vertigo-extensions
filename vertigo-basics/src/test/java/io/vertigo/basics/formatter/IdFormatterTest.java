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
public class IdFormatterTest {

	private final Formatter formatterId = new FormatterId("");

	/**
	 * Test du formatter de ID.
	 * @throws FormatterException
	*/
	@Test
	public void testFormatter() throws FormatterException {
		Assertions.assertEquals(10L, formatterId.stringToValue("10", BasicType.Long));
		Assertions.assertEquals(null, formatterId.stringToValue(null, BasicType.Long));
		Assertions.assertEquals(null, formatterId.stringToValue("", BasicType.Long));
		Assertions.assertEquals(null, formatterId.stringToValue(" ", BasicType.Long));

		Assertions.assertEquals(10L, formatterId.stringToValue(" 10", BasicType.Long));
		Assertions.assertEquals(10L, formatterId.stringToValue("10 ", BasicType.Long));

		Assertions.assertEquals("10", formatterId.valueToString(10L, BasicType.Long));
		Assertions.assertEquals("", formatterId.valueToString(null, BasicType.Long));
	}

	@Test
	public void testFormatter1() {
		Assertions.assertThrows(FormatterException.class, () -> formatterId.stringToValue("abc ", BasicType.Long));
	}

}
