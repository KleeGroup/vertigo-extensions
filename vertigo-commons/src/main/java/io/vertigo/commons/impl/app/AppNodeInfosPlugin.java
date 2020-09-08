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
package io.vertigo.commons.impl.app;

import java.util.List;
import java.util.Map;

import io.vertigo.commons.app.AppNode;
import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.node.component.Plugin;

/**
 * Plugin for retrieving infos about a node.
 * @author mlaroche
 *
 */
public interface AppNodeInfosPlugin extends Plugin {

	// TODO : à terme NodeConfig
	String getConfig(AppNode appNode);

	List<HealthCheck> getStatus(AppNode appNode);

	Map<String, Object> getStats(AppNode appNode);

	String getProtocol();

}
