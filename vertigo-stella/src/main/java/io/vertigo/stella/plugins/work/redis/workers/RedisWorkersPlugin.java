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
package io.vertigo.stella.plugins.work.redis.workers;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.impl.workers.WorkersPlugin;
import io.vertigo.stella.plugins.work.redis.RedisDB;

/**
 * NodePlugin
 * Ce plugin permet d'exécuter des travaux en mode distribué.
 * REDIS est utilisé comme plateforme d'échanges.
 *
 * @author pchretien
 */
public final class RedisWorkersPlugin implements WorkersPlugin {
	private final RedisDB redisDB;

	/**
	 *
	 * @param codecManager
	 * @param redisConnector
	 */
	@Inject
	public RedisWorkersPlugin(
			@ParamValue("connectorName") final Optional<String> connectorNameOpt,
			final List<RedisConnector> redisConnectors,
			final CodecManager codecManager) {
		Assertion.check()
				.isNotNull(codecManager)
				.isNotNull(redisConnectors);
		//-----
		final String connectorName = connectorNameOpt.orElse("main");
		final RedisConnector redisConnector = redisConnectors.stream()
				.filter(connector -> connectorName.equals(connector.getName()))
				.findFirst().get();
		redisDB = new RedisDB(codecManager, redisConnector);
	}

	/** {@inheritDoc} */
	@Override
	public <R, W> WorkItem<R, W> pollWorkItem(final String nodeId, final String workType) {
		return redisDB.pollWorkItem(workType);
	}

	/** {@inheritDoc} */
	@Override
	public <R> void putResult(final String workId, final R result, final Throwable error) {
		redisDB.putResult(workId, result, error);
	}

	/** {@inheritDoc} */
	@Override
	public void putStart(final String workId) {
		redisDB.putStart(workId);
	}
}
