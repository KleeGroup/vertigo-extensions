/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.workflow.domain.model;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.EnumVAccessor;
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
public final class WfActivityDefinition implements Entity {
	private static final long serialVersionUID = 1L;

	private Long wfadId;
	private String name;
	private Integer level;

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_WFAD_WFMD",
			fkFieldName = "WFMD_CODE",
			primaryDtDefinitionName = "DT_WF_MULTIPLICITY_DEFINITION",
			primaryIsNavigable = true,
			primaryRole = "WfMultiplicityDefinition",
			primaryLabel = "WfMultiplicityDefinition",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_WF_ACTIVITY_DEFINITION",
			foreignIsNavigable = false,
			foreignRole = "WfActivityDefinition",
			foreignLabel = "WfActivityDefinition",
			foreignMultiplicity = "0..*")
	private final EnumVAccessor<io.vertigo.workflow.domain.model.WfMultiplicityDefinition, io.vertigo.workflow.domain.model.WfMultiplicityDefinitionEnum> wfmdCodeAccessor = new EnumVAccessor<>(io.vertigo.workflow.domain.model.WfMultiplicityDefinition.class, "WfMultiplicityDefinition", io.vertigo.workflow.domain.model.WfMultiplicityDefinitionEnum.class);

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_WFWD_WFAD_CURRENT",
			fkFieldName = "WFWD_ID",
			primaryDtDefinitionName = "DT_WF_WORKFLOW_DEFINITION",
			primaryIsNavigable = true,
			primaryRole = "WfWorkflowDefinition",
			primaryLabel = "WfWorkflowDefinition",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_WF_ACTIVITY_DEFINITION",
			foreignIsNavigable = false,
			foreignRole = "WfActivityDefinition",
			foreignLabel = "WfActivityDefinition",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.workflow.domain.model.WfWorkflowDefinition> wfwdIdAccessor = new VAccessor<>(io.vertigo.workflow.domain.model.WfWorkflowDefinition.class, "WfWorkflowDefinition");

	/** {@inheritDoc} */
	@Override
	public UID<WfActivityDefinition> getUID() {
		return UID.of(this);
	}
	
	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id Activity Definition'.
	 * @return Long wfadId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_WF_ID", type = "ID", required = true, label = "Id Activity Definition")
	public Long getWfadId() {
		return wfadId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id Activity Definition'.
	 * @param wfadId Long <b>Obligatoire</b>
	 */
	public void setWfadId(final Long wfadId) {
		this.wfadId = wfadId;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'name'.
	 * @return String name
	 */
	@Field(domain = "DO_WF_LABEL", label = "name")
	public String getName() {
		return name;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'name'.
	 * @param name String
	 */
	public void setName(final String name) {
		this.name = name;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'level'.
	 * @return Integer level
	 */
	@Field(domain = "DO_WF_LEVEL", label = "level")
	public Integer getLevel() {
		return level;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'level'.
	 * @param level Integer
	 */
	public void setLevel(final Integer level) {
		this.level = level;
	}
	
	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'WfMultiplicityDefinition'.
	 * @return String wfmdCode
	 */
	@Field(domain = "DO_WF_CODE", type = "FOREIGN_KEY", label = "WfMultiplicityDefinition")
	public String getWfmdCode() {
		return (String) wfmdCodeAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'WfMultiplicityDefinition'.
	 * @param wfmdCode String
	 */
	public void setWfmdCode(final String wfmdCode) {
		wfmdCodeAccessor.setId(wfmdCode);
	}
	
	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'WfWorkflowDefinition'.
	 * @return Long wfwdId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_WF_ID", type = "FOREIGN_KEY", required = true, label = "WfWorkflowDefinition")
	public Long getWfwdId() {
		return (Long) wfwdIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'WfWorkflowDefinition'.
	 * @param wfwdId Long <b>Obligatoire</b>
	 */
	public void setWfwdId(final Long wfwdId) {
		wfwdIdAccessor.setId(wfwdId);
	}

 	/**
	 * Association : WfMultiplicityDefinition.
	 * @return l'accesseur vers la propriété 'WfMultiplicityDefinition'
	 */
	public EnumVAccessor<io.vertigo.workflow.domain.model.WfMultiplicityDefinition, io.vertigo.workflow.domain.model.WfMultiplicityDefinitionEnum> wfMultiplicityDefinition() {
		return wfmdCodeAccessor;
	}

 	/**
	 * Association : WfWorkflowDefinition.
	 * @return l'accesseur vers la propriété 'WfWorkflowDefinition'
	 */
	public VAccessor<io.vertigo.workflow.domain.model.WfWorkflowDefinition> wfWorkflowDefinition() {
		return wfwdIdAccessor;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
