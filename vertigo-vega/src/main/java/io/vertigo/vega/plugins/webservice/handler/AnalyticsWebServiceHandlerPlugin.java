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
package io.vertigo.vega.plugins.webservice.handler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;
import io.vertigo.vega.webservice.exception.SessionException;

/**
 * Analytics handler.
 * @author npiedeloup
 */
public final class AnalyticsWebServiceHandlerPlugin implements WebServiceHandlerPlugin {

	/** Stack index of the handler for sorting at startup**/
	public static final int STACK_INDEX = 30;

	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 * @param analyticsManager Analytics Manager
	 */
	@Inject
	public AnalyticsWebServiceHandlerPlugin(final AnalyticsManager analyticsManager) {
		Assertion.check().isNotNull(analyticsManager);
		//-----
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final HttpServletRequest request, final HttpServletResponse response, final WebServiceCallContext webServiceCallContext, final HandlerChain chain) throws SessionException {
		final WebServiceDefinition webServiceDefinition = webServiceCallContext.getWebServiceDefinition();
		//On ne prend pas request.pathInfo qui peut contenir des paramètres : on en veut pas ca dans les stats
		final String name = "/" + webServiceDefinition.getVerb().name() + "/" + webServiceDefinition.getPath();
		return analyticsManager.traceWithReturn(
				"webservices",
				name,
				tracer -> {
					try {
						return chain.handle(request, response, webServiceCallContext);
					} catch (final SessionException e) {
						throw WrappedException.wrap(e);
					}
				});
	}

	@Override
	public int getStackIndex() {
		return STACK_INDEX;
	}
}
