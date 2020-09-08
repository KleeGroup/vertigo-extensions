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
package io.vertigo.datamodel.impl.smarttype.dynamic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.definition.DefinitionSpace;

/**
 * Solver permet de résoudre les références.
 * Les références peuvent être orphelines : la clé ne correspond à aucune définition.
 * Les références circulaires ne peuvent pas être résolues.
 * Le solver est une fonction stateless qui prend en entrée le Repository du Model et calcule en sortie la liste des définitions.
 *
 * @author  pchretien
 */
public final class DynamicDefinitionSolver {
	private DynamicDefinitionSolver() {
		//private constructor
	}

	/**
	* Résoltuion des références.
	* On appelle SyntaxHandler dans le bon Ordre
	*/
	public static List<DynamicDefinition> solve(final DefinitionSpace definitionSpace, final Map<String, DynamicDefinition> definitionRepository) {
		Assertion.check()
				.isNotNull(definitionSpace)
				.isNotNull(definitionRepository);
		//-----
		//Liste des clés résolues
		final List<DynamicDefinition> sortedList = new ArrayList<>();

		//-----
		final Collection<DynamicDefinition> coll = new ArrayList<>(definitionRepository.values());

		DynamicDefinition dslDefinition;
		int size = coll.size();
		while (size > 0) {
			for (final Iterator<DynamicDefinition> it = coll.iterator(); it.hasNext();) {
				dslDefinition = it.next();
				//==============================================================
				//==============================================================
				//On vérifie que les sous éléments sont résolues
				if (isSolved(definitionSpace, definitionRepository, sortedList, dslDefinition, dslDefinition)) {
					sortedList.add(dslDefinition);
					it.remove();
				}
			}
			//Si la liste n'a pas diminuée c'est que l'on a fini de résoudre ce qui peut l'être.
			if (size == coll.size()) {
				throw new VSystemException(" Les références {0} ne peuvent être résolues", coll);
			}
			size = coll.size();
		}
		return sortedList;
	}

	private static boolean isSolved(
			final DefinitionSpace definitionSpace,
			final Map<String, DynamicDefinition> definitionRepository,
			final List<DynamicDefinition> orderedList,
			final DynamicDefinition dslDefinition,
			final DynamicDefinition xdefRoot) {
		//We check all references were known
		for (final String definitionName : dslDefinition.getDefinitionLinkNames()) {
			//reference should be already solved in a previous resources module : then continue
			if (!definitionSpace.contains(definitionName)) {
				//or references should be in currently parsed resources
				if (!definitionRepository.containsKey(definitionName)) {
					throw new VSystemException("Defintion {0} référencée par la definition {1} non trouvée",
							definitionName, dslDefinition.getName());
				}
				final DynamicDefinition linkedDefinition = definitionRepository.get(definitionName);
				if (!orderedList.contains(linkedDefinition)) {
					return false;
				}
			}
		}
		return true;
	}
}
