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
package io.vertigo.vega.impl.servlet.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.core.lang.Tuple;

/**
 * The Vertigo Security Filter {@link io.vertigo.vega.impl.servlet.filter.SecurityFilter} can delegate the authentication workflow to a custom compoment.
 * This component must implement this interface.
 *
 * This delegate has the hability to execute function before/after the filter chain and has a callback for a finally block.
 * It is executed after the creation of the UserSession (in Vertigo's terms) so you're in the right place to populate the fined grained security context for the connected user.
 * @author mlaroche
 *
 */
public interface DelegateAuthenticationFilterHandler {

	/**
	 * Code to execute before the call to the filter chain.
	 * @param request the httpRequest
	 * @param response the httpResponse
	 * @return a tuple composed of two values :
	 *   - a boolean to indicate wether the request has been handled in the method and chain must be stopped
	 *   - the HttpRequest that will be transfered in the filter chain. (for example if you need to wrap it for any reason  )
	 */
	Tuple<Boolean, HttpServletRequest> doBeforeChain(HttpServletRequest request, HttpServletResponse response);

	/**
	 * Code to execute before the after to the filter chain.
	 * @param request the httpRequest
	 * @param response the httpResponse
	 */
	void doAfterChain(HttpServletRequest request, HttpServletResponse response);

	/**
	 * Code to execute in a finally block that wraps all the calls to this delegate and the filter chain
	 * @param request the httpRequest
	 * @param response the httpResponse
	 */
	void doFinally(HttpServletRequest request, HttpServletResponse response);

}
