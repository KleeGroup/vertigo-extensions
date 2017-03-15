/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.dynamo.impl.database.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracer;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Implémentation Standard de KPrepareStatement.
 *
 * @author pchretien
 */
public class SqlPreparedStatementImpl implements SqlPreparedStatement {
	private static final int NO_GENERATED_KEY_ERROR_VENDOR_CODE = 100;

	private static final int TOO_MANY_GENERATED_KEY_ERROR_VENDOR_CODE = 464;

	private static final int NULL_GENERATED_KEY_ERROR_VENDOR_CODE = -407;

	private static final int REQUEST_HEADER_FOR_TRACER = 50;

	private static final int FETCH_SIZE = 150;

	/**
	 * Cet objet possède un état interne.
	 * Le fonctionnement du KPreparedStatement est régi par un automate s'appuyant sur ces états.
	 */
	enum State {
		/**
		 *  Registering input/output
		 */
		REGISTERING,

		/**
		 * Etat Défini (Clôture la phase d'enregistrement, permet d'exévuter une requête)
		 */
		DEFINED,

		/**
		 * Etat Exécuté (Permet de lire les données)
		 */
		EXECUTED,

		/**
		 * Etat Avorté (Erreur survenue durant l'exécution de la tache, une exception est automatiquement générée)
		 */
		ABORTED;

		void assertRegisteringState() {
			Assertion.checkArgument(this == State.REGISTERING, "register methods and init() requires REGISTERING state.");
		}

		void assertDefinedState() {
			Assertion.checkArgument(this == State.DEFINED, "setValues and execution requires DEFINED state");
		}

		void assertExecutedState() {
			Assertion.checkArgument(this == State.EXECUTED, "execution was not done successfully");
		}

	}

	private static final int GENERATED_KEYS_INDEX = 1;

	private State state;

	/** Connexion.*/
	private final SqlConnection connection;

	/** PreparedStatement JDBC. */
	private PreparedStatement statement;

	/** Requête SQL. */
	private final String sql;

	/** Si on récupère les clés générées.*/
	private final boolean returnGeneratedKeys;

	private final AnalyticsManager analyticsManager;

	//=========================================================================
	//-----GESTION des paramètres types & valeurs
	//=========================================================================
	/**
	 * Listes des paramètres (indexé par les index définis dans les méthodes registerXXX
	 */
	private final List<SqlParameter> parameters = new ArrayList<>();

	/**
	 * Constructeur.
	 * @param sql Requête SQL
	 * @param connection Connexion
	 * @param returnGeneratedKeys true si on récupère les clés générées.
	 */
	public SqlPreparedStatementImpl(final AnalyticsManager analyticsManager, final SqlConnection connection, final String sql,
			final boolean returnGeneratedKeys) {
		Assertion.checkNotNull(connection);
		Assertion.checkNotNull(sql);
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.connection = connection;
		this.sql = sql;
		this.returnGeneratedKeys = returnGeneratedKeys;
		//Initialistaion de l'état interne de l'automate
		state = State.REGISTERING;
		this.analyticsManager = analyticsManager;
	}

	/**
	 * Retourne l'état de l'automate
	 * @return Etat de l'automate
	 */
	final State getState() {
		return state;
	}

	/**
	 * Récupération de la liste des paramètres
	 * @return Liste des paramètres
	 */
	protected final List<SqlParameter> getParameters() {
		return parameters;
	}

	/**
	 * Récupération d'un paramètre préalablement enregistré
	 * @param index Indexe du paramètre
	 * @return Valeur du paramètre
	 */
	final SqlParameter getParameter(final int index) {
		final SqlParameter parameter = parameters.get(index);
		Assertion.checkNotNull(parameter, "Le paramètre à l''index {0} n''a pas été enregistré préalablement !", index);
		return parameter;
	}

	/** {@inheritDoc}  */
	@Override
	public final void close() {
		if (statement != null) {
			try {
				statement.close();
			} catch (final SQLException e) {
				throw WrappedException.wrap(e);
			}
		}
	}

	//=========================================================================
	//-----1ere Etape : Enregistrement
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final void registerParameter(final int index, final DataType dataType, final boolean in) {
		state.assertRegisteringState();
		//---
		final SqlParameter parameter = new SqlParameter(dataType, in);
		parameters.add(index, parameter);
	}

