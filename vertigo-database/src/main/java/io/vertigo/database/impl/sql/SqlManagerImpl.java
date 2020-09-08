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
package io.vertigo.database.impl.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;

import javax.inject.Inject;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.process.ProcessAnalyticsTracer;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.core.lang.Tuple;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.database.sql.SqlManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.statement.SqlParameter;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
import io.vertigo.database.sql.vendor.SqlMapping;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class SqlManagerImpl implements SqlManager {

	private static final int REQUEST_HEADER_FOR_TRACER = 50;

	private static final int REQUEST_STATEMENT_FOR_TRACER = 4000;

	private final AnalyticsManager analyticsManager;
	private final Map<String, SqlConnectionProvider> connectionProviderPluginMap;

	private final SqlStatementDriver sqlStatementDriver;

	/**
	 * Constructor.
	 * @param localeManager Manager des messages localisés
	 * @param analyticsManager Manager de la performance applicative
	 * @param sqlConnectionProviderPlugins List of connectionProviderPlugin. Names must be unique.
	 */
	@Inject
	public SqlManagerImpl(
			final LocaleManager localeManager,
			final AnalyticsManager analyticsManager,
			final List<SqlConnectionProviderPlugin> sqlConnectionProviderPlugins) {
		Assertion.check()
				.isNotNull(localeManager)
				.isNotNull(analyticsManager)
				.isNotNull(sqlConnectionProviderPlugins);
		//-----
		this.analyticsManager = analyticsManager;
		connectionProviderPluginMap = new HashMap<>();
		for (final SqlConnectionProviderPlugin sqlConnectionProviderPlugin : sqlConnectionProviderPlugins) {
			final String name = sqlConnectionProviderPlugin.getName();
			final SqlConnectionProvider previous = connectionProviderPluginMap.put(name, sqlConnectionProviderPlugin);
			Assertion.check().isNull(previous, "ConnectionProvider {0}, was already registered", name);
		}
		localeManager.add("io.vertigo.database.impl.sql.DataBase", io.vertigo.database.impl.sql.Resources.values());
		//---
		sqlStatementDriver = new SqlStatementDriver();
	}

	/** {@inheritDoc} */
	@Override
	public SqlConnectionProvider getConnectionProvider(final String name) {
		final SqlConnectionProvider sqlConnectionProvider = connectionProviderPluginMap.get(name);
		Assertion.check().isNotNull(sqlConnectionProvider, "ConnectionProvider {0}, wasn't registered.", name);
		return sqlConnectionProvider;
	}

	/** {@inheritDoc} */
	@Override
	public <O> List<O> executeQuery(
			final SqlStatement sqlStatement,
			final Class<O> dataType,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final Integer limit,
			final SqlConnection connection) throws SQLException {
		Assertion.check()
				.isNotNull(sqlStatement)
				.isNotNull(dataType)
				.isNotNull(connection);
		//-----
		try (final PreparedStatement statement = sqlStatementDriver.createStatement(sqlStatement.getSqlQuery(), connection)) {
			sqlStatementDriver.setParameters(statement, sqlStatement.getSqlParameters(), basicTypeAdapters, connection);
			//-----
			return traceWithReturn(sqlStatement.getSqlQuery(), tracer -> doExecuteQuery(statement, tracer, dataType, basicTypeAdapters, limit, connection));
		} catch (final WrappedSqlException e) {
			//SQl Exception is unWrapped
			throw e.getSqlException();
		}
	}

	private <O> List<O> doExecuteQuery(
			final PreparedStatement statement,
			final ProcessAnalyticsTracer tracer,
			final Class<O> dataType,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final Integer limit,
			final SqlConnection connection) {
		// ResultSet JDBC
		final SqlMapping mapping = connection.getDataBase().getSqlMapping();
		try (final ResultSet resultSet = statement.executeQuery()) {
			//Le Handler a la responsabilité de créer les données.
			final List<O> result = sqlStatementDriver.buildResult(dataType, basicTypeAdapters, mapping, resultSet, limit);
			tracer.setMeasure("nbSelectedRow", result.size());
			return result;
		} catch (final SQLException e) {
			//SQl Exception is Wrapped for lambda
			throw new WrappedSqlException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public <O> Tuple<Integer, O> executeUpdateWithGeneratedKey(
			final SqlStatement sqlStatement,
			final GenerationMode generationMode,
			final String columnName,
			final Class<O> dataType,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final SqlConnection connection) throws SQLException {
		Assertion.check()
				.isNotNull(sqlStatement)
				.isNotNull(generationMode)
				.isNotNull(columnName)
				.isNotNull(dataType)
				.isNotNull(connection);
		//---
		try (final PreparedStatement statement = sqlStatementDriver.createStatement(sqlStatement.getSqlQuery(), generationMode, new String[] { columnName }, connection)) {
			sqlStatementDriver.setParameters(statement, sqlStatement.getSqlParameters(), basicTypeAdapters, connection);
			//---
			//execution de la Requête
			final int result = traceWithReturn(sqlStatement.getSqlQuery(), tracer -> doExecute(statement, tracer));
			final O generatedId = sqlStatementDriver.getGeneratedKey(statement, columnName, dataType, connection);
			return Tuple.of(result, generatedId);
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public int executeUpdate(
			final SqlStatement sqlStatement,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final SqlConnection connection) throws SQLException {
		Assertion.check()
				.isNotNull(sqlStatement)
				.isNotNull(connection);
		//---
		try (final PreparedStatement statement = sqlStatementDriver.createStatement(sqlStatement.getSqlQuery(), connection)) {
			sqlStatementDriver.setParameters(statement, sqlStatement.getSqlParameters(), basicTypeAdapters, connection);
			//---
			return traceWithReturn(sqlStatement.getSqlQuery(), tracer -> doExecute(statement, tracer));
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		}
	}

	private static int doExecute(final PreparedStatement statement, final ProcessAnalyticsTracer tracer) {
		try {
			final int res = statement.executeUpdate();
			tracer.setMeasure("nbModifiedRow", res);
			return res;
		} catch (final SQLException e) {
			throw new WrappedSqlException(e);
		}
	}

	private static class WrappedSqlException extends RuntimeException {
		private static final long serialVersionUID = -6501399202170153122L;
		private final SQLException sqlException;

		WrappedSqlException(final SQLException sqlException) {
			Assertion.check().isNotNull(sqlException);
			//---
			this.sqlException = sqlException;
		}

		SQLException getSqlException() {
			return sqlException;
		}

	}

	/** {@inheritDoc} */
	@Override
	public OptionalInt executeBatch(
			final SqlStatement sqlStatement,
			final Map<Class, BasicTypeAdapter> basicTypeAdapters,
			final SqlConnection connection) throws SQLException {
		Assertion.check()
				.isNotNull(sqlStatement)
				.isNotNull(connection);
		//---
		try (final PreparedStatement statement = sqlStatementDriver.createStatement(sqlStatement.getSqlQuery(), connection)) {
			for (final List<SqlParameter> parameters : sqlStatement.getSqlParametersForBatch()) {
				sqlStatementDriver.setParameters(statement, parameters, basicTypeAdapters, connection);
				statement.addBatch();
			}
			return traceWithReturn(sqlStatement.getSqlQuery(), tracer -> doExecuteBatch(statement, tracer));
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		}
	}

	private static OptionalInt doExecuteBatch(final PreparedStatement statement, final ProcessAnalyticsTracer tracer) {
		try {
			final int[] res = statement.executeBatch();
			//Calcul du nombre total de lignes affectées par le batch.
			int count = 0;
			for (final int rowCount : res) {
				count += rowCount;
				if (rowCount == Statement.SUCCESS_NO_INFO) {
					//if there is only one NO _INFO then we consider that we have no info.
					return OptionalInt.empty();
				}
			}
			tracer.setMeasure("nbModifiedRow", count);
			return OptionalInt.of(count);
		} catch (final SQLException e) {
			throw new WrappedSqlException(e);
		}
	}

	/*
	 * Enregistre le début d'exécution du PrepareStatement
	 */
	private <O> O traceWithReturn(final String sql, final Function<ProcessAnalyticsTracer, O> function) {
		return analyticsManager.traceWithReturn(
				"sql",
				"/execute/" + sql.substring(0, Math.min(REQUEST_HEADER_FOR_TRACER, sql.length())),
				tracer -> {
					final O result = function.apply(tracer);
					tracer.addTag("statement", sql.substring(0, Math.min(REQUEST_STATEMENT_FOR_TRACER, sql.length())));
					return result;
				});
	}

}
