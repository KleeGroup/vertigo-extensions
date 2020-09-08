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
package io.vertigo.database.sql;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.core.lang.Tuple;
import io.vertigo.core.node.component.Manager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;

/**
 * Manages connections to database.
 * @author pchretien
 */
public interface SqlManager extends Manager {
	/** The name of the main connectionProvider. */
	String MAIN_CONNECTION_PROVIDER_NAME = "main";

	/**
	 * @param name ConnectionProvider name
	 * @return SecondaryConnectionProvider
	 */
	SqlConnectionProvider getConnectionProvider(String name);

	/**
	 * Executes a sql query returning a list
	 * @param sqlStatement sqlStatement
	 * @param dataType the return dataType of the list
	 * @param basicTypeAdapters a list of adapters from complexTypes (not natively supported by db) to basicType (supported ones).
	 *  Values are transformed from complex types to basic one before storage.
	 * @param limit the return limit (null if no limit)
	 * @param connection the sqlConnection
	 * @return the list
	 *
	 * @throws SQLException
	 */
	<O> List<O> executeQuery(
			SqlStatement sqlStatement,
			final Class<O> dataType,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final Integer limit,
			final SqlConnection connection) throws SQLException;

	/**
	 * Executes a sql query returning the number of modified rows.
	 * @param sqlStatement sqlStatement
	 * @param basicTypeAdapters a list of adapters from complexTypes (not natively supported by db) to basicType (supported ones).
	 *  Values are transformed from complex types to basic one before storage.
	 * @param connection sqlConnection
	 * @return either the row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
	 * @throws SQLException
	 */
	int executeUpdate(
			SqlStatement sqlStatement,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final SqlConnection connection) throws SQLException;

	/**
	 * Executes a sql query returning the number of modified rows.
	 * @param sqlStatement sqlStatement
	 * @param generationMode the generation methode
	 * @param columnName the column name (of the generated key)
	 * @param dataType the dataType of the generated key
	 * @param basicTypeAdapters a list of adapters from complexTypes (not natively supported by db) to basicType (supported ones).
	 *  Values are transformed from complex types to basic one before storage.
	 * @param connection sqlConnection
	 * @return a tuple with the row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing and the generated key
	 * @throws SQLException
	 */
	<O> Tuple<Integer, O> executeUpdateWithGeneratedKey(
			SqlStatement sqlStatement,
			final GenerationMode generationMode,
			final String columnName,
			final Class<O> dataType,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final SqlConnection connection) throws SQLException;

	/**
	 * Executes the batch .
	 * @param sqlStatement sqlStatement
	 * @param basicTypeAdapters a list of adapters from complexTypes (not natively supported by db) to basicType (supported ones).
	 *  Values are transformed from complex types to basic one before storage.
	 * @param connection sqlConnection
	 * @return the SUM of  row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
	 * if no info available an empty Optional is returned
	 * @throws SQLException Si erreur
	 */
	OptionalInt executeBatch(
			SqlStatement sqlStatement,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final SqlConnection connection) throws SQLException;
}
