/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.quarto.impl.services.publisher.merger.grammar;

import io.vertigo.quarto.impl.services.publisher.merger.script.ScriptContext;
import io.vertigo.quarto.impl.services.publisher.merger.script.ScriptTag;
import io.vertigo.quarto.impl.services.publisher.merger.script.ScriptTagContent;
import io.vertigo.util.StringUtil;

/**
 * @author pchretien, npiedeloup
 */
//public car instancié dynamiquement
public final class TagBlock extends AbstractScriptTag implements ScriptTag {
	/** {@inheritDoc} */
	@Override
	public String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		return START_BLOC_JSP + decode(tag.getAttribute()) + END_BLOC_JSP;
	}

	/** {@inheritDoc} */
	@Override
	public String renderClose(final ScriptTagContent tag, final ScriptContext context) {
		return START_BLOC_JSP + decode(tag.getAttribute()) + END_BLOC_JSP;
	}

	private static String decode(final String s) {
		//On décode les caractères qui peuvent avoir du sens dans un block
		final StringBuilder decode = new StringBuilder(s);
		StringUtil.replace(decode, "&quot;", "\"");
		StringUtil.replace(decode, "&lt;", "<");
		StringUtil.replace(decode, "&gt;", ">");
		return decode.toString();
	}
}
