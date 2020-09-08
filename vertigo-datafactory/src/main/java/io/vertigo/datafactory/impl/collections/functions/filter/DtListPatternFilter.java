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
package io.vertigo.datafactory.impl.collections.functions.filter;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Predicate;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datafactory.impl.collections.functions.filter.DtListPatternFilterUtil.FilterPattern;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * Filtre de DtList prenant en entrée un String qui doit respecter certains patterns.
 * Syntaxes acceptées :
 * FIELD_NAME:VALUE => FilterByValue.
 *
 * FIELD_NAME:[MINVALUE TO MAXVALUE]
 * - Le min et max doivent être du même type.
 * - Le caractère * peut être utiliser pour indiquer qu'il n'y a pas de borne max ou min.
 * - Les accolades sont ouvrantes ou fermantes pour indiquer si la valeur est comprise ou non
 *
 * @author npiedeloup
 * @param <D> Type d'objet
 */
public final class DtListPatternFilter<D extends DtObject> implements Predicate<D>, Serializable {
	private static final long serialVersionUID = 6282972172196740177L;

	private final FilterPattern filterPattern;
	private final String[] parsedFilter;
	private Predicate<D> subDtListFilter;

	/**
	 * Constructor.
	 * @param filterString Chaine représentant le filtre
	 */
	public DtListPatternFilter(final String filterString) {
		Assertion.check().isNotBlank(filterString);
		//-----
		FilterPattern foundFilterPattern = null;
		String[] foundParsedFilter = null;

		//On test les patterns dans l'ordre
		for (final FilterPattern filterPatternTemp : FilterPattern.values()) {
			final Optional<String[]> parsedFilterOpt = DtListPatternFilterUtil.parseFilter(filterString, filterPatternTemp.getPattern());
			if (parsedFilterOpt.isPresent()) {
				foundFilterPattern = filterPatternTemp;
				foundParsedFilter = parsedFilterOpt.get();
				break;
			}
		}
		//On passe par des objets intermédiaires pour permettre le 'final' sur les attributs de la class
		Assertion.check().isTrue(foundFilterPattern != null && foundParsedFilter != null, "La chaine de filtrage ne respecte pas la syntaxe.\nFiltre: {0}.", filterString);
		this.filterPattern = foundFilterPattern;
		this.parsedFilter = foundParsedFilter;
	}

	/** {@inheritDoc} */
	@Override
	public boolean test(final D dto) {
		if (subDtListFilter == null) {
			subDtListFilter = DtListPatternFilterUtil.createDtListFilterForPattern(filterPattern, parsedFilter, DtObjectUtil.findDtDefinition(dto));
		}
		return subDtListFilter.test(dto);
	}
}
