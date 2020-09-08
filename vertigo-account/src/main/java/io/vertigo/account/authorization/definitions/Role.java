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
package io.vertigo.account.authorization.definitions;

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;

/**
 * Role is a coherent group of more atomic authorizations.
 * Users have multiple roles.
 * Applications could use a concept of "profils" as a list of roles, but this concept isn't in this module's scope.
 *
 * @author prahmoune, npiedeloup
 */
@DefinitionPrefix(Role.PREFIX)
public final class Role extends AbstractDefinition {
	public static final String PREFIX = "R";
	private final String description;
	private final List<Authorization> authorizations;

	/**
	 * Constructor.
	 *
	 * @param name Role name
	 * @param description Role description
	 * @param authorizations Authorizations list of this role
	 */
	public Role(final String name, final String description, final List<Authorization> authorizations) {
		super(name);
		//---
		Assertion.check()
				.isNotBlank(name)
				.isNotBlank(description)
				.isNotNull(authorizations);
		//---
		this.description = description;
		this.authorizations = authorizations;
	}

	/**
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Authorizations list of this role
	 */
	public List<Authorization> getAuthorizations() {
		return authorizations;
	}
}
