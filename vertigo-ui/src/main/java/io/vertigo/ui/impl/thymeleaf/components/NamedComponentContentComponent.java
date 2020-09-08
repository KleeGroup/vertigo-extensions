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

import java.util.HashMap;
import java.util.Map;

import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.standard.StandardDialect;

import io.vertigo.core.lang.Assertion;

public final class NamedComponentContentComponent {

	private final IModel innerModel;
	private final String name;
	private final boolean standaloneTag;
	private final boolean openTag;
	private final boolean closeTag;
	private final Map<String, String> attributes;

	public NamedComponentContentComponent(final IModel innerModel) {
		Assertion.check().isNotNull(innerModel);
		//-----
		this.innerModel = innerModel;
		final IElementTag firstLevelTag = (IElementTag) innerModel.get(0);
		name = firstLevelTag.getElementDefinition().getElementName().getElementName();
		standaloneTag = firstLevelTag instanceof IStandaloneElementTag;
		openTag = firstLevelTag instanceof IOpenElementTag;
		closeTag = firstLevelTag instanceof ICloseElementTag;
		attributes = readAttributes(innerModel);
	}

	/**
	 * Returns the name of the first component
	 *
	 * @return Component name
	 */
	public String getName() {
		return name;
	}

	public boolean isTag(final String testedName) {
		return name.equals(testedName);
	}
	//
	//	public boolean isElement() {
	//		return elementTag;
	//	}

	public boolean isStandalone() {
		return standaloneTag;
	}

	public boolean isOpenTag() {
		return openTag;
	}

	public boolean isCloseTag() {
		return closeTag;
	}

	/**
	 * Returns the thymeleaf Model.
	 * @return IModel inner model
	 */
	public IModel getModel() {
		return innerModel;
	}

	public String getAttribute(final String attributeName) {
		return attributes.get(attributeName);
	}

	@Override
	public String toString() {
		return innerModel.toString();
	}

	private static Map<String, String> readAttributes(final IModel model) {
		final ITemplateEvent firstEvent = model.get(0);
		final Map<String, String> attributes = new HashMap<>();

		if (firstEvent instanceof IProcessableElementTag) {
			final IProcessableElementTag processableElementTag = (IProcessableElementTag) firstEvent;
			for (final IAttribute attribute : processableElementTag.getAllAttributes()) {
				final String completeName = attribute.getAttributeCompleteName();
				if (!isDynamicAttribute(completeName, StandardDialect.PREFIX)) {
					attributes.put(completeName, attribute.getValue());
				}
			}
		}
		return attributes;
	}

	private static boolean isDynamicAttribute(final String attribute, final String prefix) {
		return attribute.startsWith(prefix + ":") || attribute.startsWith("data-" + prefix + "-");
	}
}
