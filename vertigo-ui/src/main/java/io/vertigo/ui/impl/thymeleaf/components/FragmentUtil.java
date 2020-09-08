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
package io.vertigo.ui.impl.thymeleaf.components;

import java.util.Map;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.expression.Fragment;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.standard.expression.FragmentExpression.ExecutedFragmentExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.NoOpToken;
import org.thymeleaf.standard.expression.StandardExpressions;

public final class FragmentUtil {

	private FragmentUtil() {
		//private constructor for Helper
	}

	public static IModel getFragmentModel(final ITemplateContext context,
			final String attributeValue,
			final IElementModelStructureHandler structureHandler) {

		final Object fragmentObj = computeFragment(context, attributeValue);
		if (fragmentObj == null) {

			// If the Fragment result is null, this is an error. Note a NULL result is not the same as the result being the empty fragment
			// (~{})
			throw new TemplateInputException("Error resolving fragment: \"" + attributeValue + "\": " + "template or fragment could not be resolved");
		}

		final Fragment fragment = (Fragment) fragmentObj;

		final TemplateModel fragmentModel = fragment.getTemplateModel();
		final Map<String, Object> fragmentParameters = fragment.getParameters();

		/*
		 * APPLY THE FRAGMENT'S TEMPLATE RESOLUTION so that all code inside the fragment is executed with its own template resolution info (working
		 * as if it were a local variable)
		 */
		final TemplateData fragmentTemplateData = fragmentModel.getTemplateData();
		structureHandler.setTemplateData(fragmentTemplateData);

		/*
		 * APPLY THE FRAGMENT PARAMETERS AS LOCAL VARIABLES, perhaps after reshaping it according to the fragment signature
		 */
		if (fragmentParameters != null && fragmentParameters.size() > 0) {
			for (final Map.Entry<String, Object> fragmentParameterEntry : fragmentParameters.entrySet()) {
				structureHandler.setLocalVariable(fragmentParameterEntry.getKey(), fragmentParameterEntry.getValue());
			}
		}
		return fragmentModel;
	}

	private static Object computeFragment(final ITemplateContext context, final String input) {
		final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(context.getConfiguration());
		final FragmentExpression fragmentExpression = (FragmentExpression) expressionParser.parseExpression(context, input.trim());

		final ExecutedFragmentExpression executedFragmentExpression = FragmentExpression.createExecutedFragmentExpression(context, fragmentExpression);

		if (executedFragmentExpression.getFragmentSelectorExpressionResult() == null && executedFragmentExpression.getFragmentParameters() == null) {
			// We might be in the scenario that what we thought was a template name in fact was instead an expression
			// returning a Fragment itself, so we should simply return it
			final Object templateNameExpressionResult = executedFragmentExpression.getTemplateNameExpressionResult();
			if (templateNameExpressionResult != null) {
				if (templateNameExpressionResult instanceof Fragment) {
					return templateNameExpressionResult;
				}
				if (templateNameExpressionResult == NoOpToken.VALUE) {
					return NoOpToken.VALUE;
				}
			}
		}

		// Given this is a simple (originally unwrapped) fragment expression, we will consider the non-existence
		// of the fragment a failure. The reason we do this here instead of just waiting and seeing if we receive
		// a null and then failing is that, in order to receive such "null", the underlying resolution system would
		// have to execute a (potentially costly) resource.exists() call on the resolved resource.
		return FragmentExpression.resolveExecutedFragmentExpression(context, executedFragmentExpression, true);
	}
}
