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
package io.vertigo.social.webservices.account;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.account.AccountManager;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

/**
 * Webservices for account extension.
 *
 * @author npiedeloup
 */
@PathPrefix("/x/accounts")
public final class AccountWebServices implements WebServices {

	private static final String API_VERSION = "0.1.0";
	private static final String IMPL_VERSION = "0.9.2";

	@Inject
	private AccountManager identityManager;

	/**
	 * Gets an account by its id.
	 *
	 * @param id the account id.
	 * @return the account
	 */
	@GET("/api/{id}")
	@AnonymousAccessAllowed
	public Account getAccount(@PathParam("id") final String id) {
		return identityManager.getAccount(UID.of(Account.class, id));
	}

	/**
	 * Gets an account photo by its id.
	 *
	 * @param id account id.
	 * @return the photo of an account
	 */
	@GET("/api/{id}/photo")
	@AnonymousAccessAllowed
	public VFile getAccountPhoto(@PathParam("id") final String id) {
		return identityManager.getPhoto(UID.of(Account.class, id))
				.orElseGet(identityManager::getDefaultPhoto);
	}

	/**
	 * Gets a group by its id.
	 *
	 * @param id the group id.
	 * @return the group
	 */
	@GET("/api/groups/{id}")
	@AnonymousAccessAllowed
	public AccountGroup getAccountGroup(@PathParam("id") final String id) {
		return identityManager.getGroup(UID.of(AccountGroup.class, id));
	}

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
		final Map<String, Object> sizeStats = new MapBuilder<String, Object>()
				.put("accounts", "undefined") //TODO à voir quels stats on veut
				.put("groups", "undefined")
				.build();
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
				.put("api-version", API_VERSION)
				.put("impl-version", IMPL_VERSION)
				.build();
	}

	/**
	 * Extension help.
	 * @return Help object
	 */
	@GET("/infos/help")
	@AnonymousAccessAllowed
	public String getHelp() {
		return "##Account extension"
				+ "\n This extension manage a Account transversal concept use by others extensions.";
	}

}
