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
package io.vertigo.quarto.impl.publisher.merger.grammar;

import io.vertigo.quarto.impl.publisher.merger.script.ScriptContext;
import io.vertigo.quarto.impl.publisher.merger.script.ScriptTag;
import io.vertigo.quarto.impl.publisher.merger.script.ScriptTagContent;

/**
 * @author pchretien, npiedeloup
 */
//public car instancié dynamiquement
public final class TagEncodedField extends AbstractScriptTag implements ScriptTag {
	public static final String ENCODER = "encoder";

	private static final String CALL = "=" + ENCODER + ".encode({0})";

	/** {@inheritDoc} */
	@Override
	public String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		final String[] parsing = parseAttribute(tag.getAttribute(), FIELD_PATH_CALL);
		// le tag est dans le bon format
		parsing[0] = getCallForFieldPath(parsing[0], tag.getCurrentVariable());

		return getTagRepresentation(CALL, parsing);
	}

	/** {@inheritDoc} */
	@Override
	public String renderClose(final ScriptTagContent tag, final ScriptContext context) {
		return "";
	}
}
