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
package io.vertigo.basics.task.sqlserver;

import java.sql.SQLException;
import java.util.OptionalInt;

import javax.inject.Inject;

import io.vertigo.basics.task.AbstractTaskEngineSQL;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Tuple;
import io.vertigo.core.util.StringUtil;
import io.vertigo.database.sql.SqlManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * Permet l'appel de requête insert en utilisant generatedKeys du PreparedStatement pour récupérer
 * la valeur de la clé primaire. Une tache utilisant cet engine ne traite pas les DtList.<br>
 * <br>
 * @author  jmainaud, evernat
 */
public class TaskEngineInsertWithGeneratedKeys extends AbstractTaskEngineSQL {

	/**
	 * Constructor.
	 * @param scriptManager scriptManager
	 * @param transactionManager transactionManager
	 * @param entityStoreManager storeManager
	 * @param sqlManager sqlDataBaseManager
	 */
	@Inject
	public TaskEngineInsertWithGeneratedKeys(
			final ScriptManager scriptManager,
			final VTransactionManager transactionManager,
			final SqlManager sqlManager,
			final SmartTypeManager smartTypeManager) {
		super(scriptManager, transactionManager, sqlManager, smartTypeManager);
	}

	/** {@inheritDoc} */
	@Override
	public OptionalInt doExecute(
			final SqlStatement sqlStatement,
			final SqlConnection connection) throws SQLException {
		Assertion.check()
				.isNotNull(sqlStatement)
				.isNotNull(connection);
		//--
		final GenerationMode generationMode = connection.getDataBase().getSqlDialect().getGenerationMode();

		// gestion de generatedKey
		final Entity entity = getValue("dto");

		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		final DtField idField = dtDefinition.getIdField().get();

		final Tuple<Integer, ?> result = getDataBaseManager()
				.executeUpdateWithGeneratedKey(
						sqlStatement,
						generationMode,
						StringUtil.camelToConstCase(idField.getName()),
						idField.getSmartTypeDefinition().getJavaClass(),
						getModelManager().getTypeAdapters("sql"),
						connection);

		final Object id = result.getVal2();
		idField.getDataAccessor().setValue(entity, id);
		//---
		return /*sqlRowcount*/ OptionalInt.of(result.getVal1());
	}
}
