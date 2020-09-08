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
package io.vertigo.database;

import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.database.impl.migration.MigrationManagerImpl;
import io.vertigo.database.impl.sql.SqlManagerImpl;
import io.vertigo.database.impl.timeseries.TimeSeriesManagerImpl;
import io.vertigo.database.migration.MigrationManager;
import io.vertigo.database.plugins.migration.liquibase.LiquibaseMigrationPlugin;
import io.vertigo.database.plugins.sql.connection.c3p0.C3p0ConnectionProviderPlugin;
import io.vertigo.database.plugins.sql.connection.datasource.DataSourceConnectionProviderPlugin;
import io.vertigo.database.plugins.timeseries.fake.FakeTimeSeriesPlugin;
import io.vertigo.database.plugins.timeseries.influxdb.InfluxDbTimeSeriesPlugin;
import io.vertigo.database.sql.SqlManager;
import io.vertigo.database.timeseries.TimeSeriesManager;

/**
 * Defines database features.
 *
 * @author mlaroche
 */
public final class DatabaseFeatures extends Features<DatabaseFeatures> {

	/**
	 * Constructor.
	 */
	public DatabaseFeatures() {
		super("vertigo-database");
	}

	/**
	 * Add sqlDataBase management to dynamo.
	 * @return  the feature
	 */
	@Feature("sql")
	public DatabaseFeatures withSqlDataBase() {
		getModuleConfigBuilder()
				.addComponent(SqlManager.class, SqlManagerImpl.class);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("timeseries")
	public DatabaseFeatures withTimeSeriesDataBase() {
		getModuleConfigBuilder()
				.addComponent(TimeSeriesManager.class, TimeSeriesManagerImpl.class);
		return this;
	}

	/**
	 * Add database Migration service.
	 * @return  the feature
	 */
	@Feature("migration")
	public DatabaseFeatures withMigration(final Param... params) {
		getModuleConfigBuilder()
				.addComponent(MigrationManager.class, MigrationManagerImpl.class, params);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("timeseries.influxdb")
	public DatabaseFeatures withInfluxDb(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(InfluxDbTimeSeriesPlugin.class, params);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("timeseries.fake")
	public DatabaseFeatures withFakeTimeseries() {
		getModuleConfigBuilder()
				.addPlugin(FakeTimeSeriesPlugin.class);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("sql.datasource")
	public DatabaseFeatures withDatasource(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(DataSourceConnectionProviderPlugin.class, params);
		return this;
	}

	/**
	 * Add InfluxDb timeseries database.
	 * @return  the feature
	 */
	@Feature("sql.c3p0")
	public DatabaseFeatures withC3p0(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(C3p0ConnectionProviderPlugin.class, params);
		return this;
	}

	/**
	 * Add LiquibaseMigrationPlugin
	 * @return  the feature
	 */
	@Feature("migration.liquibase")
	public DatabaseFeatures withLiquibaseDataBaseMigrationPlugin(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(LiquibaseMigrationPlugin.class, params);
		return this;
	}

	/**

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		//
	}
}