	//=========================================================================
	//-----Clôture des affectations et 1ere Etape
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final void init() throws SQLException {
		state.assertRegisteringState();
		//-----
		statement = createStatement();
		//On passe à l'état Défini, l'enregistrement des types  est clôt.
		state = State.DEFINED;
		//-----
		postInit();
	}

	/**
	 * Permet d'enregistrer les variables OUT dans le cas du callableStatement.
	 *
	 * @throws SQLException Si erreur lors de la construction
	 */
	void postInit() throws SQLException {
		//Ne fait rien dans le cas du preparestatement.
	}

	/**
	 * Crée le PreparedStatement JDBC
	 * Cette méthode peut être surchargée pour redéfinir un autre statement (CallableStatement par exemple)
	 *
	 * @throws SQLException Si erreur
	 * @return PreparedStatement JDBC
	 */
	PreparedStatement createStatement() throws SQLException {
		final int autoGeneratedKeys = returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
		final PreparedStatement preparedStatement = connection.getJdbcConnection().prepareStatement(sql, autoGeneratedKeys);
		preparedStatement.setFetchSize(FETCH_SIZE); //empiriquement 150 est une bonne valeur (Oracle initialise à 10 ce qui est insuffisant)
		return preparedStatement;
	}

	//=========================================================================
	//-----2ème Etape : Setters
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final void setValue(final int index, final Object o) throws SQLException {
		state.assertDefinedState();
		final SqlParameter parameter = getParameter(index);
		Assertion.checkArgument(parameter.isIn(), "Les Setters ne peuvent se faire que sur des paramètres IN");
		//---
		//On récupère le type saisi en amont par la méthode register
		final DataType dataType = parameter.getDataType();
		connection.getDataBase().getSqlMapping().setValueOnStatement(statement, index + 1, dataType, o);
		//On sauvegarde la valeur du paramètre
		parameter.setValue(o);
	}

	//=========================================================================
	//-----3ème Etape : Exécution
	//=========================================================================

	/** {@inheritDoc} */
	@Override
	public final SqlQueryResult executeQuery(final Domain domain) throws SQLException {
		state.assertDefinedState();
		Assertion.checkNotNull(domain);
		//-----
		boolean success = false;
		try {
			final SqlQueryResult result = traceWithReturn(tracer -> doExecuteQuery(tracer, domain));
			success = true;
			return result;
		} catch (final WrappedSqlException e) {
			//SQl Exception is unWrapped
			throw e.getSqlException();
		} finally {
			state = success ? State.EXECUTED : State.ABORTED;
		}
	}

