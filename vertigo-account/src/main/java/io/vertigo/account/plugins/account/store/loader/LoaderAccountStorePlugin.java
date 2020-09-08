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
package io.vertigo.account.plugins.account.store.loader;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountStorePlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Specific account store, use to redirect loading to a applicative component.
 * @author npiedeloup
 */
public final class LoaderAccountStorePlugin implements AccountStorePlugin, Activeable {

	private final String accountLoaderName;
	private final Optional<String> groupLoaderNameOpt;
	private AccountLoader accountLoader;
	private Optional<GroupLoader> groupLoader;

	/**
	 * @param accountLoaderName Custom AccountLoader component name
	 * @param groupLoaderName Custom GroupLoader component name
	 */
	@Inject
	public LoaderAccountStorePlugin(
			@ParamValue("accountLoaderName") final String accountLoaderName,
			@ParamValue("groupLoaderName") final Optional<String> groupLoaderName) {
		Assertion.check()
				.isNotBlank(accountLoaderName)
				.isNotNull(groupLoaderName);

		this.accountLoaderName = accountLoaderName;
		this.groupLoaderNameOpt = groupLoaderName;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		accountLoader = Node.getNode().getComponentSpace().resolve(accountLoaderName, AccountLoader.class);
		groupLoader = groupLoaderNameOpt
				.map((groupLoaderName) -> Node.getNode().getComponentSpace().resolve(groupLoaderName, GroupLoader.class));
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//rien
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Account getAccount(final UID<Account> accountURI) {
		return accountLoader.getAccount(accountURI);
	}

	//-----
	/** {@inheritDoc} */
	@Override
	public synchronized AccountGroup getGroup(final UID<AccountGroup> groupURI) {
		return getGroupLoader().getGroup(groupURI);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<UID<AccountGroup>> getGroupUIDs(final UID<Account> accountUID) {
		return getGroupLoader().getGroupURIs(accountUID);
	}

	private GroupLoader getGroupLoader() {
		return groupLoader.orElseThrow(() -> new UnsupportedOperationException("No GroupLoader was defined, Groups operations are not supported"));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<UID<Account>> getAccountUIDs(final UID<AccountGroup> groupUID) {
		return getGroupLoader().getAccountURIs(groupUID);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountUID) {
		return accountLoader.getPhoto(accountUID);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		return accountLoader.getAccountByAuthToken(userAuthToken);
	}

}
