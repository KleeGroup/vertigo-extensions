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
package io.vertigo.datafactory.impl.search.loader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.basics.task.TaskEngineSelect;
import io.vertigo.commons.transaction.Transactional;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.node.Node;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.model.Task;

/**
 * Default SearchLoader for Database datasource.
 * @author npiedeloup
 * @param <P> Primary key type
 * @param <S> KeyConcept type
 * @param <I> Index type
 */
public abstract class AbstractSqlSearchLoader<P extends Serializable, S extends KeyConcept, I extends DtObject> extends AbstractSearchLoader<P, S, I> {
	private static final int SEARCH_CHUNK_SIZE = 500;
	private final TaskManager taskManager;
	private final VTransactionManager transactionManager;

	/**
	 * Constructor.
	 * @param taskManager Task manager
	 * @param transactionManager transactionManager
	 */
	@Inject
	public AbstractSqlSearchLoader(
			final TaskManager taskManager,
			final VTransactionManager transactionManager) {
		Assertion.check()
				.isNotNull(taskManager)
				.isNotNull(transactionManager);
		// -----
		this.taskManager = taskManager;
		this.transactionManager = transactionManager;
	}

	protected final VTransactionManager getTransactionManager() {
		return transactionManager;
	}

	/** {@inheritDoc} */
	@Override
	@Transactional
	protected final List<UID<S>> loadNextURI(final P lastId, final DtDefinition dtDefinition) {
		try (final VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final String entityName = getEntityName(dtDefinition);
			final String tableName = StringUtil.camelToConstCase(entityName);
			final String taskName = "TkSelect" + entityName + "NextSearchChunk";
			final DtField idField = dtDefinition.getIdField().get();
			final String idFieldName = idField.getName();
			final String request = getNextIdsSqlQuery(tableName, idFieldName);

			final TaskDefinition taskDefinition = TaskDefinition.builder(taskName)
					.withEngine(TaskEngineSelect.class)
					.withDataSpace(dtDefinition.getDataSpace())
					.withRequest(request)
					.addInAttribute(idFieldName, idField.getSmartTypeDefinition(), Cardinality.ONE)
					.withOutAttribute("dtc", Node.getNode().getDefinitionSpace().resolve(SmartTypeDefinition.PREFIX + dtDefinition.getName(), SmartTypeDefinition.class), Cardinality.MANY)
					.build();

			final Task task = Task.builder(taskDefinition)
					.addValue(idFieldName, lastId)
					.build();

			final DtList<S> resultDtc = taskManager
					.execute(task)
					.getResult();

			final List<UID<S>> uris = new ArrayList<>(resultDtc.size());
			for (final S dto : resultDtc) {
				uris.add(UID.<S> of(dtDefinition, DtObjectUtil.getId(dto)));
			}
			return uris;
		}
	}

	/**
	 * Create a SQL query to get next chunk's ids next in table from previous chunk
	 * @param tableName Table name to use
	 * @param pkFieldName Pk field name
	 * @return SQL query
	 */
	protected String getNextIdsSqlQuery(final String tableName, final String pkFieldName) {
		final String pkColumnName = StringUtil.camelToConstCase(pkFieldName);
		final StringBuilder request = new StringBuilder()
				.append(" select ").append(pkColumnName).append(" from ")
				.append(tableName)
				.append(" where ")
				.append(pkColumnName)
				.append(" > #")
				.append(pkFieldName)
				.append('#');
		final String sqlQueryFilter = getSqlQueryFilter();
		Assertion.check().isNotNull(sqlQueryFilter, "getSqlQueryFilter can't be null");
		if (!sqlQueryFilter.isEmpty()) {
			request.append(" and (").append(sqlQueryFilter).append(')');
		}
		request.append(" order by ").append(pkColumnName).append(" ASC");
		appendMaxRows(request, SEARCH_CHUNK_SIZE);
		return request.toString();
	}

	/**
	 * Ajoute à la requete les éléments techniques nécessaire pour limiter le resultat à {maxRows}.
	 * @param request Buffer de la requete
	 * @param maxRows Nombre de lignes max
	 */
	protected void appendMaxRows(final StringBuilder request, final Integer maxRows) {
		request.append(" limit ").append(maxRows); //Attention : non compatible avec toutes les bases
		//sur Oracle, il faut ajouter "select * from ("+request+") where rownum <= "+mawRows
	}

	/**
	 * @return Specific SqlQuery filter
	 */
	protected String getSqlQueryFilter() {
		//nothing, but overrideable
		return "";
	}

	/**
	 * @return TaskManager
	 */
	protected final TaskManager getTaskManager() {
		return taskManager;
	}

	/**
	 * Nom de la table en fonction de la définition du DT mappé.
	 *
	 * @param dtDefinition Définition du DT mappé
	 * @return Nom de la table
	 */
	protected static final String getEntityName(final DtDefinition dtDefinition) {
		return dtDefinition.getFragment().orElse(dtDefinition).getLocalName();
	}

}
