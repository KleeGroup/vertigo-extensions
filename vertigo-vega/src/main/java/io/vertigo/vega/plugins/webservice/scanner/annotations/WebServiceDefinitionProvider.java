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
package io.vertigo.vega.plugins.webservice.scanner.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.config.DefinitionResourceConfig;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.Selector;
import io.vertigo.core.util.Selector.ClassConditions;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;

/**
 * Web service definition provider.
 * @author npiedeloup
 */
public final class WebServiceDefinitionProvider implements SimpleDefinitionProvider {

	private final List<DefinitionResourceConfig> definitionResourceConfigs = new ArrayList<>();

	/** {@inheritDoc} */
	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		Assertion.check().isFalse(definitionResourceConfigs.isEmpty(), "No definitionResource registered");
		//-----
		final List<Definition> webServiceDefinitions = new ArrayList<>();
		for (final DefinitionResourceConfig definitionResourceConfig : definitionResourceConfigs) {
			final String resourcePath = definitionResourceConfig.getPath();
			if (resourcePath.endsWith(".*")) {
				scanAndAddPackage(resourcePath.substring(0, resourcePath.length() - ".*".length()), webServiceDefinitions);
			} else {
				final Class<? extends WebServices> webServicesClass = ClassUtil.classForName(resourcePath, WebServices.class);
				webServiceDefinitions.addAll(scanWebServices(webServicesClass));
			}
		}
		Assertion.check().isFalse(webServiceDefinitions.isEmpty(), "No webService found by WebServiceDefinitionProvider");
		//----
		return webServiceDefinitions;
	}

	private static void scanAndAddPackage(final String packagePath, final List<Definition> webServiceDefinitions) {
		final Collection<Class> webServicesClasses = Selector
				.from(packagePath)
				.filterClasses(ClassConditions.subTypeOf(WebServices.class))
				.findClasses();

		//--Enregistrement des fichiers java annotés
		for (final Class<? extends WebServices> webServicesClass : webServicesClasses) {
			webServiceDefinitions.addAll(scanWebServices(webServicesClass));
		}
	}

	private static List<WebServiceDefinition> scanWebServices(final Class<? extends WebServices> webServicesClass) {
		return AnnotationsWebServiceScannerUtil.scanWebService(webServicesClass);
	}

	/** {@inheritDoc} */
	@Override
	public void addDefinitionResourceConfig(final DefinitionResourceConfig definitionResourceConfig) {
		Assertion.check().isTrue("webservice".equals(definitionResourceConfig.getType()), "This DefinitionProvider Support only 'webservice' type (not {0})", definitionResourceConfig.getType());
		//-----
		definitionResourceConfigs.add(definitionResourceConfig);
	}

}
