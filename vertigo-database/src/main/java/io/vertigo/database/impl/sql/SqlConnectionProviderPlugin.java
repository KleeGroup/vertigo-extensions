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

import java.util.Collections;

import io.vertigo.core.analytics.health.HealthChecked;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthMeasureBuilder;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.database.sql.SqlManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.statement.SqlStatement;

/**
* Plugin du provider de connexions.
*
* @author pchretien
*/
public interface SqlConnectionProviderPlugin extends SqlConnectionProvider, Plugin {

	/**
	 * @return ConnectionProvider's name
	 */
	String getName();

	@HealthChecked(name = "testQuery", feature = "sqlDatabase")
	default HealthMeasure checkTestSelect() {

		final HealthMeasureBuilder healthMeasureBuilder = HealthMeasure.builder();

		final String testQuery = getDataBase().getSqlDialect().getTestQuery();
		try {

			final SqlManager sqlManager = Node.getNode().getComponentSpace().resolve(SqlManager.class);
			try (final SqlConnection connection = obtainConnection()) {
				sqlManager.executeQuery(
						SqlStatement.builder(testQuery).build(),
						Integer.class,
						Collections.emptyMap(),
						1,
						connection);
			}
			healthMeasureBuilder.withGreenStatus();
		} catch (final Exception e) {
			healthMeasureBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthMeasureBuilder.build();

	}
}
