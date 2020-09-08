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

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.Fragment;
import org.thymeleaf.templatemode.TemplateMode;

import io.vertigo.core.lang.Assertion;

public class SlotAttributeTagProcessor extends AbstractAttributeTagProcessor {
	protected static final String VARIABLE_PLACEHOLDER_SEPARATOR = "_";
	protected static final String SLOTS_SUFFIX = "slot";
	private static final String ATTR_NAME = "slot";
	private static final int PRECEDENCE = 400;

	public SlotAttributeTagProcessor(final String dialectPrefix) {
		super(
				TemplateMode.HTML, // This processor will apply only to HTML mode
				dialectPrefix, // Prefix to be applied to name for matching
				null, // No tag name: match any tag name
				false, // No prefix to be applied to tag name
				ATTR_NAME, // Name of the attribute that will be matched
				true, // Apply dialect prefix to attribute name
				PRECEDENCE, // Precedence (inside dialect's precedence)
				true); // Remove the matched attribute afterwards
	}

	@Override
	protected void doProcess(
			final ITemplateContext context, final IProcessableElementTag tag,
			final AttributeName attributeName, final String attributeValue,
			final IElementTagStructureHandler structureHandler) {
		Assertion.check().isTrue(
				attributeValue.endsWith(VARIABLE_PLACEHOLDER_SEPARATOR + SLOTS_SUFFIX),
				"{0} isn't a slot. Attribute vu:slot supports only slots, names must ends with '_slot'", attributeValue);
		//-----
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
				structureHandler.removeElement();
			} else {
				//Else we replace the body by user defined slot
				//structureHandler.removeBody();
				structureHandler.setBody(slotModel, true);
			}
		} else if (tag instanceof IStandaloneElementTag) {
			//if empty slot we remove all tag (open tag, body and close tag)
			structureHandler.removeElement();
		}
		//else we keep slot in component as is => use component default
	}

}
