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
package io.vertigo.quarto.publisher.data.domain;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.DtObject;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données AbstractMisEnCause
 */
public final class MisEnCause implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyBoolean", label = "Sexe")
	private Boolean siHomme;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Nom")
	private String nom;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Prenom")
	private String prenom;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyDtAddress", cardinality = Cardinality.MANY, label = "Addresses connues")
	private io.vertigo.datamodel.structure.model.DtList<io.vertigo.quarto.publisher.data.domain.Address> adressesConnues;

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Sexe'.
	 * @return Boolean siHomme
	 */
	public final Boolean getSiHomme() {
		return siHomme;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Sexe'.
	 * @param siHomme Boolean
	 */
	public final void setSiHomme(final Boolean siHomme) {
		this.siHomme = siHomme;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Nom'.
	 * @return String nom
	 */
	public final String getNom() {
		return nom;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Nom'.
	 * @param nom String
	 */
	public final void setNom(final String nom) {
		this.nom = nom;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Prenom'.
	 * @return String prenom
	 */
	public final String getPrenom() {
		return prenom;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Prenom'.
	 * @param prenom String
	 */
	public final void setPrenom(final String prenom) {
		this.prenom = prenom;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Addresses connues'.
	 * @return DtList<io.vertigo.publisher.mock.Address> adressesConnues
	 */
	public final io.vertigo.datamodel.structure.model.DtList<io.vertigo.quarto.publisher.data.domain.Address> getAdressesConnues() {
		return adressesConnues;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Addresses connues'.
	 * @param adressesConnues DtList<io.vertigo.publisher.mock.Address>
	 */
	public final void setAdressesConnues(final io.vertigo.datamodel.structure.model.DtList<io.vertigo.quarto.publisher.data.domain.Address> adressesConnues) {
		this.adressesConnues = adressesConnues;
	}
}
