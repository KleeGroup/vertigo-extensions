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

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import io.vertigo.core.lang.Assertion;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.definitions.WebServiceParam;
import io.vertigo.vega.webservice.definitions.WebServiceParam.WebServiceParamType;

public final class PathJsonReader implements JsonReader<String> {

	/** {@inheritDoc} */
	@Override
	public WebServiceParamType[] getSupportedInput() {
		return new WebServiceParamType[] { WebServiceParamType.Path };
	}

	/** {@inheritDoc} */
	@Override
	public Class<String> getSupportedOutput() {
		return String.class;
	}

	/** {@inheritDoc} */
	@Override
	public String extractData(final HttpServletRequest request, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		Assertion.check().isTrue(
				getSupportedInput()[0].equals(webServiceParam.getParamType()),
				"This JsonReader can't read the asked request ParamType {0}. Only {1} is supported", webServiceParam.getParamType(), Arrays.toString(getSupportedInput()));
		//-----
		return routeContext.getPathParam(webServiceParam);
	}

}
