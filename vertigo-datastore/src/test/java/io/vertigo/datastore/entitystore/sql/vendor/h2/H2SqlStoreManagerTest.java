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
package io.vertigo.datastore.entitystore.sql.vendor.h2;

import org.h2.Driver;

import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.datastore.entitystore.sql.AbstractSqlStoreManagerTest;
import io.vertigo.datastore.entitystore.sql.SqlDataStoreNodeConfig;

/**
 * Test of sql storage in H2 DB.
 * @author mlaroche
 *
 */
public final class H2SqlStoreManagerTest extends AbstractSqlStoreManagerTest {

	@Override
	protected NodeConfig buildNodeConfig() {
		return SqlDataStoreNodeConfig.build(
				H2DataBase.class.getCanonicalName(),
				Driver.class.getCanonicalName(),
				"jdbc:h2:mem:database");
	}

}
