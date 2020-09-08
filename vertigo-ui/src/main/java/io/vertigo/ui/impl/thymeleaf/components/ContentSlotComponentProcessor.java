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
import org.thymeleaf.standard.expression.Fragment;
import org.thymeleaf.templatemode.TemplateMode;

import io.vertigo.core.lang.Assertion;

public class ContentSlotComponentProcessor extends AbstractElementModelProcessor {

	private static final String CONTENT_TAG_NAME = "content-slot";
	private static final int PRECEDENCE = 450;

	/**
	 * Constructor
	 *
	 * @param dialectPrefix Dialect prefix (tc)
	 */
	public ContentSlotComponentProcessor(final String dialectPrefix) {
		super(TemplateMode.HTML, dialectPrefix, CONTENT_TAG_NAME, true, null, false, PRECEDENCE);
	}

	private static void removeCurrentTag(final IModel model) {
		model.remove(0);
		if (model.size() > 0) {
			model.remove(model.size() - 1);
		}
	}

	@Override
	protected void doProcess(final ITemplateContext context, final IModel model, final IElementModelStructureHandler structureHandler) {
		final Map<String, String> attributes = processAttribute(model);
		final String attributeValue = attributes.get("name");
		Assertion.check().isTrue(
				attributeValue.endsWith(SlotAttributeTagProcessor.VARIABLE_PLACEHOLDER_SEPARATOR + SlotAttributeTagProcessor.SLOTS_SUFFIX),
				"{0} isn't a slot. Tag vu:content-slot supports only slots, names must ends with '_slot'", attributeValue);
		//-----
		removeCurrentTag(model);
		final Object slotModelObject = context.getVariable(attributeValue);
		final IModel slotModel;
		if (slotModelObject instanceof Fragment) {
			slotModel = ((Fragment) slotModelObject).getTemplateModel();
		} else {
			slotModel = (IModel) slotModelObject;
		}
		if (slotModel != null) {
			if (slotModel.size() == 0) {
				//if empty slot we remove all tag (open tag, body and close tag)
				model.reset();
			} else {
				//Else we replace the body by user defined slot
				model.reset();
				model.addModel(slotModel);
			}
		} else {
			//if empty slot we remove all tag (open tag, body and close tag)
		}
		//-----
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
