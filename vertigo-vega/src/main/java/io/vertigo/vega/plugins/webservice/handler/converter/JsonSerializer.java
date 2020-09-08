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

import io.vertigo.vega.webservice.definitions.WebServiceDefinition;

/**
 * Converter result object into json.
 * @author npiedeloup
 */
public interface JsonSerializer {

	/**
	 * @param paramClass Class to test
	 * @return If this converter can output this type of data.
	 */
	boolean canHandle(Class<?> paramClass);

	/**
	 * Convert result to json.
	 * @param result Result
	 * @param response Response
	 * @param webServiceDefinition WebService definition
	 * @return Json (or empty string)
	 */
	String toJson(Object result, HttpServletResponse response, WebServiceDefinition webServiceDefinition);

}
