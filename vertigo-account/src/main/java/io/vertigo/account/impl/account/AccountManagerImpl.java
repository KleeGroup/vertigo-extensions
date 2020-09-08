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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.account.AccountManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.StreamFile;

/**
 * @author pchretien, npiedeloup
 */
public final class AccountManagerImpl implements AccountManager {
	private final AccountStorePlugin accountStorePlugin; //accès local aux users
	private final Optional<AccountCachePlugin> accountCachePlugin;
	private final VFile defaultPhoto;

	/**
	 * Constructor.
	 * @param accountStorePlugin the account store plugin
	 * @param accountCachePlugin the account cache plugin
	 */
	@Inject
	public AccountManagerImpl(
			final AccountStorePlugin accountStorePlugin,
			final Optional<AccountCachePlugin> accountCachePlugin) {
		Assertion.check().isNotNull(accountStorePlugin)
				.isNotNull(accountCachePlugin);
		//-----
		this.accountStorePlugin = accountStorePlugin;
		this.accountCachePlugin = accountCachePlugin;
		defaultPhoto = StreamFile.of(
				"defaultPhoto.png",
				"image/png",
				AccountManagerImpl.class.getResource("defaultPhoto.png"));
	}

	/** {@inheritDoc} */
	@Override
	public VFile getDefaultPhoto() {
		return defaultPhoto;
	}

	//------------------//
	//-- AccountStore --//
	//------------------//
	/** {@inheritDoc} */
	@Override
	public Account getAccount(final UID<Account> accountUID) {
		return loadWithCache(accountUID,
				() -> accountCachePlugin.get()::getAccount,
				accountStorePlugin::getAccount,
				() -> accountCachePlugin.get()::putAccount);
	}

	/** {@inheritDoc} */
	@Override
	public AccountGroup getGroup(final UID<AccountGroup> groupUID) {
		return loadWithCache(groupUID,
				() -> accountCachePlugin.get()::getGroup,
				accountStorePlugin::getGroup,
				() -> accountCachePlugin.get()::putGroup);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountUID) {
		return loadWithCacheOptionalValue(accountUID,
				() -> accountCachePlugin.get()::getPhoto,
				accountStorePlugin::getPhoto,
				() -> photo -> accountCachePlugin.get().setPhoto(accountUID, photo));
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<Account>> getAccountUIDs(final UID<AccountGroup> groupUID) {
		return loadWithCacheSetValue(groupUID,
				() -> accountCachePlugin.get()::getAccountUIDs,
				accountStorePlugin::getAccountUIDs,
				() -> accountsUID -> accountCachePlugin.get().attach(accountsUID, groupUID));
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<AccountGroup>> getGroupUIDs(final UID<Account> accountUID) {
		return loadWithCacheSetValue(accountUID,
				() -> accountCachePlugin.get()::getGroupUIDs,
				accountStorePlugin::getGroupUIDs,
				() -> groupsUID -> accountCachePlugin.get().attach(accountUID, groupsUID));
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		return loadWithCacheOptionalValue(userAuthToken,
				() -> accountCachePlugin.get()::getAccountByAuthToken,
				accountStorePlugin::getAccountByAuthToken,
				() -> accountCachePlugin.get()::putAccount);
	}

	private <O, U> Optional<O> loadWithCacheOptionalValue(
			final U uid,
			final Supplier<Function<U, Optional<O>>> cacheSupplier,
			final Function<U, Optional<O>> storeSupplier,
			final Supplier<Consumer<O>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Optional<O> resultOpt = cacheSupplier.get().apply(uid);
			if (resultOpt.isEmpty()) {
				final Optional<O> result = storeSupplier.apply(uid);
				result.ifPresent(o -> cacheRegister.get().accept(o));
				return result;
			}
		}
		return storeSupplier.apply(uid);

	}

	private <O, U> Set<O> loadWithCacheSetValue(
			final U uid,
			final Supplier<Function<U, Set<O>>> cacheSupplier,
			final Function<U, Set<O>> storeSupplier,
			final Supplier<Consumer<Set<O>>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Set<O> resultOpt = cacheSupplier.get().apply(uid);
			if (resultOpt.isEmpty()) {
				final Set<O> result = storeSupplier.apply(uid);
				if (!result.isEmpty()) {
					cacheRegister.get().accept(result);
				}
				return result;
			}
		}
		return storeSupplier.apply(uid);

	}

	private <O, U> O loadWithCache(
			final U uid,
			final Supplier<Function<U, Optional<O>>> cacheSupplier,
			final Function<U, O> storeSupplier,
			final Supplier<Consumer<O>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Optional<O> resultOpt = cacheSupplier.get().apply(uid);
			if (resultOpt.isEmpty()) {
				final O result = storeSupplier.apply(uid);
				cacheRegister.get().accept(result);
				return result;
			}
		}
		return storeSupplier.apply(uid);
	}
}
