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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.javalin.Javalin;
import io.vertigo.connectors.javalin.JavalinConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;
import io.vertigo.vega.impl.webservice.WebServerPlugin;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;

/**
 * RoutesRegisterPlugin use to register Javalin route.
 * @author npiedeloup
 */
public final class JavalinWebServerPlugin implements WebServerPlugin {
	private final Optional<String> apiPrefix;
	private final JavalinConnector javalinConnector;

	@Inject
	public JavalinWebServerPlugin(
			@ParamValue("connectorName") final Optional<String> connectorNameOpt,
			@ParamValue("apiPrefix") final Optional<String> apiPrefix,
			final List<JavalinConnector> javalinConnectors) {
		Assertion.check()
				.isNotNull(connectorNameOpt)
				.isNotNull(javalinConnectors)
				.isNotNull(apiPrefix)
				.when(apiPrefix.isPresent(), () -> Assertion.check()
						.isTrue(apiPrefix.get().startsWith("/"), "Global route apiPrefix must starts with /"));
		//-----
		this.apiPrefix = apiPrefix;

		final String connectorName = connectorNameOpt.orElse("main");
		javalinConnector = javalinConnectors.stream()
				.filter(connector -> connectorName.equals(connector.getName()))
				.findFirst().get();
	}

	/** {@inheritDoc} */
	@Override
	public void registerWebServiceRoute(final HandlerChain handlerChain, final Collection<WebServiceDefinition> webServiceDefinitions) {
		Assertion.check()
				.isNotNull(handlerChain)
				.isNotNull(webServiceDefinitions);
		//-----
		boolean corsProtected = false;
		final Javalin javalinApp = javalinConnector.getClient();
		for (final WebServiceDefinition webServiceDefinition : webServiceDefinitions) {
			final String routePath = convertJaxRsPathToJavalin(apiPrefix.orElse("") + webServiceDefinition.getPath());
			final JavalinRouteHandler javalinRouteHandler = new JavalinRouteHandler(webServiceDefinition, handlerChain);
			switch (webServiceDefinition.getVerb()) {
				case Get:
					javalinApp.get(routePath, javalinRouteHandler);
					break;
				case Post:
					javalinApp.post(routePath, javalinRouteHandler);
					break;
				case Put:
					javalinApp.put(routePath, javalinRouteHandler);
					break;
				case Patch:
					javalinApp.patch(routePath, javalinRouteHandler);
					break;
				case Delete:
					javalinApp.delete(routePath, javalinRouteHandler);
					break;
				default:
					throw new UnsupportedOperationException();
			}
			corsProtected = corsProtected || webServiceDefinition.isCorsProtected();
		}
		if (corsProtected) {
			final JavalinOptionsRouteHandler javalinOptionsRouteHandler = new JavalinOptionsRouteHandler(handlerChain);
			javalinApp.options("*", javalinOptionsRouteHandler);
		}
	}

	private static String convertJaxRsPathToJavalin(final String path) {
		return path.replaceAll("\\(", "%28")
				.replaceAll("\\)", "%29")
				.replaceAll("\\{(.+?)\\}", ":$1"); //.+? : Reluctant regexp
	}
}
