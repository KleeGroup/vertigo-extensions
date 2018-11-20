/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.orchestra.domain.execution;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.model.VAccessor;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
@io.vertigo.dynamo.domain.stereotype.DataSpace("orchestra")
public final class OActivityExecution implements Entity {
	private static final long serialVersionUID = 1L;

	private Long aceId;
	private java.util.Date creationTime;
	private java.util.Date beginTime;
	private java.util.Date endTime;
	private String engine;
	private String token;

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_ACE_ACT",
			fkFieldName = "ACT_ID",
			primaryDtDefinitionName = "DT_O_ACTIVITY",
			primaryIsNavigable = true,
			primaryRole = "Activity",
			primaryLabel = "Activity",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_O_ACTIVITY_EXECUTION",
			foreignIsNavigable = false,
			foreignRole = "ExecutionActivity",
			foreignLabel = "ExecutionActivity",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.definition.OActivity> actIdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.definition.OActivity.class, "Activity");

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_ACE_PRE",
			fkFieldName = "PRE_ID",
			primaryDtDefinitionName = "DT_O_PROCESS_EXECUTION",
			primaryIsNavigable = true,
			primaryRole = "ProcessusExecution",
			primaryLabel = "Processus",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_O_ACTIVITY_EXECUTION",
			foreignIsNavigable = false,
			foreignRole = "ExecutionActivity",
			foreignLabel = "ExecutionActivity",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.execution.OProcessExecution> preIdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.execution.OProcessExecution.class, "ProcessusExecution");

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_ACE_NOD",
			fkFieldName = "NOD_ID",
			primaryDtDefinitionName = "DT_O_NODE",
			primaryIsNavigable = true,
			primaryRole = "Node",
			primaryLabel = "Node",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_O_ACTIVITY_EXECUTION",
			foreignIsNavigable = false,
			foreignRole = "ExecutionActivity",
			foreignLabel = "ExecutionActivity",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.execution.ONode> nodIdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.execution.ONode.class, "Node");

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_ACE_EST",
			fkFieldName = "EST_CD",
			primaryDtDefinitionName = "DT_O_EXECUTION_STATE",
			primaryIsNavigable = true,
			primaryRole = "ExecutionState",
			primaryLabel = "ExecutionState",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_O_ACTIVITY_EXECUTION",
			foreignIsNavigable = false,
			foreignRole = "ExecutionActivity",
			foreignLabel = "ExecutionActivity",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.referential.OExecutionState> estCdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.referential.OExecutionState.class, "ExecutionState");

	/** {@inheritDoc} */
	@Override
	public UID<OActivityExecution> getUID() {
		return UID.of(this);
	}

	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id de l'execution d'un processus'.
	 * @return Long aceId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "ID", required = true, label = "Id de l'execution d'un processus")
	public Long getAceId() {
		return aceId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id de l'execution d'un processus'.
	 * @param aceId Long <b>Obligatoire</b>
	 */
	public void setAceId(final Long aceId) {
		this.aceId = aceId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Date de création'.
	 * @return Date creationTime <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_TIMESTAMP", required = true, label = "Date de création")
	public java.util.Date getCreationTime() {
		return creationTime;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date de création'.
	 * @param creationTime Date <b>Obligatoire</b>
	 */
	public void setCreationTime(final java.util.Date creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Date de début'.
	 * @return Date beginTime
	 */
	@Field(domain = "DO_O_TIMESTAMP", label = "Date de début")
	public java.util.Date getBeginTime() {
		return beginTime;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date de début'.
	 * @param beginTime Date
	 */
	public void setBeginTime(final java.util.Date beginTime) {
		this.beginTime = beginTime;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Date de fin'.
	 * @return Date endTime
	 */
	@Field(domain = "DO_O_TIMESTAMP", label = "Date de fin")
	public java.util.Date getEndTime() {
		return endTime;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date de fin'.
	 * @param endTime Date
	 */
	public void setEndTime(final java.util.Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Implémentation effective de l'execution'.
	 * @return String engine
	 */
	@Field(domain = "DO_O_CLASSE", label = "Implémentation effective de l'execution")
	public String getEngine() {
		return engine;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Implémentation effective de l'execution'.
	 * @param engine String
	 */
	public void setEngine(final String engine) {
		this.engine = engine;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Token d'identification'.
	 * @return String token
	 */
	@Field(domain = "DO_O_TOKEN", label = "Token d'identification")
	public String getToken() {
		return token;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Token d'identification'.
	 * @param token String
	 */
	public void setToken(final String token) {
		this.token = token;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Activity'.
	 * @return Long actId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "Activity")
	public Long getActId() {
		return (Long) actIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Activity'.
	 * @param actId Long <b>Obligatoire</b>
	 */
	public void setActId(final Long actId) {
		actIdAccessor.setId(actId);
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Processus'.
	 * @return Long preId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "Processus")
	public Long getPreId() {
		return (Long) preIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Processus'.
	 * @param preId Long <b>Obligatoire</b>
	 */
	public void setPreId(final Long preId) {
		preIdAccessor.setId(preId);
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Node'.
	 * @return Long nodId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "Node")
	public Long getNodId() {
		return (Long) nodIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Node'.
	 * @param nodId Long <b>Obligatoire</b>
	 */
	public void setNodId(final Long nodId) {
		nodIdAccessor.setId(nodId);
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'ExecutionState'.
	 * @return String estCd <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_CODE_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "ExecutionState")
	public String getEstCd() {
		return (String) estCdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'ExecutionState'.
	 * @param estCd String <b>Obligatoire</b>
	 */
	public void setEstCd(final String estCd) {
		estCdAccessor.setId(estCd);
	}

	/**
	 * Association : Activity.
	 * @return l'accesseur vers la propriété 'Activity'
	 */
	public VAccessor<io.vertigo.orchestra.domain.definition.OActivity> activity() {
		return actIdAccessor;
	}

	@Deprecated
	public io.vertigo.orchestra.domain.definition.OActivity getActivity() {
		// we keep the lazyness
		if (!actIdAccessor.isLoaded()) {
			actIdAccessor.load();
		}
		return actIdAccessor.get();
	}

	/**
	 * Retourne l'URI: Activity.
	 * @return URI de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.UID<io.vertigo.orchestra.domain.definition.OActivity> getActivityURI() {
		return actIdAccessor.getURI();
	}

	/**
	 * Association : ExecutionState.
	 * @return l'accesseur vers la propriété 'ExecutionState'
	 */
	public VAccessor<io.vertigo.orchestra.domain.referential.OExecutionState> executionState() {
		return estCdAccessor;
	}

	@Deprecated
	public io.vertigo.orchestra.domain.referential.OExecutionState getExecutionState() {
		// we keep the lazyness
		if (!estCdAccessor.isLoaded()) {
			estCdAccessor.load();
		}
		return estCdAccessor.get();
	}

	/**
	 * Retourne l'URI: ExecutionState.
	 * @return URI de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.UID<io.vertigo.orchestra.domain.referential.OExecutionState> getExecutionStateURI() {
		return estCdAccessor.getURI();
	}

	/**
	 * Association : Node.
	 * @return l'accesseur vers la propriété 'Node'
	 */
	public VAccessor<io.vertigo.orchestra.domain.execution.ONode> node() {
		return nodIdAccessor;
	}

	@Deprecated
	public io.vertigo.orchestra.domain.execution.ONode getNode() {
		// we keep the lazyness
		if (!nodIdAccessor.isLoaded()) {
			nodIdAccessor.load();
		}
		return nodIdAccessor.get();
	}

	/**
	 * Retourne l'URI: Node.
	 * @return URI de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.UID<io.vertigo.orchestra.domain.execution.ONode> getNodeURI() {
		return nodIdAccessor.getURI();
	}

	/**
	 * Association : Processus.
	 * @return l'accesseur vers la propriété 'Processus'
	 */
	public VAccessor<io.vertigo.orchestra.domain.execution.OProcessExecution> processusExecution() {
		return preIdAccessor;
	}

	@Deprecated
	public io.vertigo.orchestra.domain.execution.OProcessExecution getProcessusExecution() {
		// we keep the lazyness
		if (!preIdAccessor.isLoaded()) {
			preIdAccessor.load();
		}
		return preIdAccessor.get();
	}

	/**
	 * Retourne l'URI: Processus.
	 * @return URI de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.UID<io.vertigo.orchestra.domain.execution.OProcessExecution> getProcessusExecutionURI() {
		return preIdAccessor.getURI();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
