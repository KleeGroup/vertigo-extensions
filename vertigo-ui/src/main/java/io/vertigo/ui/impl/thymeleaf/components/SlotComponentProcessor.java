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

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

public class SlotComponentProcessor extends AbstractElementModelProcessor {

	public static final String SLOT_CONTENT_VAR_NAME = "slots";
	private static final String CONTENT_TAG_NAME = "slot";
	private static final int PRECEDENCE = 450;

	/**
	 * Constructor
	 *
	 * @param dialectPrefix Dialect prefix (vu)
	 */
	public SlotComponentProcessor(final String dialectPrefix) {
		super(TemplateMode.HTML, dialectPrefix, CONTENT_TAG_NAME, true, null, false, PRECEDENCE);
	}

	@Override
	protected void doProcess(final ITemplateContext context, final IModel model, final IElementModelStructureHandler structureHandler) {
		final Map<String, String> attributes = processAttribute(model);
		final String name = attributes.get("name");
		throw new IllegalStateException("Component slot " + name + " wasn't correctly parsed. All slots must be set at start in component body, before the content.");
	}

	private static Map<String, String> processAttribute(final IModel model) {
		final ITemplateEvent firstEvent = model.get(0);
		final Map<String, String> attributes = new HashMap<>();
		if (firstEvent instanceof IProcessableElementTag) {
			final IProcessableElementTag processableElementTag = (IProcessableElementTag) firstEvent;
			for (final IAttribute attribute : processableElementTag.getAllAttributes()) {
				final String completeName = attribute.getAttributeCompleteName();
				attributes.put(completeName, attribute.getValue());
			}
		}
		return attributes;
	}

}
