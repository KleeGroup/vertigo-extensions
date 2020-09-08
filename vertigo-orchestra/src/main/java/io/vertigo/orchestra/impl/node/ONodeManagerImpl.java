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
package io.vertigo.orchestra.impl.node;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.lang.Assertion;
import io.vertigo.orchestra.dao.execution.ONodeDAO;
import io.vertigo.orchestra.domain.execution.ONode;

/**
 * Implémentation du gestionnaire de noeuds.
 *
 * @author mlaroche.
 * @version $Id$
 */
@Transactional
public class ONodeManagerImpl implements ONodeManager {
	private static final Logger LOGGER = LogManager.getLogger(ONodeManagerImpl.class);

	@Inject
	private ONodeDAO nodeDAO;

	private Instant lastHeartBeatTime;

	@Override
	public Long registerNode(final String nodeName) {
		Assertion.check().isNotBlank(nodeName);
		// ---
		final Optional<ONode> existingNode = nodeDAO.getNodeByName(nodeName);
		final ONode node = existingNode.orElseGet(ONode::new);
		lastHeartBeatTime = Instant.now().truncatedTo(ChronoUnit.MILLIS); // precision need to be consistent with database
		node.setHeartbeat(lastHeartBeatTime);
		if (existingNode.isPresent()) {
			nodeDAO.update(node);
		} else {
			node.setName(nodeName);
			nodeDAO.create(node);
		}
		return node.getNodId();

	}

	@Override
	public void updateHeartbeat(final Long nodId) {
		final ONode node = nodeDAO.get(nodId);
		if (!lastHeartBeatTime.equals(node.getHeartbeat().truncatedTo(ChronoUnit.MILLIS))) {// precision need to be consistent with database
			//On ne veut pas d'exception, on ne fait que logger en ERROR
			LOGGER.error("Two nodes running with same NodeName {}", node.getName());
		}
		lastHeartBeatTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);// precision need to be consistent with database
		node.setHeartbeat(lastHeartBeatTime);
		nodeDAO.update(node);

	}

}
