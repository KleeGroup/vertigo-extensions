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

import io.vertigo.core.node.component.Plugin;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Provider of Identities for external sources.
 * Convert external entities to local User's model.
 *
 * IdentityProviders could be use to get users and store them in local system.
 *
 * @author pchretien, npiedeloup
 */
public interface IdentityProviderPlugin extends Plugin {
	/**
	 * @return the number of accounts
	 */
	long getUsersCount();

	/**
	 * @return all users
	 * @param <E> project's User entity type
	 */
	<E extends Entity> List<E> getAllUsers();

	/**
	 * Gets the photo of an account defined by its URI.
	 *
	 * @param accountURI the account defined by its URI
	 * @return the photo as a file
	 * @param <E> project's User entity type
	 */
	<E extends Entity> Optional<VFile> getPhoto(UID<E> accountURI);

	/**
	 * Gets an newly authentified account by his authToken.
	 * @param userAuthToken user authToken
	 * @return Logged account
	 * @param <E> project's User entity type
	 */
	<E extends Entity> E getUserByAuthToken(String userAuthToken);

}
