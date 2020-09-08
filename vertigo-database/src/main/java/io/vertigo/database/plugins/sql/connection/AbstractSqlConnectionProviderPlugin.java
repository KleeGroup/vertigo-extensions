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
package io.vertigo.database.plugins.sql.connection;

import io.vertigo.core.lang.Assertion;
import io.vertigo.database.impl.sql.SqlConnectionProviderPlugin;
import io.vertigo.database.sql.vendor.SqlDataBase;

/**
 * Classe de base des fournisseurs de connexions dynamo.
 *
 * @author pchretien
 */
public abstract class AbstractSqlConnectionProviderPlugin implements SqlConnectionProviderPlugin {
	private final String name;
	/**
	* Base de données utilisée
	*/
	private final SqlDataBase dataBase;

	/**
	 * Constructor.
	 * @param name ConnectionProvider's name
	 * @param dataBase Type de base de données
	 */
	protected AbstractSqlConnectionProviderPlugin(final String name, final SqlDataBase dataBase) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(dataBase);
		//-----
		this.name = name;
		this.dataBase = dataBase;
	}

	/** {@inheritDoc} */
	@Override
	public final String getName() {
		return name;
	}

	//=========================================================================
	//-----GESTION DU CONNECTION PROVIDER
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final SqlDataBase getDataBase() {
		return dataBase;
	}
}
