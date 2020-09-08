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
import java.util.Optional;

import io.vertigo.commons.app.AppNode;
import io.vertigo.core.node.component.Plugin;

/**
 * Plugin for storing and querying the node topology of an App.
 * @author mlaroche
 *
 */
public interface AppNodeRegistryPlugin extends Plugin {

	/**
	 * Register a node
	 * @param appNode the node to register
	 */
	void register(AppNode appNode);

	/**
	 * Unregister a node
	 * @param appNode the node to unregister
	 */
	void unregister(AppNode appNode);

	/**
	 * Get the whole topology of the app
	 * @return the list of node of the app
	 */
	List<AppNode> getTopology();

	/**
	 * Find a node in the topology with the given id
	 * @param nodeId the id to look for
	 * @return an optional Node
	 */
	Optional<AppNode> find(String nodeId);

	/**
	 * Update the status of a node
	 * @param appNode the node to update
	 */
	void updateStatus(AppNode appNode);

}
