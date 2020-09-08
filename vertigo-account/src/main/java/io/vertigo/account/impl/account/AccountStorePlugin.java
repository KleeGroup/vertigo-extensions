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
package io.vertigo.account.impl.account;

import java.util.Optional;
import java.util.Set;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * @author npiedeloup, pchretien
 */
public interface AccountStorePlugin extends Plugin {

	/**
	 * @param accountUID the account defined by its UID
	 * @return the account
	 */
	Account getAccount(UID<Account> accountUID);

	/**
	 * @param accountUID the account defined by its UID
	 * @return Set of groups of this account
	 */
	Set<UID<AccountGroup>> getGroupUIDs(UID<Account> accountUID);

	/**
	 * Gets the group defined by an UID.
	 * @param groupUID the group UID
	 * @return the group
	 */
	AccountGroup getGroup(UID<AccountGroup> groupUID);

	/**
	 * Lists the accounts for a defined group.
	 * @param groupUID the group UID
	 * @return the list of acccounts.
	 */
	Set<UID<Account>> getAccountUIDs(UID<AccountGroup> groupUID);

	/**
	 * Gets the photo of an account defined by its UID.
	 *
	 * @param accountUID the account defined by its UID
	 * @return the photo as a file
	 */
	Optional<VFile> getPhoto(UID<Account> accountUID);

	/**
	 * Get an newly authentify user by his authToken.
	 * @param userAuthToken user authToken
	 * @return Logged account
	 */
	Optional<Account> getAccountByAuthToken(String userAuthToken);
}
