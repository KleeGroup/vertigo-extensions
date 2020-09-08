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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.javalin.http.Context;
import io.vertigo.vega.plugins.webservice.handler.WebServiceContext;

final class JavalinWebServiceContext implements WebServiceContext {
	private final Context ctx;

	/**
	 * Constructor.
	 * @param ctx Javalin context
	 */
	JavalinWebServiceContext(final Context ctx) {
		this.ctx = ctx;
	}

	/** {@inheritDoc} */
	@Override
	public HttpServletRequest getRequest() {
		return ctx.req;
	}

	/** {@inheritDoc} */
	@Override
	public HttpServletResponse getResponse() {
		return ctx.res;
	}

	/** {@inheritDoc} */
	@Override
	public String getPathParam(final String pathName) {
		return ctx.pathParam(pathName);
	}

	/** {@inheritDoc} */
	@Override
	public String getBody() {
		return ctx.body();
	}
}
