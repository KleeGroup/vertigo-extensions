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
package io.vertigo.vega.plugins.webservice.handler.converter;

import javax.servlet.http.HttpServletResponse;

import io.vertigo.core.lang.Assertion;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;

public final class StringJsonSerializer implements JsonSerializer {

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return String.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public String toJson(final Object result, final HttpServletResponse response, final WebServiceDefinition webServiceDefinition) {
		final String resultString = (String) result;
		final int length = resultString.length();
		Assertion.check().isTrue(
				!(resultString.charAt(0) == '{' && resultString.charAt(length - 1) == '}') && !(resultString.charAt(0) == '[' && resultString.charAt(length - 1) == ']'),
				"Can't return pre-build json : {0}", resultString);
		response.setContentType("text/plain;charset=UTF-8");
		return (String) result;
	}

}
