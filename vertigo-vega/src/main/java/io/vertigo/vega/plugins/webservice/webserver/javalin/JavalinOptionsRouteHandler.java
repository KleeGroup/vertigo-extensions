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
package io.vertigo.vega.plugins.webservice.webserver.javalin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition.Verb;

/**
 * Handler of Options preflight request.
 * @author npiedeloup
 */
public final class JavalinOptionsRouteHandler implements Handler {
	private static final Logger LOGGER = LogManager.getLogger(JavalinOptionsRouteHandler.class);

	private final HandlerChain handlerChain;
	private final WebServiceDefinition webServiceCors;

	/**
	 * @param handlerChain handlerChain
	 */
	JavalinOptionsRouteHandler(final HandlerChain handlerChain) {
		this.handlerChain = handlerChain;
		//we use a fake webServiceDefinition, to ensure no webservice was called on Options request
		webServiceCors = WebServiceDefinition.builder(ClassUtil.findMethod(JavalinOptionsRouteHandler.class, "unsupported"))
				.with(Verb.Get, "/_OPTIONS_*")
				.withCorsProtected(true)
				.build();
	}

	/**
	 * Fake method for prefligth Options request.
	 */
	public void unsupported() {
		throw new UnsupportedOperationException("OPTIONS is unsupported but preflight");
	}

	/** {@inheritDoc} */
	@Override
	public void handle(final Context ctx) {
		try {
			final Object result = handlerChain.handle(ctx.req, ctx.res, new WebServiceCallContext(new JavalinWebServiceContext(ctx), webServiceCors)); //no WebService
			if (result instanceof String) {
				ctx.result((String) result);
			} else {
				//TODO usefull ?
				ctx.json(result);
			}
		} catch (final Exception e) {
			LOGGER.error("Option route error", e);
			ctx.result(e.getMessage());
		}
	}
}
