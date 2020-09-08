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
package io.vertigo.orchestra.webservices;

import java.util.HashMap;
import java.util.Map;

import io.vertigo.core.util.MapBuilder;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

/**
 * Webservice for Orchestra extension.
 *
 * @author mlaroche
 */
@PathPrefix("/orchestra")
public class WsInfos implements WebServices {
	//-----
	/**
	 * Extension status (code 200 or 500)
	 * @return "OK" or error message
	 */
	@GET("/infos/status")
	@AnonymousAccessAllowed
	public String getStatus() {
		return "OK";
	}

	/**
	 * Extension stats.
	 * @return "OK" or error message
	 */
	@GET("/infos/stats")
	@AnonymousAccessAllowed
	public Map<String, Object> getStats() {
		final Map<String, Object> stats = new HashMap<>();
		final Map<String, Object> sizeStats = new HashMap<>();
		sizeStats.put("orchestra", "not yet");
		stats.put("size", sizeStats);
		return stats;
	}

	/**
	 * Extension config.
	 * @return Config object
	 */
	@GET("/infos/config")
	@AnonymousAccessAllowed
	public Map<String, Object> getConfig() {
		return new MapBuilder<String, Object>()
				.build();
	}

	/**
	 * Extension help.
	 * @return Help object
	 */
	@GET("/infos/help")
	@AnonymousAccessAllowed
	public String getHelp() {
		return "##Orchestra extension"
				+ "\n This extension manage the jobs/batch center.";
	}
}
