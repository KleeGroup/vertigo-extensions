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
package io.vertigo.quarto.plugins.publisher.docx;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.core.util.StringUtil;

/**
 * Implémentation de l'encodage des données dans un fichier ODT.
 *
 * @author npiedeloup
 */
public final class DOCXValueEncoder implements Encoder<String, String> {

	/** {@inheritDoc} */
	@Override
	public String encode(final String toEncode) {
		if (toEncode == null) {
			return null;
		}
		final StringBuilder result = new StringBuilder(toEncode);
		StringUtil.replace(result, "&", "&amp;");
		StringUtil.replace(result, "<", "&lt;");
		StringUtil.replace(result, ">", "&gt;");
		StringUtil.replace(result, "\t", "<w:tab/>");
		StringUtil.replace(result, "\n", "<w:br/>");
		StringUtil.replace(result, String.valueOf((char) 128), String.valueOf((char) 8364));

		//on remet les &#xxxx; non encodé
		final String strResult = result.toString();
		return strResult.replaceAll("&amp;#([0-9]{2,4});", "&#$1;");
	}

}
