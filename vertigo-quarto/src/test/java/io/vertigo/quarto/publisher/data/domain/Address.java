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
 * Objet de données AbstractAddress
 */
public final class Address implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Rue")
	private String rue;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyDtVille", label = "Ville")
	private io.vertigo.quarto.publisher.data.domain.Ville ville;

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Rue'.
	 * @return String rue
	 */
	public final String getRue() {
		return rue;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Rue'.
	 * @param rue String
	 */
	public final void setRue(final String rue) {
		this.rue = rue;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Ville'.
	 * @return io.vertigo.publisher.mock.Ville ville
	 */
	public final io.vertigo.quarto.publisher.data.domain.Ville getVille() {
		return ville;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Ville'.
	 * @param ville io.vertigo.publisher.mock.Ville
	 */
	public final void setVille(final io.vertigo.quarto.publisher.data.domain.Ville ville) {
		this.ville = ville;
	}
}
