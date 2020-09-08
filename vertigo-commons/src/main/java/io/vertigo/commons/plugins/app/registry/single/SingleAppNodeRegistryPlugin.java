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
package io.vertigo.commons.plugins.app.registry.single;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.vertigo.commons.app.AppNode;
import io.vertigo.commons.impl.app.AppNodeRegistryPlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;

/**
 * Memory implementation for a single node node.
 * @author mlaroche
 *
 */
public final class SingleAppNodeRegistryPlugin implements AppNodeRegistryPlugin {

	private AppNode localNode;

	@Override
	public void register(final AppNode appNode) {
		Assertion.check()
				.isNull(localNode, "SingleNode has already been registered")
				.isNotNull(appNode);
		// ---
		localNode = appNode;

	}

	@Override
	public synchronized void unregister(final AppNode appNode) {
		localNode = null;
	}

	@Override
	public Optional<AppNode> find(final String nodeId) {
		if (Node.getNode().getNodeConfig().getNodeId().equals(nodeId)) {
			return Optional.of(localNode);
		}
		return Optional.empty();
	}

	@Override
	public void updateStatus(final AppNode appNode) {
		localNode = appNode;

	}

	@Override
	public List<AppNode> getTopology() {
		if (localNode != null) {
			return Collections.singletonList(localNode);
		}
		return Collections.emptyList();
	}

}
