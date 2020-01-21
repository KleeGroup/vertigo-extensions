package io.vertigo.orchestra.monitoring.domain.uidefinitions;

import io.vertigo.core.lang.Generated;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
@io.vertigo.dynamo.ngdomain.annotations.Mapper(clazz = io.vertigo.dynamo.domain.util.JsonMapper.class, dataType = io.vertigo.dynamo.domain.metamodel.DataType.String)
public final class OProcessUi implements DtObject {
	private static final long serialVersionUID = 1L;

	private Long proId;
	private String name;
	private String label;
	private String cronExpression;
	private String initialParams;
	private Boolean multiexecution;
	private Boolean active;
	private Integer rescuePeriod;
	private String metadatas;
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Id du processus'.
	 * @return Long proId <b>Obligatoire</b>
	 */
	@Field(domain = "STyOIdentifiant", cardinality = io.vertigo.core.lang.Cardinality.ONE, label = "Id du processus")
	public Long getProId() {
		return proId;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Id du processus'.
	 * @param proId Long <b>Obligatoire</b>
	 */
	public void setProId(final Long proId) {
		this.proId = proId;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Nom du processus'.
	 * @return String name
	 */
	@Field(domain = "STyOLibelle", label = "Nom du processus")
	public String getName() {
		return name;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Nom du processus'.
	 * @param name String
	 */
	public void setName(final String name) {
		this.name = name;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Libellé du processus'.
	 * @return String label
	 */
	@Field(domain = "STyOLibelle", label = "Libellé du processus")
	public String getLabel() {
		return label;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Libellé du processus'.
	 * @param label String
	 */
	public void setLabel(final String label) {
		this.label = label;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Expression récurrence du processus'.
	 * @return String cronExpression
	 */
	@Field(domain = "STyOLibelle", label = "Expression récurrence du processus")
	public String getCronExpression() {
		return cronExpression;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Expression récurrence du processus'.
	 * @param cronExpression String
	 */
	public void setCronExpression(final String cronExpression) {
		this.cronExpression = cronExpression;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Paramètres initiaux sous forme de JSON'.
	 * @return String initialParams
	 */
	@Field(domain = "STyOJsonText", label = "Paramètres initiaux sous forme de JSON")
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
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Accepte la multi-execution'.
	 * @return Boolean multiexecution
	 */
	@Field(domain = "STyOBooleen", label = "Accepte la multi-execution")
	public Boolean getMultiexecution() {
		return multiexecution;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Accepte la multi-execution'.
	 * @param multiexecution Boolean
	 */
	public void setMultiexecution(final Boolean multiexecution) {
		this.multiexecution = multiexecution;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Processus actif'.
	 * @return Boolean active <b>Obligatoire</b>
	 */
	@Field(domain = "STyOBooleen", cardinality = io.vertigo.core.lang.Cardinality.ONE, label = "Processus actif")
	public Boolean getActive() {
		return active;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Processus actif'.
	 * @param active Boolean <b>Obligatoire</b>
	 */
	public void setActive(final Boolean active) {
		this.active = active;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Temps de validité d'une planification'.
	 * @return Integer rescuePeriod <b>Obligatoire</b>
	 */
	@Field(domain = "STyONombre", cardinality = io.vertigo.core.lang.Cardinality.ONE, label = "Temps de validité d'une planification")
	public Integer getRescuePeriod() {
		return rescuePeriod;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Temps de validité d'une planification'.
	 * @param rescuePeriod Integer <b>Obligatoire</b>
	 */
	public void setRescuePeriod(final Integer rescuePeriod) {
		this.rescuePeriod = rescuePeriod;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Métadonnées du processus'.
	 * @return String metadatas
	 */
	@Field(domain = "STyOMetadatas", label = "Métadonnées du processus")
	public String getMetadatas() {
		return metadatas;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Métadonnées du processus'.
	 * @param metadatas String
	 */
	public void setMetadatas(final String metadatas) {
		this.metadatas = metadatas;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
