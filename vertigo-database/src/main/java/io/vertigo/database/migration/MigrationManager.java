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
package io.vertigo.database.migration;

import io.vertigo.core.node.component.Manager;
import io.vertigo.database.sql.SqlManager;

/**
 * MigrationManager is used to perform migration tasks on databases.
 * It's mainly a question of creating/migration database schemas.
 * Two type of tasks can be performed  :
 *   <ul>
 *    <li>update : Update your database schema and data to the current version of your app</li>
 *    <li>check :  Check that node and database are in a coherent state</li>
 *   </ul>
 * @author mlaroche
 *
 */
public interface MigrationManager extends Manager {

	/**
	 * Check if database is ok.
	 * @param connectionName ConnectionProviderName (in the Vertigo's way). For example @see {@link SqlManager.MAIN_CONNECTION_PROVIDER_NAME}
	 */
	void check(final String connectionName);

	/**
	 * Perform all taks on database to make it up to date.
	 * @param connectionName ConnectionProviderName (in the Vertigo's way). For example @see {@link SqlManager.MAIN_CONNECTION_PROVIDER_NAME}
	 */
	void update(final String connectionName);

}
