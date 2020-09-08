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
package io.vertigo.social.webservices.handle;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.social.handle.Handle;
import io.vertigo.social.handle.HandleManager;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.InnerBodyParam;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

/**
 * Webservice for Handles.
 *
 * @author mlaroche
 */
@PathPrefix("/vertigo/handle")
public final class HandleWebServices implements WebServices {

	@Inject
	private HandleManager handleServices;

	@POST("/_search")
	public List<Handle> search(@InnerBodyParam("prefix") final String prefix) {
		return handleServices.searchHandles(prefix);
	}

	@GET("/_reindexAll")
	public void reindexAll() {
		handleServices.reindexAll();
	}

}
