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
package io.vertigo.database.sql.connection;

import java.sql.Connection;
import java.sql.SQLException;

import io.vertigo.commons.transaction.VTransactionResource;
import io.vertigo.core.lang.Assertion;
import io.vertigo.database.sql.vendor.SqlDataBase;

/**
 * Connexion à une base de données JDBC.
 * Une connexion est une ressource qui participe à la transaction.
 * Le commit (ou rollback) de la transaction commit (ou rollback) les différentes
 * resources participant à la transaction puis libére (release) les différentes ressources.
 *
 * @author pchretien, npiedeloup
 */
public final class SqlConnection implements VTransactionResource {
	private final Connection jdbcConnection;
	private final SqlDataBase dataBase;

	/**
	 * Constructor.
	 *
	 * @param jdbcConnection Connexion JDBC
	 * @param dataBase Base de données
	 * @throws SQLException Exception sql
	 */
	public SqlConnection(final Connection jdbcConnection, final SqlDataBase dataBase) throws SQLException {
		Assertion.check()
				.isNotNull(jdbcConnection)
				.isNotNull(dataBase);
		//-----
		this.jdbcConnection = jdbcConnection;
		this.dataBase = dataBase;
		//mode autocommit MUST NEVER be activated.
		jdbcConnection.setAutoCommit(false);
	}

	/**
	 * Retourne la connexion JDBC.
	 *
	 * @return Connexion JDBC
	 */
	public Connection getJdbcConnection() {
		return jdbcConnection;
	}

	/**
	 * @return Base de données dont est issue la connexion.
	 */
	public SqlDataBase getDataBase() {
		return dataBase;
	}

	/** {@inheritDoc} */
	@Override
	public void commit() throws SQLException {
		jdbcConnection.commit();
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() throws SQLException {
		jdbcConnection.rollback();
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws SQLException {
		jdbcConnection.close();
	}
}
