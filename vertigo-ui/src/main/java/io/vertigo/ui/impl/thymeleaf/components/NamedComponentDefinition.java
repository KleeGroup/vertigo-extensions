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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.thymeleaf.standard.expression.VariableExpression;

import io.vertigo.core.lang.Assertion;

public final class NamedComponentDefinition {

	private String name;
	private String fragmentTemplate;
	private final Optional<VariableExpression> selectionExpressionOpt;
	private final List<String> parameters;
	private final String frag;

	public NamedComponentDefinition(final String name, final String fragmentTemplate, final String selectionExpression, final Optional<String> parameters, final String frag) {
		Assertion.check()
				.isNotBlank(name)
				.isNotBlank(fragmentTemplate)
				.isNotBlank(selectionExpression)
				.isTrue(
						selectionExpression.startsWith("${") && selectionExpression.endsWith("}"),
						"Component {0} selector expression must starts with $\\{ and ends with \\} ({1})", name, selectionExpression);
		Assertion.check().isNotBlank(frag);
		//-----
		this.name = name;
		this.fragmentTemplate = fragmentTemplate;
		final String trimmedSelectionExpression = selectionExpression.substring(2, selectionExpression.length() - 1);
		this.selectionExpressionOpt = Optional.of(new VariableExpression(trimmedSelectionExpression));
		this.parameters = splitAsSet(parameters);
		this.frag = frag;
	}

	public NamedComponentDefinition(final String name, final String fragmentTemplate, final Optional<String> parameters, final String frag) {
		Assertion.check()
				.isNotBlank(name)
				.isNotBlank(fragmentTemplate)
				.isNotBlank(frag);
		//-----
		this.name = name;
		this.fragmentTemplate = fragmentTemplate;
		selectionExpressionOpt = Optional.empty();
		this.parameters = splitAsSet(parameters);
		this.frag = frag;
	}

	private static List<String> splitAsSet(final Optional<String> parameters) {
        return parameters.map(s -> Arrays.asList(s.split("\\s*,\\s*"))).orElse(Collections.emptyList());
    }

	/**
	 * Returns the name of the component (e.g. panel)
	 *
	 * @return Component name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the component (e.g. panel). This will be uses as the
	 * selector in the html file like.
	 *
	 * @param name
	 *            Component name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Returns the thymeleaf fragment template
	 *
	 * @return Fragment template
	 */
	public String getFragmentTemplate() {
		return fragmentTemplate;
	}

	public Optional<VariableExpression> getSelectionExpression() {
		return selectionExpressionOpt;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public String getFrag() {
		return frag;
	}

	/**
	 * Sets the thymeleaf fragment template (e.g components/panel :: panel). The
	 * pattern is without the fragments parameters, so 'components/panel ::
	 * panel(title)' will throw an error.
	 *
	 * @param fragmentTemplate
	 *            Fragment template
	 */
	public void setFragmentTemplate(final String fragmentTemplate) {
		this.fragmentTemplate = fragmentTemplate;
	}

	@Override
	public String toString() {
		return "Component [name=" + name + ", fragmentTemplate=" + fragmentTemplate + "]";
	}

}
