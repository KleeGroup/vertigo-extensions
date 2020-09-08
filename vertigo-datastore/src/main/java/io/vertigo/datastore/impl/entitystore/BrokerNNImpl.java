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
package io.vertigo.datastore.impl.entitystore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vertigo.basics.task.AbstractTaskEngineSQL;
import io.vertigo.basics.task.TaskEngineProc;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.lang.Cardinality;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.association.AssociationNNDefinition;
import io.vertigo.datamodel.structure.definitions.association.AssociationNode;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForNNAssociation;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.AssociationUtil;
import io.vertigo.datamodel.task.TaskManager;
import io.vertigo.datamodel.task.definitions.TaskDefinition;
import io.vertigo.datamodel.task.definitions.TaskDefinitionBuilder;
import io.vertigo.datamodel.task.model.Task;
import io.vertigo.datamodel.task.model.TaskBuilder;
import io.vertigo.datastore.entitystore.BrokerNN;

/**
 * Gestion des tables de relations NN.
 * @author dchallas
 */
@Deprecated
final class BrokerNNImpl implements BrokerNN {
	private final SmartTypeDefinition integerSmartType;
	private final TaskManager taskManager;

	private static final class DescriptionNN {

		private final String dataSpace;
		private final String tableName;
		private final DtField sourceField;
		private final Object sourceValue;
		private final DtField targetField;

		DescriptionNN(final DtListURIForNNAssociation dtListURIForAssociation) {
			Assertion.check().isNotNull(dtListURIForAssociation);
			final AssociationNNDefinition associationNNDefinition = dtListURIForAssociation.getAssociationDefinition();

			tableName = associationNNDefinition.getTableName();
			dataSpace = associationNNDefinition.getAssociationNodeB().getDtDefinition().getDataSpace();

			//Par rapport à l'objet on distingue la source et la cible.
			final AssociationNode sourceAssociationNode = AssociationUtil.getAssociationNodeTarget(associationNNDefinition, dtListURIForAssociation.getRoleName());
			sourceField = sourceAssociationNode.getDtDefinition().getIdField().get();

			//Clés primaires de la relation n-n.
			final AssociationNode targetAssociationNode = AssociationUtil.getAssociationNode(associationNNDefinition, dtListURIForAssociation.getRoleName());
			targetField = targetAssociationNode.getDtDefinition().getIdField().get();
			sourceValue = dtListURIForAssociation.getSource().getId();
		}
	}

	/**
	 * Constructor.
	 * @param taskManager Manager des Tasks
	 */
	BrokerNNImpl(final TaskManager taskManager) {
		Assertion.check().isNotNull(taskManager);
		//-----
		this.taskManager = taskManager;
		integerSmartType = SmartTypeDefinition.builder("STyIntegerBroker", BasicType.Integer).build();
	}

	/** {@inheritDoc} */
	@Override
	public void appendNN(final DtListURIForNNAssociation dtListURI, final UID uriToAppend) {
		Assertion.check().isNotNull(uriToAppend);
		//-----
		appendNN(new DescriptionNN(dtListURI), uriToAppend.getId());
	}

	/** {@inheritDoc} */
	@Override
	public void removeAllNN(final DtListURIForNNAssociation dtListURI) {
		// on supprime tout
		removeNN(new DescriptionNN(dtListURI));
	}

	/** {@inheritDoc} */
	@Override
	public void removeNN(final DtListURIForNNAssociation dtListURI, final UID uriToDelete) {
		Assertion.check().isNotNull(uriToDelete);
		//-----
		removeNN(new DescriptionNN(dtListURI), uriToDelete.getId());
	}

	/** {@inheritDoc} */
	@Override
	public void updateNN(final DtListURIForNNAssociation dtListURI, final List<UID> newUriList) {
		Assertion.check().isNotNull(newUriList);
		//-----
		final DescriptionNN descriptionNN = new DescriptionNN(dtListURI);
		//1. on supprime tout
		removeNN(descriptionNN);
		//2. on enregistre la liste actuelle (un par un)
		final Set<UID> set = new HashSet<>();
		for (final UID dtoUri : newUriList) {
			//On vérifie que l'on n'enregistre pas deux fois la même relation.
			Assertion.check().isTrue(set.add(dtoUri), "Duplicate key [{0}] dans la nouvelle collection.", dtoUri);
			appendNN(descriptionNN, dtoUri.getId());
		}
	}

