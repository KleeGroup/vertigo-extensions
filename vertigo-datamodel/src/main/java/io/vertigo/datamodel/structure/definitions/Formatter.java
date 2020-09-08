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
package io.vertigo.datamodel.structure.definitions;

import io.vertigo.core.lang.BasicType;

/**
 * Permet de définir des formats particuliers d'affichages et de saisie.
 *
 * La remontée des erreurs est asymétrique :
 * - stringToValue remonte une exception typée, qui est interceptée et présentée en erreur utilisateur
 *
 * @author pchretien
 */
public interface Formatter {

	/**
	 * Transforme une valeur typée en String.
	 * @param objValue Valeur typée
	 * @param dataType Type
	 * @return  chaine formattée
	 */
	String valueToString(Object objValue, BasicType dataType);

	/**
	 * Transforme une String en valeur typée.
	 * @param strValue chaine saisie
	 * @param dataType Type
	 * @return  Valeur typée (déformattage)
	 * @throws FormatterException Erreur de parsing
	 */
	Object stringToValue(String strValue, BasicType dataType) throws FormatterException;
}
