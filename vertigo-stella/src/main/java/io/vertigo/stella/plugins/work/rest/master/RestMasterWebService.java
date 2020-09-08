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
package io.vertigo.stella.plugins.work.rest.master;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.stella.impl.master.MasterPlugin;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;
import io.vertigo.vega.webservice.stereotype.SessionLess;

/**
 * Exécution synchrone et distante des Works avec un transfert par WS REST.
 *
 * @author npiedeloup, pchretien
 */
@PathPrefix("/backend/workQueue")
public final class RestMasterWebService implements WebServices {
	private final RestQueueServer restQueueServer;

	/**
	 * Constructor.
	 * @param masterPlugin RestMasterPlugin
	 */
	@Inject
	public RestMasterWebService(final MasterPlugin masterPlugin) {
		Assertion.check()
				.isNotNull(masterPlugin)
				.isTrue(masterPlugin instanceof RestMasterPlugin, "Node MasterPlugin must be a RestMasterPlugin, in order to use Rest protocol ({0})", masterPlugin.getClass().getName());
		//-----
		restQueueServer = ((RestMasterPlugin) masterPlugin).getRestQueueServer();
	}

	/**
	 * @param workType Work type name
	 * @param nodeUID node uid
	 * @return Work todo or empty array
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/pollWork/{workType}")
	public String[] pollWork(@PathParam("workType") final String workType, @QueryParam("nodeUID") final String nodeUID) {
		//-----
		return restQueueServer.pollWork(workType, nodeUID);
	}

	/**
	 * Node marks work as started.
	 * @param uuid work uuid
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@POST("/event/start/{uuid}")
	public void onStart(@PathParam("uuid") final String uuid) {
		//-----
		restQueueServer.onStart(uuid);
	}

	/**
	 * Node marks work as success.
	 * @param uuid work uuid
	 * @param base64Result result serialized/compressed in base64
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@POST("/event/success/{uuid}")
	public void onSuccess(@PathParam("uuid") final String uuid, final String base64Result) {
		//-----
		restQueueServer.onDone(true, uuid, base64Result);
	}

	/**
	 * Node marks work as failur.
	 * @param uuid work uuid
	 * @param base64Result exception serialized/compressed in base64
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@POST("/event/failure/{uuid}")
	public void onFailure(@PathParam("uuid") final String uuid, final String base64Result) {
		//-----
		restQueueServer.onDone(false, uuid, base64Result);
	}

	/**
	 * @return Api version
	 */
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/version")
	public String getApiVersion() {
		//-----
		return restQueueServer.getApiVersion();
	}
}
