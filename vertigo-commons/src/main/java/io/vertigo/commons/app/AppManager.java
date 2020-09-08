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
package io.vertigo.commons.app;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.node.component.Manager;

/**
 * Node Manager.
 * @author mlaroche
 *
 */
public interface AppManager extends Manager {

	/**
	 * Find a node by an id
	 * @param nodeId the id to look for
	 * @return the optional found Node
	 */
	Optional<AppNode> find(String nodeId);

	/**
	 * Find node with a given skill
	 * @param skills the skills to look for
	 * @return the Node matching the skills
	 */
	List<AppNode> locateSkills(String... skills);

	/**
	 * Get the whole topology of an app
	 * @return the topology
	 */
	List<AppNode> getTopology();

	/**
	 * Get the current node
	 * @return the current node
	 */
	AppNode getCurrentNode();

	/**
	 * List the dead nodes of the app
	 * @return the dead nodes
	 */
	List<AppNode> getDeadNodes();

	/**
	 * A consolidated view of the cluster (id+status)
	 * @return the overall status
	 */
	Map<String, List<HealthCheck>> getStatus();

	/**
	 * A consolidated view of the stats of the app
	 * @return the overall stats
	 */
	Map<String, Object> getStats();

	/**
	 * A consolidated view of the config of the app
	 * @return the overall config
	 */
	Map<String, String> getConfig();

}
