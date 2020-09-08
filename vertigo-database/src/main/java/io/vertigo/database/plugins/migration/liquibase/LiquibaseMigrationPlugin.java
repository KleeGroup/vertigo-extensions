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
package io.vertigo.database.plugins.migration.liquibase;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.param.ParamValue;
import io.vertigo.database.impl.migration.MigrationPlugin;
import io.vertigo.database.sql.SqlManager;
import io.vertigo.database.sql.connection.SqlConnection;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Liquibase Plugin to perform migration tasks on SQL Databases
 * @author mlaroche
 *
 */
public final class LiquibaseMigrationPlugin implements MigrationPlugin {

	private static final Logger LOGGER = LogManager.getLogger(LiquibaseMigrationPlugin.class);

	private final SqlManager sqlManager;

	private final String connectionName;
	private final String masterFile;

	/**
	 * @param masterFile configPath of liquibase
	 * @param connectionNameOpt connectionName to use to performs the tasks (by default {@link SqlManager}.MAIN_CONNECTION_PROVIDER_NAME
	 * @param sqlManager sqlManager
	 */
	@Inject
	public LiquibaseMigrationPlugin(
			@ParamValue("masterFile") final String masterFile,
			@ParamValue("connectionName") final Optional<String> connectionNameOpt,
			final SqlManager sqlManager) {
		Assertion.check()
				.isNotNull(masterFile)
				.isNotNull(connectionNameOpt)
				.isNotNull(sqlManager);
		//---
		this.masterFile = masterFile;
		connectionName = connectionNameOpt.orElse(SqlManager.MAIN_CONNECTION_PROVIDER_NAME);
		this.sqlManager = sqlManager;
	}

	/** {@inheritDoc} */
	@Override
	public void update() {
		LOGGER.info("Liquibase  : checking  on connection {}", connectionName);

		try (final SqlConnection sqlConnection = sqlManager.getConnectionProvider(connectionName).obtainConnection()) {
			final Liquibase lb = createLiquibase();
			final Collection<RanChangeSet> unexpectedChangeSets = lb.listUnexpectedChangeSets(new Contexts(), new LabelExpression());
			Assertion.check().isTrue(unexpectedChangeSets.isEmpty(), "Database is to recent. Please make sure you run the correct version of the node.");
			lb.update(new Contexts());
		} catch (final LiquibaseException | SQLException e) {
			throw WrappedException.wrap(e);
		}
		LOGGER.info("Liquibase  : finished checking on connection {}", connectionName);

	}

	private Liquibase createLiquibase() throws DatabaseException {
		final JdbcConnection jdbcConnection = new JdbcConnection(sqlManager.getConnectionProvider(connectionName).obtainConnection().getJdbcConnection());
		final Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
		return new Liquibase(masterFile, new ClassLoaderResourceAccessor(), db);
	}

	/** {@inheritDoc} */
	@Override
	public void check() {
		LOGGER.info("Liquibase  : updating  on connection {}", connectionName);
		try {
			final Liquibase lb = createLiquibase();
			final List<ChangeSet> changeSetList = lb.listUnrunChangeSets(new Contexts(), new LabelExpression());
			Assertion.check().isTrue(changeSetList.isEmpty(), "Database is not up to date. Please update it before launching the node.");
			final Collection<RanChangeSet> unexpectedChangeSets = lb.listUnexpectedChangeSets(new Contexts(), new LabelExpression());
			Assertion.check().isTrue(unexpectedChangeSets.isEmpty(), "Database is to recent. Please make sure you run the correct version of the node.");
		} catch (final LiquibaseException e) {
			throw WrappedException.wrap(e);
		}
		LOGGER.info("Liquibase  : finished updating on connection {}", connectionName);
	}

	/** {@inheritDoc} */
	@Override
	public String getConnectionName() {
		return connectionName;
	}

}
