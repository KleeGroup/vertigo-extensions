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
package io.vertigo.basics.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.task.definitions.TaskAttribute;

/**
 * Simule le mécanisme JSP sur un fichier texte quelconque.
 * Remplace les éléments compris entre les séparateurs par une évaluation dynamique.
 * <% %>  : permet d'insérer des blocs java
 * <%= %> : permet d'ajouter des éléments au texte
 *
 * @author  pchretien
 */
final class ScriptPreProcessor {
	private final ScriptManager scriptManager;
	private final Map<TaskAttribute, Object> inTaskAttributes;
	private final SeparatorType separatorType;

	/**
	 * Constructor.
	 * @param inTaskAttributes Map des paramètres
	 * @param separatorType Type de preprocessing CLASSIC ou HTML
	 */
	ScriptPreProcessor(final ScriptManager scriptManager, final Map<TaskAttribute, Object> inTaskAttributes, final SeparatorType separatorType) {
		Assertion.check()
				.isNotNull(scriptManager)
				.isNotNull(inTaskAttributes)
				.isNotNull(separatorType);
		//-----
		this.scriptManager = scriptManager;
		this.inTaskAttributes = inTaskAttributes;
		this.separatorType = separatorType;
	}

	private static List<ExpressionParameter> createParameters(final ScriptManager scriptManager, final Map<TaskAttribute, Object> inTaskAttributes) {
		Assertion.check()
				.isNotNull(scriptManager)
				.isNotNull(inTaskAttributes);
		//-----
		final List<ExpressionParameter> tmpParameters = new ArrayList<>(inTaskAttributes.size());

		//==========Initialisation des types et noms de paramètre==============
		ExpressionParameter scriptEvaluatorParameter;
		for (final Entry<TaskAttribute, Object> entry : inTaskAttributes.entrySet()) {
			final TaskAttribute taskAttribute = entry.getKey();
			final Class<?> clazz = taskAttribute.getTargetJavaClass();
			scriptEvaluatorParameter = ExpressionParameter.of(taskAttribute.getName(), clazz, entry.getValue());
			tmpParameters.add(scriptEvaluatorParameter);
		}
		return tmpParameters;

	}

	String evaluate(final String query) {
		//On commence par vérifier si le preprocessor s'applique.
		if (containsSeparator(query, separatorType.getSeparator())) {
			//Evaluation de la query à la mode JSP avec les paramètres passés au démarrage.
			return scriptManager.evaluateScript(query, separatorType, createParameters(scriptManager, inTaskAttributes));
		}
		return query;
	}

	private static boolean containsSeparator(final String query, final ScriptSeparator separator) {
		return query.contains(separator.getBeginSeparator())
				&& query.contains(separator.getEndSeparator());
	}
}
