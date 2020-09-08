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
package io.vertigo.ui.data.services.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import io.vertigo.commons.impl.codec.base64.Base64Codec;

/**
 * Classe utilitaire offrant un ensemble de services concernant les DtObject.
 *
 * @author npiedeloup
 * @version $Id: PasswordUtil.java,v 1.1 2014/08/05 09:14:42 npiedeloup Exp $
 */
public final class PasswordHelper {
	//private static final String SALT_USER_TYPED_CHAR = "esarintulomdpcfbvhgjqzyxwk0123456789,;:!?.ESARINTULOMDPCFBVHGJQZYXWK+&\"'(-_)=~#{[|\\^@]}^$*%�/�><*�?";

	private final Charset defaultCharsetUTF8;
	private final MessageDigest sha256Digest;
	private final Base64Codec base64Codec;
	private final SecureRandom rnd;

	/**
	 * Contructeur
	 */
	public PasswordHelper() {
		try {
			sha256Digest = MessageDigest.getInstance("SHA-256");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		defaultCharsetUTF8 = Charset.forName("UTF-8");
		base64Codec = new Base64Codec();
		rnd = new SecureRandom();
	}

	public String generateNewSalt() {
		final byte[] byteSalt = rnd.generateSeed(6);
		return encodeBase64(byteSalt);
	}

	public String extractSalt(final String password) {
		return password.substring(0, 8); //must be  ceil(saltSizeInBytes / 3) * 4
	}

	public String encodePassword(final String salt, final String password) {
		return salt + encodeBase64(sha256(salt + password));
	}

	/**
	 * Encode un tableau d'octets en base 64.
	 * @param data La donnee.
	 * @return La valeur encodee.
	 */
	private String encodeBase64(final byte[] data) {
		return base64Codec.encode(data);
	}

	/**
	 * Calcule le hash SHA-256 d'une chaine de caractere.
	 * @param string Chaine de caractere.
	 * @return Tableau d'octets.
	 */
	private byte[] sha256(final String string) {
		return sha256Digest.digest(string.getBytes(defaultCharsetUTF8));
	}

}
