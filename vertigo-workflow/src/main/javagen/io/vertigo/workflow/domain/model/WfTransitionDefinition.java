package io.vertigo.workflow.domain.model;

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
public final class WfTransitionDefinition implements Entity {
	private static final long serialVersionUID = 1L;

	private Long wftdId;
	private String name;

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_WFWD_WFTD",
			fkFieldName = "WFWD_ID",
			primaryDtDefinitionName = "DT_WF_WORKFLOW_DEFINITION",
			primaryIsNavigable = false,
			primaryRole = "WfWorkflowDefinition",
			primaryLabel = "WfWorkflowDefinition",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_WF_TRANSITION_DEFINITION",
			foreignIsNavigable = true,
			foreignRole = "WfTransitionDefinition",
			foreignLabel = "WfTransitionDefinition",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.workflow.domain.model.WfWorkflowDefinition> wfwdIdAccessor = new VAccessor<>(io.vertigo.workflow.domain.model.WfWorkflowDefinition.class, "WfWorkflowDefinition");

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_WFT_WFA_FROM",
			fkFieldName = "WFAD_ID_FROM",
			primaryDtDefinitionName = "DT_WF_ACTIVITY_DEFINITION",
			primaryIsNavigable = true,
			primaryRole = "TransitionFrom",
			primaryLabel = "transitionFrom",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_WF_TRANSITION_DEFINITION",
			foreignIsNavigable = false,
			foreignRole = "WfTransitionDefinition",
			foreignLabel = "WfTransitionDefinition",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.workflow.domain.model.WfActivityDefinition> wfadIdFromAccessor = new VAccessor<>(io.vertigo.workflow.domain.model.WfActivityDefinition.class, "TransitionFrom");

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_WFT_WFA_TO",
			fkFieldName = "WFAD_ID_TO",
			primaryDtDefinitionName = "DT_WF_ACTIVITY_DEFINITION",
			primaryIsNavigable = true,
			primaryRole = "TransitionTo",
			primaryLabel = "transitionTo",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_WF_TRANSITION_DEFINITION",
			foreignIsNavigable = false,
			foreignRole = "WfTransitionDefinition",
			foreignLabel = "WfTransitionDefinition",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.workflow.domain.model.WfActivityDefinition> wfadIdToAccessor = new VAccessor<>(io.vertigo.workflow.domain.model.WfActivityDefinition.class, "TransitionTo");

	/** {@inheritDoc} */
	@Override
	public UID<WfTransitionDefinition> getUID() {
		return UID.of(this);
	}
	
	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id Transition Definition'.
	 * @return Long wftdId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_WF_ID", type = "ID", required = true, label = "Id Transition Definition")
	public Long getWftdId() {
		return wftdId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id Transition Definition'.
	 * @param wftdId Long <b>Obligatoire</b>
	 */
	public void setWftdId(final Long wftdId) {
		this.wftdId = wftdId;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'name'.
	 * @return String name <b>Obligatoire</b>
	 */
	@Field(domain = "DO_WF_LABEL", required = true, label = "name")
	public String getName() {
		return name;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'name'.
	 * @param name String <b>Obligatoire</b>
	 */
	public void setName(final String name) {
		this.name = name;
	}
	
	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'WfWorkflowDefinition'.
	 * @return Long wfwdId
	 */
	@Field(domain = "DO_WF_ID", type = "FOREIGN_KEY", label = "WfWorkflowDefinition")
	public Long getWfwdId() {
		return (Long)  wfwdIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'WfWorkflowDefinition'.
	 * @param wfwdId Long
	 */
	public void setWfwdId(final Long wfwdId) {
		wfwdIdAccessor.setId(wfwdId);
	}
	
	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'transitionFrom'.
	 * @return Long wfadIdFrom <b>Obligatoire</b>
	 */
	@Field(domain = "DO_WF_ID", type = "FOREIGN_KEY", required = true, label = "transitionFrom")
	public Long getWfadIdFrom() {
		return (Long)  wfadIdFromAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'transitionFrom'.
	 * @param wfadIdFrom Long <b>Obligatoire</b>
	 */
	public void setWfadIdFrom(final Long wfadIdFrom) {
		wfadIdFromAccessor.setId(wfadIdFrom);
	}
	
	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'transitionTo'.
	 * @return Long wfadIdTo <b>Obligatoire</b>
	 */
	@Field(domain = "DO_WF_ID", type = "FOREIGN_KEY", required = true, label = "transitionTo")
	public Long getWfadIdTo() {
		return (Long)  wfadIdToAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'transitionTo'.
	 * @param wfadIdTo Long <b>Obligatoire</b>
	 */
	public void setWfadIdTo(final Long wfadIdTo) {
		wfadIdToAccessor.setId(wfadIdTo);
	}

 	/**
	 * Association : transitionFrom.
	 * @return l'accesseur vers la propriété 'transitionFrom'
	 */
	public VAccessor<io.vertigo.workflow.domain.model.WfActivityDefinition> transitionFrom() {
		return wfadIdFromAccessor;
	}
	
	@Deprecated
	public io.vertigo.workflow.domain.model.WfActivityDefinition getTransitionFrom() {
		// we keep the lazyness
		if (!wfadIdFromAccessor.isLoaded()) {
			wfadIdFromAccessor.load();
		}
		return wfadIdFromAccessor.get();
	}

	/**
	 * Retourne l'UID: transitionFrom.
	 * @return UID de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.UID<io.vertigo.workflow.domain.model.WfActivityDefinition> getTransitionFromUID() {
		return wfadIdFromAccessor.getUID();
	}

 	/**
	 * Association : transitionTo.
	 * @return l'accesseur vers la propriété 'transitionTo'
	 */
	public VAccessor<io.vertigo.workflow.domain.model.WfActivityDefinition> transitionTo() {
		return wfadIdToAccessor;
	}
	
	@Deprecated
	public io.vertigo.workflow.domain.model.WfActivityDefinition getTransitionTo() {
		// we keep the lazyness
		if (!wfadIdToAccessor.isLoaded()) {
			wfadIdToAccessor.load();
		}
		return wfadIdToAccessor.get();
	}

	/**
	 * Retourne l'UID: transitionTo.
	 * @return UID de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.UID<io.vertigo.workflow.domain.model.WfActivityDefinition> getTransitionToUID() {
		return wfadIdToAccessor.getUID();
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
