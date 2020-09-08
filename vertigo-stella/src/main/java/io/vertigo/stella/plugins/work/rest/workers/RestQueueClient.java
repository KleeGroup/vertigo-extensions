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

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.message.GZipEncoder;

import com.google.gson.Gson;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.stella.impl.work.WorkItem;

/**
 * api de distributedWorkQueueManager en REST avec jersey.
 * Pour la partie appel webService voir http://ghads.wordpress.com/2008/09/24/calling-a-rest-webservice-from-java-without-libs/
 *
 * @author npiedeloup
 */
final class RestQueueClient {
	private static final int CONNECT_TIMEOUT = 10 * 1000; // 10s
	private static final Logger LOG = LogManager.getLogger(RestQueueClient.class);
	private final CodecManager codecManager;
	private final String serverUrl;
	private final Client locatorClient;
	private final ConcurrentMap<String, Object> lockByWorkType = new ConcurrentHashMap<>();

	/**
	 * Constructeur.
	 */
	RestQueueClient(final String serverUrl, final int timeoutSeconds, final CodecManager codecManager) {
		Assertion.check()
				.isNotBlank(serverUrl)
				.isNotNull(codecManager);
		//-----
		this.serverUrl = serverUrl;
		this.codecManager = codecManager;
		locatorClient = ClientBuilder.newClient();
		locatorClient.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		locatorClient.property(ClientProperties.READ_TIMEOUT, timeoutSeconds * 1000);
	}

	<R, W> WorkItem<R, W> pollWorkItem(final String nodeId, final String workType) {
		//call methode distante, passe le workItem à started
		try {
			try {
				final String jsonResult;
				final Object lockWork = lockByWorkType.putIfAbsent(workType, new Object());
				//Cette tache est synchronized sur le workType, pour éviter de surcharger le serveur en demandes multiple
				synchronized (lockWork) {
					checkInterrupted(); //must be check because Thread.interrupt() doesn't freed the synchronized lock

					final WebTarget remoteWebResource = prepareTarget(serverUrl + "/pollWork/" + workType + "?nodeUID=" + nodeId);
					final Response response = remoteWebResource.request(MediaType.TEXT_PLAIN).get();
					checkResponseStatus(response);
					jsonResult = response.readEntity(String.class);
				}

				if (!jsonResult.isEmpty() && !"[]".equals(jsonResult)) { //le json est vide s'il n'y a pas de tache en attente
					final String[] result = new Gson().fromJson(jsonResult, String[].class);
					final String uuid = result[0];
					final byte[] serializedResult = codecManager.getBase64Codec().decode(result[1]);
					final Object work = codecManager.getCompressedSerializationCodec().decode(serializedResult);
					LOG.info("pollWork({}) : 1 Work", workType);
					return new WorkItem(uuid, work, ClassUtil.classForName(workType));
				}
				LOG.info("pollWork({}) : no Work", workType);
				//pas de travaux : inutil d'attendre le poll attend déjà 1s coté serveur
			} catch (final ProcessingException c) {
				LOG.warn("[pollWork] Erreur de connexion au serveur " + serverUrl + "/pollWork/" + workType + " (" + c.getMessage() + ")", c);
				//En cas d'erreur on attend quelques secondes, pour attendre que le serveur revienne
				final Object lockWork = lockByWorkType.putIfAbsent(serverUrl, new Object());
				//En cas d'absence du serveur,
				//ce synchronized permet d'étaler les appels au serveur de chaque worker : le premier attendra 2s, le second 2+2s, le troisième : 4+2s, etc..
				//dés le retour du serveur, on récupère un worker toute les 2s
				synchronized (lockWork) {
					checkInterrupted();//must be check because Thread.interrupt() doesn't freed the synchronized lock
					Thread.sleep(2000); //on veut bien un sleep
				}
			} catch (final InterruptedException e) {
				//nothing, in case of interrupt just return null, like no message
			} catch (final Exception c) {
				LOG.warn("[pollWork] Erreur de traitement de l'accès au serveur " + serverUrl + "/pollWork/" + workType + " (" + c.getMessage() + ")", c);
			}
		} catch (final InterruptedException e) {
			//nothing, in case of interrupt just return null, like no message
		}
		return null;
	}

	private WebTarget prepareTarget(final String targetUrl) {
		final WebTarget remoteWebResource = locatorClient.target(targetUrl);
		return remoteWebResource
				.register(GZipEncoder.class);
	}

	void putStart(final String workId) {
		//call methode distante
		final WebTarget remoteWebResource = locatorClient.target(serverUrl + "/event/start/" + workId);
		try {
			final Response response = remoteWebResource.request(MediaType.TEXT_PLAIN).post(Entity.text(""));
			checkResponseStatus(response);
		} catch (final Exception c) {
			LOG.warn("[onStart] Erreur de connexion au serveur {} ({})", remoteWebResource.getUri(), c.getMessage());
		}
	}

	<R> void putResult(final String workId, final R result, final Throwable error) {
		Assertion.check()
				.isNotBlank(workId)
				.isTrue(result == null ^ error == null, "result xor error is null");
		//-----
		final String address;
		final Object value;
		if (error == null) {
			address = serverUrl + "/event/success/";
			value = result;
		} else {
			address = serverUrl + "/event/failure/";
			value = error;
		}
		//call methode distante
		sendValue(workId, address, value);
	}

	private void sendValue(final String uuid, final String address, final Object value) {
		final WebTarget remoteWebResource = locatorClient.target(address + uuid);
		try {
			final byte[] serializedResult = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
			final String jsonResult = codecManager.getBase64Codec().encode(serializedResult);
			final Response response = remoteWebResource.request().post(Entity.text(jsonResult));
			checkResponseStatus(response);
		} catch (final Exception c) {
			LOG.warn("[{}] Erreur de connexion au serveur {} ({})", address, remoteWebResource.getUri(), c.getMessage());
		}
	}

	private static void checkResponseStatus(final Response response) {
		final StatusType status = response.getStatusInfo();
		if (status.getFamily() != Family.SUCCESSFUL) {
			throw new VSystemException("An error occured : {0} {1}\n{2}", status.getStatusCode(), status.getReasonPhrase(), response.readEntity(String.class));
		}
	}

	private static void checkInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Thread interruption required");
		}
	}
}
