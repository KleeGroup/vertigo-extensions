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
package io.vertigo.vega.plugins.webservice.servlet;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.servlet.ServletContext;

import io.vertigo.core.impl.resource.ResourceResolverPlugin;
import io.vertigo.core.lang.Assertion;

/**
 * Résolution des URL liées à la servlet.
 * @author prahmoune
 */
public final class ServletResourceResolverPlugin implements ResourceResolverPlugin {
	private static WeakReference<ServletContext> servletContextRef;
	private final ServletContext servletContext;

	/**
	 * @param servletContext ServletContext
	 */
	public static synchronized void setServletContext(final ServletContext servletContext) {
		Assertion.check().isNotNull(servletContext);
		//-----
		servletContextRef = new WeakReference<>(servletContext);
	}

	/**
	 * Constructor.
	 */
	public ServletResourceResolverPlugin() {
		Assertion.check().isNotNull(servletContextRef.get(), "Ce servletContext n'est plus accessible");
		//-----
		servletContext = servletContextRef.get();
	}

	/** {@inheritDoc} */
	@Override
	public Optional<URL> resolve(final String resource) {
		Assertion.check().isNotNull(resource);
		//-----
		// 2. On recherche dans le context de la webapp
		try {
			return Optional.ofNullable(servletContext.getResource(resource));
		} catch (final MalformedURLException e) {
			return Optional.empty();
		}
	}
}
