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
package io.vertigo.stella.plugins.work.redis.master;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;
import io.vertigo.stella.impl.master.MasterPlugin;
import io.vertigo.stella.impl.master.WorkResult;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.plugins.work.redis.RedisDB;

/**
 * Ce plugin permet de distribuer des travaux.
 * REDIS est utilisé comme plateforme d'échanges.
 *
 * @author pchretien
 */
public final class RedisMasterPlugin implements MasterPlugin {
	private final RedisDB redisDB;

	@Inject
	public RedisMasterPlugin(
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

	/** {@inheritDoc}*/
	@Override
	public WorkResult pollResult(final int waitTimeSeconds) {
		return redisDB.pollResult(waitTimeSeconds);
	}

	/** {@inheritDoc} */
	@Override
	public <R, W> void putWorkItem(final WorkItem<R, W> workItem) {
		redisDB.putWorkItem(workItem);
	}
}
