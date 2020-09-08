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
package io.vertigo.account.impl.identityprovider;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.account.identityprovider.IdentityProviderManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * @author pchretien, npiedeloup
 */
public final class IdentityProviderManagerImpl implements IdentityProviderManager {
	private final IdentityProviderPlugin identityProviderPlugin; //external identity access

	/**
	 * Constructor.
	 * @param identityProviderPlugin the account to provision external identities
	 */
	@Inject
	public IdentityProviderManagerImpl(
			final IdentityProviderPlugin identityProviderPlugin) {
		Assertion.check().isNotNull(identityProviderPlugin);
		//-----
		this.identityProviderPlugin = identityProviderPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public long getUsersCount() {
		return identityProviderPlugin.getUsersCount();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> List<E> getAllUsers() {
		return identityProviderPlugin.getAllUsers();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> Optional<VFile> getPhoto(final UID<E> userURI) {
		return identityProviderPlugin.getPhoto(userURI);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E getUserByAuthToken(final String userAuthToken) {
		return identityProviderPlugin.getUserByAuthToken(userAuthToken);
	}

}
