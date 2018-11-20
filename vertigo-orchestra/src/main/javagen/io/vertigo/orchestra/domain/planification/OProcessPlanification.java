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
package io.vertigo.orchestra.domain.planification;

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
public final class OProcessPlanification implements Entity {
	private static final long serialVersionUID = 1L;

	private Long prpId;
	private java.util.Date expectedTime;
	private String initialParams;

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_PRP_PRO",
			fkFieldName = "PRO_ID",
			primaryDtDefinitionName = "DT_O_PROCESS",
			primaryIsNavigable = true,
			primaryRole = "Processus",
			primaryLabel = "Processus",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_O_PROCESS_PLANIFICATION",
			foreignIsNavigable = false,
			foreignRole = "ProcessPlanification",
			foreignLabel = "PlanificationProcessus",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.definition.OProcess> proIdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.definition.OProcess.class, "Processus");

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_PRP_NOD",
			fkFieldName = "NOD_ID",
			primaryDtDefinitionName = "DT_O_NODE",
			primaryIsNavigable = true,
			primaryRole = "Node",
			primaryLabel = "Node",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_O_PROCESS_PLANIFICATION",
			foreignIsNavigable = false,
			foreignRole = "ProcessPlanification",
			foreignLabel = "PlanificationProcessus",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.execution.ONode> nodIdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.execution.ONode.class, "Node");

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_PRP_PST",
			fkFieldName = "SST_CD",
			primaryDtDefinitionName = "DT_O_SCHEDULER_STATE",
			primaryIsNavigable = true,
			primaryRole = "PlanificationState",
			primaryLabel = "PlanificationState",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_O_PROCESS_PLANIFICATION",
			foreignIsNavigable = false,
			foreignRole = "ProcessPlanification",
			foreignLabel = "ProcessPlanification",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.referential.OSchedulerState> sstCdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.referential.OSchedulerState.class, "PlanificationState");

	/** {@inheritDoc} */
	@Override
	public UID<OProcessPlanification> getUID() {
		return UID.of(this);
	}

	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id Planification'.
	 * @return Long prpId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "ID", required = true, label = "Id Planification")
	public Long getPrpId() {
		return prpId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id Planification'.
	 * @param prpId Long <b>Obligatoire</b>
	 */
	public void setPrpId(final Long prpId) {
		this.prpId = prpId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Date d'execution prévue'.
	 * @return Date expectedTime
	 */
	@Field(domain = "DO_O_TIMESTAMP", label = "Date d'execution prévue")
	public java.util.Date getExpectedTime() {
		return expectedTime;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date d'execution prévue'.
	 * @param expectedTime Date
	 */
	public void setExpectedTime(final java.util.Date expectedTime) {
		this.expectedTime = expectedTime;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Paramètres initiaux sous forme de JSON'.
	 * @return String initialParams
	 */
	@Field(domain = "DO_O_JSON_TEXT", label = "Paramètres initiaux sous forme de JSON")
	public String getInitialParams() {
		return initialParams;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Paramètres initiaux sous forme de JSON'.
	 * @param initialParams String
	 */
	public void setInitialParams(final String initialParams) {
		this.initialParams = initialParams;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Processus'.
	 * @return Long proId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "Processus")
	public Long getProId() {
		return (Long) proIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Processus'.
	 * @param proId Long <b>Obligatoire</b>
	 */
	public void setProId(final Long proId) {
		proIdAccessor.setId(proId);
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Node'.
	 * @return Long nodId
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "FOREIGN_KEY", label = "Node")
	public Long getNodId() {
		return (Long) nodIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Node'.
	 * @param nodId Long
	 */
	public void setNodId(final Long nodId) {
		nodIdAccessor.setId(nodId);
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'PlanificationState'.
	 * @return String sstCd
	 */
	@Field(domain = "DO_O_CODE_IDENTIFIANT", type = "FOREIGN_KEY", label = "PlanificationState")
	public String getSstCd() {
		return (String) sstCdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'PlanificationState'.
	 * @param sstCd String
	 */
	public void setSstCd(final String sstCd) {
		sstCdAccessor.setId(sstCd);
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
	public VAccessor<io.vertigo.orchestra.domain.definition.OProcess> processus() {
		return proIdAccessor;
	}

	@Deprecated
	public io.vertigo.orchestra.domain.definition.OProcess getProcessus() {
		// we keep the lazyness
		if (!proIdAccessor.isLoaded()) {
			proIdAccessor.load();
		}
		return proIdAccessor.get();
	}

	/**
	 * Retourne l'URI: Processus.
	 * @return URI de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.UID<io.vertigo.orchestra.domain.definition.OProcess> getProcessusURI() {
		return proIdAccessor.getURI();
	}

	/**
	 * Association : PlanificationState.
	 * @return l'accesseur vers la propriété 'PlanificationState'
	 */
	public VAccessor<io.vertigo.orchestra.domain.referential.OSchedulerState> planificationState() {
		return sstCdAccessor;
	}

	@Deprecated
	public io.vertigo.orchestra.domain.referential.OSchedulerState getPlanificationState() {
		// we keep the lazyness
		if (!sstCdAccessor.isLoaded()) {
			sstCdAccessor.load();
		}
		return sstCdAccessor.get();
	}

	/**
	 * Retourne l'URI: PlanificationState.
	 * @return URI de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.UID<io.vertigo.orchestra.domain.referential.OSchedulerState> getPlanificationStateURI() {
		return sstCdAccessor.getURI();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
