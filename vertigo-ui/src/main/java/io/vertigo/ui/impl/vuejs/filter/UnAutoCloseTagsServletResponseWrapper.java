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
package io.vertigo.ui.impl.vuejs.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.vega.impl.servlet.filter.AbstractHttpServletResponseWrapper;

/**
 * Impl of HttpServletResponseWrapper to intercept and return content as String.
 * @author npiedeloup
 */
class UnAutoCloseTagsServletResponseWrapper extends AbstractHttpServletResponseWrapper {

	/**
	 * Constructeur qui crée un adapteur de ServletResponse wrappant la response spécifiée.
	 * @param response javax.servlet.HttpServletResponse
	 */
	UnAutoCloseTagsServletResponseWrapper(final HttpServletResponse response) {
		super(response);
	}

	/**
	 * Crée et retourne un ServletOutputStream pour écrire le contenu dans la response associée.
	 * @return javax.servlet.ServletOutputStream
	 */
	@Override
	public ServletOutputStream createOutputStream() {
		return new UnAutoCloseTagsResponseStream();
	}

	/**
	 * Get the contents of the outputStream.
	 * @return contents of the outputStream
	 */
	public String getAsString() {
		return getStream() != null ? getStream().toString() : ""; //if stream is null the outputstream wasn't open at all : no result we return "" instead of null which will print "null"
	}

	/**
	 * Ne fait rien
	 * @param length int
	 */
	@Override
	public void setContentLength(final int length) {
		// ne fait rien
	}
}
