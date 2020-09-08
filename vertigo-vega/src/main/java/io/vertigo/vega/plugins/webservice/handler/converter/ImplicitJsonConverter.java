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

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.core.lang.Assertion;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;
import io.vertigo.vega.webservice.definitions.WebServiceParam;
import io.vertigo.vega.webservice.definitions.WebServiceParam.ImplicitParam;
import io.vertigo.vega.webservice.validation.UiMessageStack;

public final class ImplicitJsonConverter implements JsonConverter, JsonSerializer {

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return UiMessageStack.class.isAssignableFrom(paramClass)
				|| HttpServletRequest.class.isAssignableFrom(paramClass)
				|| HttpServletResponse.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public void populateWebServiceCallContext(final Object input, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		Assertion.check().isTrue(getSupportedInputs()[0].isInstance(input), "This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final Object value = readImplicitValue((HttpServletRequest) input, webServiceParam, routeContext);
		routeContext.setParamValue(webServiceParam, value);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { HttpServletRequest.class };
	}

	private static Object readImplicitValue(final HttpServletRequest request, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		switch (ImplicitParam.valueOf(webServiceParam.getName())) {
			case UiMessageStack:
				return routeContext.getUiMessageStack();
			case Request:
				return request;
			case Response:
				return routeContext.getResponse();
			default:
				throw new IllegalArgumentException("ImplicitParam : " + webServiceParam.getName());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toJson(final Object result, final HttpServletResponse response, final WebServiceDefinition webServiceDefinition) {
		Assertion.check()
				.isTrue(HttpServletResponse.class.isInstance(result), "This JsonConverter doesn't support this output type {0}. Only {1} is supported", result.getClass().getSimpleName(), HttpServletResponse.class.getSimpleName())
				//-----
				.isTrue(((HttpServletResponse) result).isCommitted(), "The httpResponse returned wasn't close. Ensure you have close your streams.");
		//-----
		return ""; // response already send but can't send null : javaspark understand it as : not consumed here
	}

}
