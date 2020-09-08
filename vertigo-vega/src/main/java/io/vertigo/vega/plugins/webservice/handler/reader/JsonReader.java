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

import javax.servlet.http.HttpServletRequest;

import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.definitions.WebServiceParam;
import io.vertigo.vega.webservice.definitions.WebServiceParam.WebServiceParamType;

/**
 * Read request to extract a not converted parameter.
 * @author npiedeloup
 * @param <O> Output type
 */
public interface JsonReader<O> {

	/**
	 * @return Supported type of parameter in request
	 */
	WebServiceParamType[] getSupportedInput();

	/**
	 * @return Output classe supported
	 */
	Class<O> getSupportedOutput();

	/**
	 * Extract parameter value from request as readType.
	 * This doesn't convert it to value object, it's only extraction, the converter do the convert task.
	 * @param request Request
	 * @param webServiceParam Param infos
	 * @param routeContext routeContext
	 * @return output value
	 */
	O extractData(HttpServletRequest request, WebServiceParam webServiceParam, WebServiceCallContext routeContext);

}
