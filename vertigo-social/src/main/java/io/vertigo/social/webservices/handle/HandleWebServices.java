/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.social.webservices.handle;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.social.services.handle.Handle;
import io.vertigo.social.services.handle.HandleServices;
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
	private HandleServices handleServices;

	@POST("/_search")
	public List<Handle> search(@InnerBodyParam("prefix") final String prefix) {
		return handleServices.searchHandles(prefix);
	}

	@GET("/_reindexAll")
	public void reindexAll() {
		handleServices.reindexAll();
	}

}
