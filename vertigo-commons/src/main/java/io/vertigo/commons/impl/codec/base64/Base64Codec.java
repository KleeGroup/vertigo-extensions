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
package io.vertigo.commons.impl.codec.base64;

import java.util.Base64;

import io.vertigo.commons.codec.Codec;
import io.vertigo.core.lang.Assertion;

/**
 * Implémentation threadSafe des mécanismes standards d'encodage/décodage.
 * Base 64 modifié pour passer dans les urls ('+', '/' remplacer par '-', '_' )
 * Les codes sont gérés par quatre octets.
 * {voir wikipedia http://en.wikipedia.org/wiki/Base64#Privacy-Enhanced_Mail_.28PEM.29}
 * @author  npiedeloup
 */
public final class Base64Codec implements Codec<byte[], String> {

	/** {@inheritDoc} */
	@Override
	public byte[] decode(final String coded) {
		Assertion.check().isNotNull(coded);
		//-----
		return Base64.getUrlDecoder().decode(coded);
	}

	/** {@inheritDoc} */
	@Override
	public String encode(final byte[] raw) {
		Assertion.check().isNotNull(raw);
		//-----
		return Base64.getUrlEncoder().encodeToString(raw);
	}
}