	private SqlQueryResult doExecuteQuery(final AnalyticsTracer tracer, final Domain domain) {
		// ResultSet JDBC
		final SqlMapping mapping = connection.getDataBase().getSqlMapping();
		try (final ResultSet resultSet = statement.executeQuery()) {
			//Le Handler a la responsabilité de créer les données.
			final SqlQueryResult result = SqlUtil.buildResult(domain, mapping, resultSet);
			tracer.setMeasure("nbSelectedRow", result.getSQLRowCount());
			return result;
		} catch (final SQLException e) {
			//SQl Exception is Wrapped for lambda
			throw new WrappedSqlException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public final int executeUpdate() throws SQLException {
		state.assertDefinedState();
		//---
		boolean success = false;
		try {
			//execution de la Requête
			final int result = traceWithReturn(tracer -> {
				try {
					final int res = statement.executeUpdate();
					tracer.setMeasure("nbModifiedRow", res);
					return res;
				} catch (final SQLException e) {
					throw new WrappedSqlException(e);
				}
			});
			success = true;
			return result;
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		} finally {
			state = success ? State.EXECUTED : State.ABORTED;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void addBatch() throws SQLException {
		state.assertDefinedState();
		//---
		statement.addBatch();
	}

	private static class WrappedSqlException extends RuntimeException {
		private static final long serialVersionUID = -6501399202170153122L;
		private final SQLException sqlException;

		WrappedSqlException(final SQLException sqlException) {
			Assertion.checkNotNull(sqlException);
			//---
			this.sqlException = sqlException;
		}

		SQLException getSqlException() {
			return sqlException;
		}
	}

	/** {@inheritDoc} */
	@Override
	public int executeBatch() throws SQLException {
		state.assertDefinedState();
		//---
		boolean success = false;
		try {
			final int result = traceWithReturn(tracer -> {
				try {
					final int[] res = statement.executeBatch();
					//Calcul du nombre total de lignes affectées par le batch.
					int count = 0;
					for (final int rowCount : res) {
						count += rowCount;
					}
					tracer.setMeasure("nbModifiedRow", res.length);
					return count;
				} catch (final SQLException e) {
					throw new WrappedSqlException(e);
				}
			});
			success = true;
			return result;
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		} finally {
			state = success ? State.EXECUTED : State.ABORTED;
		}
	}

	/**
	 * Enregistre le début d'exécution du PrepareStatement
	 */
	private <O> O traceWithReturn(final Function<AnalyticsTracer, O> function) {
		return analyticsManager.traceWithReturn(
				"sql",
				"/execute/" + sql.substring(0, Math.min(REQUEST_HEADER_FOR_TRACER, sql.length())),
				tracer -> {
					final O result = function.apply(tracer);
					tracer.addTag("statement", toString());
					return result;
				});
	}

	//=========================================================================
	//-----Utilitaires
	//-----> affichages de la Query  avec ou sans binding pour faciliter le debugging
	//-----> Récupération du statement
	//-----> Récupération de la connection
	//=========================================================================

	/**
	 * Retourne la chaine SQL de la requête.
	 * @return Chaine SQL de la Requête
	 */
	final String getSql() {
		return sql;
	}

	/**
	 * Retourne la connexion utilisée
	 * @return Connexion utilisée
	 */
	final SqlConnection getConnection() {
		return connection;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return getParameters()
				.stream()
				.map(SqlParameter::toString)
				.collect(Collectors.joining(", ", getSql() + '(', ")"));
	}

	/**
	 * Retourne le preparedStatement
	 *
	 * @return PreparedStatement
	 */
	final PreparedStatement getPreparedStatement() {
		Assertion.checkNotNull(statement, "Le statement est null, l'exécution est elle OK ?");
		//-----
		return statement;
	}

	/** {@inheritDoc} */
	@Override
	public final Object getGeneratedKey(final String columnName, final Domain domain) throws SQLException {
		Assertion.checkArgNotEmpty(columnName);
		Assertion.checkNotNull(domain);
		Assertion.checkArgument(returnGeneratedKeys, "Statement non créé pour retourner les clés générées");
		state.assertExecutedState();
		//-----
		// L'utilisation des generatedKeys permet d'avoir un seul appel réseau entre le
		// serveur d'application et la base de données pour un insert et la récupération de la
		// valeur de la clé primaire en respectant les standards jdbc et sql ansi.

		// Cela est actuellement utilisé en ms sql server.
		// Cela pourrait à terme être utilisé en Oracle à partir de 10g R2, à condition d'indiquer
		// le nom de la colonne clé primaire lors de la création du PreparedStatement jdbc
		// (et en supprimant la syntaxe propriétaire oracle dans le StoreSQL :
		// begin insert ... returning ... into ... end;)
		// cf http://download-east.oracle.com/docs/cd/B19306_01/java.102/b14355/jdbcvers.htm#CHDEGDHJ
		//code SQLException : http://publib.boulder.ibm.com/infocenter/iseries/v5r3/index.jsp?topic=%2Frzala%2Frzalaco.htm
		try (final ResultSet rs = statement.getGeneratedKeys()) {
			final boolean next = rs.next();
			if (!next) {
				throw new SQLException("GeneratedKeys empty", "02000", NO_GENERATED_KEY_ERROR_VENDOR_CODE);
			}
			final SqlMapping mapping = connection.getDataBase().getSqlMapping();
			//ResultSet haven't correctly named columns so we fall back to get the first column, instead of looking for column index by name.
			final int pkRsCol = GENERATED_KEYS_INDEX;//attention le pkRsCol correspond au n° de column dans le RETURNING
			final Object id = mapping.getValueForResultSet(rs, pkRsCol, domain.getDataType()); //attention le pkRsCol correspond au n° de column dans le RETURNING
			if (rs.wasNull()) {
				throw new SQLException("GeneratedKeys wasNull", "23502", NULL_GENERATED_KEY_ERROR_VENDOR_CODE);
			}

			if (rs.next()) {
				throw new SQLException("GeneratedKeys.size >1 ", "0100E", TOO_MANY_GENERATED_KEY_ERROR_VENDOR_CODE);
			}
			return id;
		}
	}
}
