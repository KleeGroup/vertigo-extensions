/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.dynamo.plugins.work.rest.worker;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.work.WorkEngineProvider;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * api de distributedWorkQueueManager en REST avec jersey.
 * Pour la partie appel webService voir http://ghads.wordpress.com/2008/09/24/calling-a-rest-webservice-from-java-without-libs/
 *
 * @author npiedeloup
 */
final class RestQueueClient {
	private static final Logger LOG = Logger.getLogger(RestQueueClient.class);
	private final CodecManager codecManager;
	private final String nodeUID;
	private final String serverUrl;
	private final Client locatorClient;
	private final ConcurrentMap<String, Object> lockByWorkType = new ConcurrentHashMap<>();

	/**
	 * Constructeur.
	 */
	RestQueueClient(final String nodeUID, final String serverUrl, final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(nodeUID);
		Assertion.checkArgNotEmpty(serverUrl);
		Assertion.checkNotNull(codecManager);
		//---------------------------------------------------------------------
		this.nodeUID = nodeUID;
		this.serverUrl = serverUrl;
		this.codecManager = codecManager;
		locatorClient = Client.create();
		locatorClient.addFilter(new com.sun.jersey.api.client.filter.GZIPContentEncodingFilter());
	}

	<WR, W> WorkItem<WR, W> pollWorkItem(final String workType, final int timeoutInSeconds) {
		//call methode distante, passe le workItem à started
		try {
			final String jsonResult;
			lockByWorkType.putIfAbsent(workType, new Object());
			//Cette tache est synchronized sur le workType, pour éviter de surcharger le serveur en demandes multiple
			synchronized (lockByWorkType.get(workType)) {
				final WebResource remoteWebResource = locatorClient.resource(serverUrl + "/pollWork/" + workType + "?nodeUID=" + nodeUID);
				final ClientResponse response = remoteWebResource.get(ClientResponse.class);
				checkResponseStatus(response);
				jsonResult = response.getEntity(String.class);
			}
			if (!jsonResult.isEmpty()) { //le json est vide s'il n'y a pas de tache en attente
				final String[] result = new Gson().fromJson(jsonResult, String[].class);
				final String uuid = result[0];
				final byte[] serializedResult = codecManager.getBase64Codec().decode(result[1]);
				final Object work = codecManager.getCompressedSerializationCodec().decode(serializedResult);
				LOG.info("pollWork(" + workType + ") : 1 Work");
				return new WorkItem(uuid, work, new WorkEngineProvider(workType));
				//				return new WorkItem(uuid, work, new WorkEngineProvider(workType), new CallbackWorkResultHandler(uuid, this));
			}
			LOG.info("pollWork(" + workType + ") : no Work");
			//pas de travaux : inutil d'attendre le poll attend déjà 1s coté serveur
		} catch (final ClientHandlerException c) {
			LOG.warn("[pollWork] Erreur de connexion au serveur " + serverUrl + "/pollWork/" + workType + " (" + c.getMessage() + ")");
			//En cas d'erreur on attend quelques secondes, pour attendre que le serveur revienne
			try {
				lockByWorkType.putIfAbsent(serverUrl, new Object());
				//En cas d'absence du serveur,
				//ce synchronized permet d'étaler les appels au serveur de chaque worker : le premier attendra 2s, le second 2+2s, le troisième : 4+2s, etc..
				//dés le retour du serveur, on récupère un worker toute les 2s
				synchronized (lockByWorkType.get(serverUrl)) {
					Thread.sleep(2000); //on veut bien un sleep
				}
			} catch (final InterruptedException e) {
				//rien on retourne
			}
		} catch (final Exception c) {
			LOG.warn("[pollWork] Erreur de traitement de l'accès au serveur " + serverUrl + "/pollWork/" + workType + " (" + c.getMessage() + ")", c);
		}
		return null;
	}

	void putStart(final String workId) {
		//call methode distante
		final WebResource remoteWebResource = locatorClient.resource(serverUrl + "/event/start/" + workId);
		try {
			final ClientResponse response = remoteWebResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class);
			checkResponseStatus(response);
		} catch (final Exception c) {
			LOG.warn("[onStart] Erreur de connexion au serveur " + remoteWebResource.getURI() + " (" + c.getMessage() + ")");
		}
	}

	<WR> void putResult(final String workId, final WR result, final Throwable error) {
		Assertion.checkArgNotEmpty(workId);
		Assertion.checkArgument(result == null ^ error == null, "result xor error is null");
		//---------------------------------------------------------------------
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
		final WebResource remoteWebResource = locatorClient.resource(address + uuid);
		try {
			final byte[] serializedResult = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
			final String jsonResult = codecManager.getBase64Codec().encode(serializedResult);
			final ClientResponse response = remoteWebResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, jsonResult);
			checkResponseStatus(response);
		} catch (final Exception c) {
			LOG.warn("[" + address + "] Erreur de connexion au serveur " + remoteWebResource.getURI() + " (" + c.getMessage() + ")");
		}
	}

	private static void checkResponseStatus(final ClientResponse response) {
		final Status status = response.getClientResponseStatus();
		if (status.getFamily() != Family.SUCCESSFUL) {
			throw new RuntimeException("Une erreur est survenue : " + status.getStatusCode() + " " + status.getReasonPhrase());
		}
	}
}
