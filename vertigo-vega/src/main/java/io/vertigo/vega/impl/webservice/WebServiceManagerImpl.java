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
package io.vertigo.vega.impl.webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.component.AopPlugin;
import io.vertigo.core.node.component.ComponentSpace;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.util.ListBuilder;
import io.vertigo.vega.plugins.webservice.handler.AccessTokenWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.CorsAllowerWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ExceptionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.plugins.webservice.handler.JsonConverterWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.RateLimitingWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.RestfulServiceWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SecurityWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SessionInvalidateWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SessionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ValidatorWebServiceHandlerPlugin;
import io.vertigo.vega.webservice.WebServiceManager;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;

/**
 * Webservice manager.
 * Use some plugins :
 * - WebServiceIntrospectorPlugin : introspect WebService and register WebServiceDefinitions
 * - RoutesRegisterPlugin : Register WebServiceDefinitions to Routing engine (Jersey, Spark or other)
 * - List<WebServiceHandlerPlugin> : Ordered handlers list to managed : request to WebService impl and callback response
 *
 * @author npiedeloup
 */
public final class WebServiceManagerImpl implements WebServiceManager, SimpleDefinitionProvider, Activeable {

	private static final String STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG = "Standard configuration (order is important) :\n"
			+ "- " + ExceptionWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + CorsAllowerWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + SessionInvalidateWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + SessionWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + RateLimitingWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + SecurityWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + AccessTokenWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + JsonConverterWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + ValidatorWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + RestfulServiceWebServiceHandlerPlugin.class.getSimpleName() + "\n";

	private final WebServiceScannerPlugin webServiceScannerPlugin;
	private final WebServerPlugin webServerPlugin;
	private final HandlerChain handlerChain;

	/**
	 * Constructor.
	 * @param webServiceScannerPlugin webServiceScanner Plugin
	 * @param webServerPlugin WebServer use to serve routes
	 * @param restHandlerPlugins WebServiceHandler plugins
	 */
	@Inject
	public WebServiceManagerImpl(
			final WebServiceScannerPlugin webServiceScannerPlugin,
			final WebServerPlugin webServerPlugin,
			final List<WebServiceHandlerPlugin> restHandlerPlugins) {
		Assertion.check()
				.isNotNull(webServiceScannerPlugin)
				.isNotNull(webServerPlugin)
				.isFalse(restHandlerPlugins.isEmpty(), "No WebServiceHandlerPlugins found, check you have declared your WebServiceHandlerPlugins in RestManagerImpl.\n{0}",
						STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG)
				.isNotNull(webServerPlugin);
		//-----
		final List<WebServiceHandlerPlugin> sortedWebServiceHandlerPlugins = restHandlerPlugins
				.stream()
				.sorted(Comparator.comparingInt(WebServiceHandlerPlugin::getStackIndex))
				.collect(Collectors.toList());
		//-----
		Assertion.check().isTrue(sortedWebServiceHandlerPlugins.get(sortedWebServiceHandlerPlugins.size() - 1) instanceof RestfulServiceWebServiceHandlerPlugin,
				"WebServiceHandlerPlugins must end with a RestfulServiceHandler in order to dispatch request to WebService, check your WebServiceHandlerPlugins in RestManagerImpl.\n{0}",
				STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG);
		//-----
		this.webServiceScannerPlugin = webServiceScannerPlugin;
		this.webServerPlugin = webServerPlugin;
		handlerChain = new HandlerChain(sortedWebServiceHandlerPlugins);

	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return scanComponents(Node.getNode().getComponentSpace());
	}

	@Override
	public void start() {
		//we do nothing with webServerPlugin
		//2- We sort by path, parameterized path should be after strict path
		final List<WebServiceDefinition> allWebServiceDefinitions = new ArrayList<>(Node.getNode().getDefinitionSpace().getAll(WebServiceDefinition.class));
		Collections.sort(allWebServiceDefinitions, Comparator.comparing(WebServiceDefinition::getSortPath));
		webServerPlugin.registerWebServiceRoute(handlerChain, allWebServiceDefinitions);
	}

	@Override
	public void stop() {
		// nothing
	}

	/**
	 * Scan WebServices as WebServiceDefinitions on all the components.
	 * @param componentSpace ComponentSpace
	 * @return Scanned webServiceDefinitions
	 */
	List<WebServiceDefinition> scanComponents(final ComponentSpace componentSpace) {
		final AopPlugin aopPlugin = Node.getNode().getNodeConfig().getBootConfig().getAopPlugin();

		final ListBuilder<WebServiceDefinition> allWebServiceDefinitionListBuilder = new ListBuilder<>();
		//1- We introspect all RestfulService class
		for (final String componentId : componentSpace.keySet()) {
			final Object component = componentSpace.resolve(componentId, Object.class);
			if (component instanceof WebServices) {
				final WebServices webServices = aopPlugin.unwrap(WebServices.class.cast(component));
				final List<WebServiceDefinition> webServiceDefinitions = webServiceScannerPlugin.scanWebService(webServices.getClass());
				allWebServiceDefinitionListBuilder.addAll(webServiceDefinitions);
			}
		}

		return allWebServiceDefinitionListBuilder.build();

	}

}
