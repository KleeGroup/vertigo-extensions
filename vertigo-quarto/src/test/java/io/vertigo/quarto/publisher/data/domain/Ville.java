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

import io.vertigo.datamodel.structure.model.DtObject;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données AbstractVille
 */
public final class Ville implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Nom")
	private String nom;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Code postal")
	private String codePostal;

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
	 * Récupère la valeur de la propriété 'Code postal'.
	 * @return String codePostal
	 */
	public final String getCodePostal() {
		return codePostal;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Code postal'.
	 * @param codePostal String
	 */
	public final void setCodePostal(final String codePostal) {
		this.codePostal = codePostal;
	}
}
