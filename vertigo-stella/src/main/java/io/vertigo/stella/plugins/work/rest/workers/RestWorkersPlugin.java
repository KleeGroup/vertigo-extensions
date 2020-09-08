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
package io.vertigo.stella.plugins.work.rest.workers;

import javax.inject.Inject;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.impl.workers.WorkersPlugin;

/**
 * Implémentation de DistributedWorkManager, pour l'execution de travaux par des Workers distant.
 * Cette implémentation représente la partie client qui se déploie en ferme.
 * 1- contacte la partie serveur pour récupérer les travaux qu'elle sait gérer,
 * 2- execute la tache en synchrone exclusivement
 * 3- retourne le résultat au serveur
 *
 * @author npiedeloup, pchretien
 */
public final class RestWorkersPlugin implements WorkersPlugin {
	private final RestQueueClient restQueueClient; //devrait etre un plugin

	/**
	 * Constructeur.
	 * @param serverUrl Url du serveur
	 * @param timeoutSeconds Timeout en seconde des connections vers le serveur (doit être > au timeoutSeconds du serveur)
	 * @param codecManager Manager d'encodage/decodage
	 */
	@Inject
	public RestWorkersPlugin(
			@ParamValue("serverUrl") final String serverUrl,
			@ParamValue("timeoutSeconds") final int timeoutSeconds,
			final CodecManager codecManager) {
		Assertion.check()
				.isNotBlank(serverUrl)
				.isTrue(timeoutSeconds < 10000, "Le timeout s'exprime en seconde.");
		//-----
		restQueueClient = new RestQueueClient(serverUrl + "/backend/workQueue", timeoutSeconds, codecManager);
	}

	/** {@inheritDoc} */
	@Override
	public <WR, W> WorkItem<WR, W> pollWorkItem(final String nodeId, final String workType) {
		return restQueueClient.pollWorkItem(nodeId, workType);
	}

	/** {@inheritDoc} */
	@Override
	public <R> void putResult(final String workId, final R result, final Throwable error) {
		restQueueClient.putResult(workId, result, error);
	}

	/** {@inheritDoc} */
	@Override
	public void putStart(final String workId) {
		restQueueClient.putStart(workId);
	}
}