	/**
	 * Supprime toutes les associations liées à l'objet source.
	 * @param nn description de la nn
	 */
	private void removeNN(final DescriptionNN nn) {
		//FieldName
		final String sourceFieldName = nn.sourceField.getName();
		final String sourceColName = StringUtil.camelToConstCase(sourceFieldName);

		final String taskName = "TkDelete" + StringUtil.constToUpperCamelCase(nn.tableName);
		final String request = String.format("delete from %s where %s = #%s#", nn.tableName, sourceColName, sourceFieldName);

		processNN(taskName, request, nn.dataSpace, nn.sourceField, nn.sourceValue, null, null);
	}

	/**
	 * Créer une association.
	 * @param nn description de la nn
	 * @param targetValue targetValue
	 */
	private void appendNN(final DescriptionNN nn, final Object targetValue) {
		//FieldName
		final String sourceFieldName = nn.sourceField.getName();
		final String sourceColName = StringUtil.camelToConstCase(sourceFieldName);
		final String targetFieldName = nn.targetField.getName();
		final String targetColName = StringUtil.camelToConstCase(targetFieldName);
		final String taskName = "TkInsert" + StringUtil.constToUpperCamelCase(nn.tableName);

		final String request = String.format("insert into %s (%s, %s) values (#%s#, #%s#)", nn.tableName, sourceColName, targetColName, sourceFieldName, targetFieldName);
		final int sqlRowCount = processNN(taskName, request, nn.dataSpace, nn.sourceField, nn.sourceValue, nn.targetField, targetValue);
		if (sqlRowCount > 1) {
			throw new VSystemException("More than one row inserted");
		} else if (sqlRowCount == 0) {
			throw new VSystemException("No row inserted");
		}
	}

	/**
	 * Supprime une association.
	 * @param nn description de la nn
	 * @param targetValue targetValue
	 */
	private void removeNN(final DescriptionNN nn, final Object targetValue) {
		//FieldName
		final String sourceFieldName = nn.sourceField.getName();
		final String sourceColName = StringUtil.camelToConstCase(sourceFieldName);
		final String targetFieldName = nn.targetField.getName();
		final String targetColName = StringUtil.camelToConstCase(targetFieldName);
		final String taskName = "TkDelete" + StringUtil.constToUpperCamelCase(nn.tableName);

		final String request = String.format("delete from %s where %s = #%s# and %s = #%s#",
				nn.tableName, sourceColName, sourceFieldName, targetColName, targetFieldName);
		final int sqlRowCount = processNN(taskName, request, nn.dataSpace, nn.sourceField, nn.sourceValue, nn.targetField, targetValue);
		if (sqlRowCount > 1) {
			throw new VSystemException("More than one row removed");
		} else if (sqlRowCount == 0) {
			throw new VSystemException("No row removed");
		}
	}

	private int processNN(
			final String taskDefinitionName,
			final String request,
			final String dataSpace,
			final DtField sourceField,
			final Object sourceValue,
			final DtField targetField,
			final Object targetValue) {
		//FieldName
		final String sourceFieldName = sourceField.getName();

		final TaskDefinitionBuilder taskDefinitionBuilder = TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineProc.class)
				.withDataSpace(dataSpace)
				.withRequest(request)
				.addInAttribute(sourceFieldName, sourceField.getSmartTypeDefinition(), Cardinality.ONE);
		if (targetField != null) {
			taskDefinitionBuilder.addInAttribute(targetField.getName(), targetField.getSmartTypeDefinition(), Cardinality.ONE);
		}
		//OUT, obligatoire
		final TaskDefinition taskDefinition = taskDefinitionBuilder.withOutAttribute(AbstractTaskEngineSQL.SQL_ROWCOUNT, integerSmartType, Cardinality.ONE)
				.build();

		/* Création de la tache. */
		final TaskBuilder taskBuilder = Task.builder(taskDefinition)
				.addValue(sourceFieldName, sourceValue);
		if (targetField != null) {
			taskBuilder.addValue(targetField.getName(), targetValue);
		}

		return taskManager
				.execute(taskBuilder.build())
				.getResult();
	}
}
