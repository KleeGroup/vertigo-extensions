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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.attoparser.AbstractMarkupHandler;
import org.attoparser.MarkupParser;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.dom.Element;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templateresource.ITemplateResource;

import io.vertigo.core.lang.Assertion;

public class NamedComponentParser extends AbstractMarkupHandler {

	private static final Logger LOG = LogManager.getLogger(NamedComponentParser.class);

	protected static final String NAME_ATTRIBUTE = "alias";
	protected static final String SELECTOR_ATTRIBUTE = "selector";
	protected static final String FRAGMENT_ATTRIBUTE = "fragment";

	private final VuiResourceTemplateResolver componentResolver;

	protected final String dialectPrefix;

	private List<Element> elements;
	private Element currentElement;

	public NamedComponentParser(final String dialectPrefix, final VuiResourceTemplateResolver componentResolver) {
		this.dialectPrefix = dialectPrefix;
		this.componentResolver = componentResolver;
	}

	public Set<NamedComponentDefinition> parseComponent(final String componentName) {
		final Set<NamedComponentDefinition> components = new HashSet<>();

		final ITemplateResource templateResource = componentResolver.resolveResource("components/" + componentName);
		for (final Element element : parseElements(templateResource)) {
			if (isThymeleafComponent(element)) {
				components.addAll(createComponent(element, componentName));
			}
		}
		return components;
	}

	@Override
	public void handleAttribute(final char[] buffer, final int nameOffset, final int nameLen,
			final int nameLine, final int nameCol, final int operatorOffset, final int operatorLen,
			final int operatorLine, final int operatorCol, final int valueContentOffset,
			final int valueContentLen, final int valueOuterOffset, final int valueOuterLen,
			final int valueLine, final int valueCol) throws ParseException {
		final String attributeName = new String(buffer, nameOffset, nameLen);
		final String attributeValue = new String(buffer, valueContentOffset, valueContentLen);
		if (currentElement != null) {
			currentElement.addAttribute(attributeName, attributeValue);
		}
	}

	@Override
	public void handleOpenElementStart(final char[] buffer, final int nameOffset,
			final int nameLen, final int line, final int col) throws ParseException {
		final String attributeName = new String(buffer, nameOffset, nameLen);
		currentElement = new Element(attributeName);
		elements.add(currentElement);
	}

	private List<Element> parseElements(final ITemplateResource templateResource) {
		elements = new ArrayList<>();

		try (Reader reader = templateResource.reader()) {
			final ParseConfiguration config = ParseConfiguration.htmlConfiguration();

			final ParseConfiguration autoCloseConfig = ParseConfiguration.htmlConfiguration();
			autoCloseConfig.setElementBalancing(
					ParseConfiguration.ElementBalancing.AUTO_OPEN_CLOSE);

			final MarkupParser htmlStandardParser = new MarkupParser(config);
			htmlStandardParser.parse(reader, this);

		} catch (IOException | ParseException e) {
			LOG.error("Error while parsing elements", e);
		}

		return elements;
	}

	private Set<NamedComponentDefinition> createComponent(final Element element, final String componentName) {
		final String fragmentAttribute = getDynamicAttributeValue(element, StandardDialect.PREFIX, FRAGMENT_ATTRIBUTE);
		final Pattern parametersPattern = Pattern.compile("^\\s*([^(]+)\\s*(?:\\((.*)\\))?");
		final Matcher matcher = parametersPattern.matcher(fragmentAttribute);
		final boolean matches = matcher.matches();
		Assertion.check().isTrue(matches, "fragment '{0}' should match 'xxx' or 'xxx(params, ...)'", fragmentAttribute);
		//----
		final String frag = matcher.group(1);
		final Optional<String> parametersOpt = Optional.ofNullable(matcher.group(2));

		String name = getDynamicAttributeValue(element, dialectPrefix, NAME_ATTRIBUTE);
		if (name == null) {
			name = frag;
		}

		final String selector = getDynamicAttributeValue(element, dialectPrefix, SELECTOR_ATTRIBUTE);
		if (selector != null && !selector.isEmpty()) {
			final Set<NamedComponentDefinition> thymeleafComponents = new HashSet<>();
			thymeleafComponents.add(new NamedComponentDefinition(name, "components/" + componentName + ".html", selector, parametersOpt, frag));
			thymeleafComponents.add(new NamedComponentDefinition(frag, "components/" + componentName + ".html", parametersOpt, frag)); //Fragment always accessible without selector
			return thymeleafComponents;
		}
		return Collections.singleton(new NamedComponentDefinition(name, "components/" + componentName + ".html", parametersOpt, frag));
	}

	private static boolean isThymeleafComponent(final Element element) {
		return hasDynamicAttribute(element, StandardDialect.PREFIX, FRAGMENT_ATTRIBUTE);
	}

	private static boolean hasDynamicAttribute(final Element element, final String prefix, final String dynamicAttribute) {
		return element.hasAttribute("data-" + prefix + "-" + dynamicAttribute)
				|| element.hasAttribute(prefix + ":" + dynamicAttribute);
	}

	private static String getDynamicAttributeValue(final Element element, final String prefix, final String dynamicAttribute) {
		final String value = element.getAttributeValue("data-" + prefix + "-" + dynamicAttribute);
		if (value != null) {
			return value;
		}
		return element.getAttributeValue(prefix + ":" + dynamicAttribute);
	}

}
