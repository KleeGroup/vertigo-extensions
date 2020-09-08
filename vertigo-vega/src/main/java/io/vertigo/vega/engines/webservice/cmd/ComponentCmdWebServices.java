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
package io.vertigo.vega.engines.webservice.cmd;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.SessionLess;

public final class ComponentCmdWebServices implements WebServices {

	@Inject
	private JsonEngine jsonEngine;

	@Inject
	private AnalyticsManager analyticsManager;

	/**
	 * Healthcheck WebService.
	 * @return constant string "OK" that can be used to monitor the technical health.
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/vertigo/healthcheck")
	public List<HealthCheck> healthcheck() {
		return analyticsManager.getHealthChecks();
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/components")
	public NodeConfig getNodeConfig() {
		return Node.getNode().getNodeConfig();
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/components/{componentId}")
	public String getComponentConfig(@PathParam("componentId") final String componentId) {
		Assertion.check().isNotBlank(componentId);
		//-----
		final JsonArray jsonModuleConfigs = doGetModuleConfigs();
		for (int i = 0; i < jsonModuleConfigs.size(); i++) {
			final JsonObject jsonModuleConfig = (JsonObject) jsonModuleConfigs.get(i);
			final JsonArray jsonComponentConfigs = jsonModuleConfig.get("componentConfigs").getAsJsonArray();

			for (int j = 0; j < jsonComponentConfigs.size(); j++) {
				final JsonObject jsonComponentConfig = (JsonObject) jsonComponentConfigs.get(j);
				if (componentId.equalsIgnoreCase(jsonComponentConfig.get("id").getAsString())) {
					return jsonEngine.toJson(jsonComponentConfig);
				}
			}
		}
		throw new VSystemException("NotFoundException");
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/components/modules")
	public String getModuleConfigs() {
		return jsonEngine.toJson(doGetModuleConfigs());
	}

	private JsonArray doGetModuleConfigs() {
		final String json = jsonEngine.toJson(Node.getNode().getNodeConfig());
		final JsonParser parser = new JsonParser();
		final JsonObject jsonObject = (JsonObject) parser.parse(json);
		final JsonArray jsonModuleConfigs = jsonObject.get("moduleConfigs").getAsJsonArray();
		return jsonModuleConfigs;
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/components/modules/{moduleName}")
	public String getModuleConfig(@PathParam("moduleName") final String moduleName) {
		Assertion.check().isNotBlank(moduleName);
		//-----
		final JsonArray jsonModuleConfigs = doGetModuleConfigs();
		for (int i = 0; i < jsonModuleConfigs.size(); i++) {
			final JsonObject jsonModuleConfig = (JsonObject) jsonModuleConfigs.get(i);
			if (moduleName.equalsIgnoreCase(jsonModuleConfig.get("name").getAsString())) {
				return jsonEngine.toJson(jsonModuleConfig);
			}
		}
		throw new VSystemException("NotFoundException");
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/definitions")
	public DefinitionSpace getDefinitionsSpace() {
		return Node.getNode().getDefinitionSpace();
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/types")
	public Collection<Class<? extends Definition>> getDefinitionTypes() {
		return Node.getNode().getDefinitionSpace().getAllTypes();
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/definitions/types/{definitionType}")
	public String getDefinitionType(@PathParam("definitionType") final String definitionType) {
		for (final Class<? extends Definition> definitionClass : Node.getNode().getDefinitionSpace().getAllTypes()) {
			if (definitionClass.getSimpleName().equals(definitionType)) {
				return jsonEngine.toJson(Node.getNode().getDefinitionSpace().getAll(definitionClass));
			}
		}
		throw new VSystemException("NotFoundException");
	}

	@AnonymousAccessAllowed
	@GET("/vertigo/definitions/{definitionName}")
	public Definition getDefinition(@PathParam("definitionName") final String definitionName) {
		return Node.getNode().getDefinitionSpace().resolve(definitionName, Definition.class);
	}

}
