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

import java.time.LocalDate;

import io.vertigo.core.lang.Cardinality;
import io.vertigo.datamodel.structure.model.DtObject;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données AbstractPublisherMock
 */
public final class PublisherMock implements DtObject {
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Titre")
	private String titre;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Nom")
	private String nom;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Prénom")
	private String prenom;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Adresse (multi ligne)")
	private String address;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Commentaire (multi paragraphe)")
	private String commentaire;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyBoolean", label = "booleen 1")
	private Boolean boolean1;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyBoolean", label = "booleen 2")
	private Boolean boolean2;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyBoolean", label = "booleen 3")
	private Boolean boolean3;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyString", label = "Test champs inutilisé")
	private String testDummy;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyLong", label = "Test long")
	private Long testLong;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyDouble", label = "Test double")
	private Double testDouble;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyInteger", label = "Test integer")
	private Integer testInteger;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyDate", label = "Test date")
	private LocalDate testDate;
	@io.vertigo.datamodel.structure.stereotype.Field(smartType = "STyDtPublisherMock", cardinality = Cardinality.MANY, label = "Test list")
	private io.vertigo.datamodel.structure.model.DtList<io.vertigo.quarto.publisher.data.domain.PublisherMock> dtcTest;

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Titre'.
	 * @return String titre
	 */
	public final String getTitre() {
		return titre;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Titre'.
	 * @param titre String
	 */
	public final void setTitre(final String titre) {
		this.titre = titre;
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
	 * Récupère la valeur de la propriété 'Prénom'.
	 * @return String prenom
	 */
	public final String getPrenom() {
		return prenom;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Prénom'.
	 * @param prenom String
	 */
	public final void setPrenom(final String prenom) {
		this.prenom = prenom;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Adresse (multi ligne)'.
	 * @return String address
	 */
	public final String getAddress() {
		return address;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Adresse (multi ligne)'.
	 * @param address String
	 */
	public final void setAddress(final String address) {
		this.address = address;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Commentaire (multi paragraphe)'.
	 * @return String commentaire
	 */
	public final String getCommentaire() {
		return commentaire;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Commentaire (multi paragraphe)'.
	 * @param commentaire String
	 */
	public final void setCommentaire(final String commentaire) {
		this.commentaire = commentaire;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'booleen 1'.
	 * @return Boolean boolean1
	 */
	public final Boolean getBoolean1() {
		return boolean1;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'booleen 1'.
	 * @param boolean1 Boolean
	 */
	public final void setBoolean1(final Boolean boolean1) {
		this.boolean1 = boolean1;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'booleen 2'.
	 * @return Boolean boolean2
	 */
	public final Boolean getBoolean2() {
		return boolean2;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'booleen 2'.
	 * @param boolean2 Boolean
	 */
	public final void setBoolean2(final Boolean boolean2) {
		this.boolean2 = boolean2;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'booleen 3'.
	 * @return Boolean boolean3
	 */
	public final Boolean getBoolean3() {
		return boolean3;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'booleen 3'.
	 * @param boolean3 Boolean
	 */
	public final void setBoolean3(final Boolean boolean3) {
		this.boolean3 = boolean3;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Test champs inutilisé'.
	 * @return String testDummy
	 */
	public final String getTestDummy() {
		return testDummy;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Test champs inutilisé'.
	 * @param testDummy String
	 */
	public final void setTestDummy(final String testDummy) {
		this.testDummy = testDummy;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Test long'.
	 * @return Long testLong
	 */
	public final Long getTestLong() {
		return testLong;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Test long'.
	 * @param testLong Long
	 */
	public final void setTestLong(final Long testLong) {
		this.testLong = testLong;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Test double'.
	 * @return Double testDouble
	 */
	public final Double getTestDouble() {
		return testDouble;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Test double'.
	 * @param testDouble Double
	 */
	public final void setTestDouble(final Double testDouble) {
		this.testDouble = testDouble;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Test integer'.
	 * @return Integer testInteger
	 */
	public final Integer getTestInteger() {
		return testInteger;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Test integer'.
	 * @param testInteger Integer
	 */
	public final void setTestInteger(final Integer testInteger) {
		this.testInteger = testInteger;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Test date'.
	 * @return java.util.Date testDate
	 */
	public final LocalDate getTestDate() {
		return testDate;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Test date'.
	 * @param testDate java.util.Date
	 */
	public final void setTestDate(final LocalDate testDate) {
		this.testDate = testDate;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Test list'.
	 * @return DtList<io.vertigo.publisher.mock.PublisherMock> dtcTest
	 */
	public final io.vertigo.datamodel.structure.model.DtList<io.vertigo.quarto.publisher.data.domain.PublisherMock> getDtcTest() {
		return dtcTest;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Test list'.
	 * @param dtcTest DtList<io.vertigo.publisher.mock.PublisherMock>
	 */
	public final void setDtcTest(final io.vertigo.datamodel.structure.model.DtList<io.vertigo.quarto.publisher.data.domain.PublisherMock> dtcTest) {
		this.dtcTest = dtcTest;
	}
}
