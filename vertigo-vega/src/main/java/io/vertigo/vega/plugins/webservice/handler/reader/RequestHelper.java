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
package io.vertigo.vega.plugins.webservice.handler.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public final class RequestHelper {
	private static final String DEFAULT_CONTENT_CHARSET = "UTF-8";
	private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");
	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RequestHelper.class);

	private final HttpServletRequest innerRequest;
	private String body;

	public RequestHelper(final HttpServletRequest request) {
		innerRequest = request;
	}

	public String body() {
		if (body == null) {
			try {
				body = inputStreamToString(innerRequest.getInputStream(), getContentCharset());
			} catch (final Exception e) {
				LOGGER.warn("Exception when reading body", e);
			}
		}
		return body;
	}

	private static String inputStreamToString(final InputStream input, final String contentCharset) throws IOException {
		final StringWriter sw = new StringWriter();
		copy(new InputStreamReader(input, contentCharset), sw); //set charset on InputStreamReader
		return sw.toString();
	}

	private String getContentCharset() {
		final Matcher m = CHARSET_PATTERN.matcher(innerRequest.getContentType());
		if (m.find()) {
			return m.group(1).trim().toUpperCase(Locale.ENGLISH);
		}
		return DEFAULT_CONTENT_CHARSET;
	}

	/**
	 * Copie le contenu d'un flux d'entrée vers un flux de sortie.
	 * @param in flux d'entrée
	 * @param out flux de sortie
	 * @throws IOException Erreur d'entrée/sortie
	 */
	private static void copy(final Reader in, final Writer out) throws IOException {
		final int bufferSize = 10 * 1024;
		final char[] bytes = new char[bufferSize];
		int read = in.read(bytes);
		while (read != -1) {
			out.write(bytes, 0, read);
			read = in.read(bytes);
		}
	}

}
