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
import java.util.Optional;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;

public class ContentItemComponentProcessor extends AbstractElementModelProcessor {

	private static final String CONTENT_VAR_NAME = "contentItem";
	private static final String CONTENT_TAG_NAME = "content-item";
	private static final int PRECEDENCE = 400;

	/**
	 * Constructor
	 *
	 * @param dialectPrefix Dialect prefix (tc)
	 */
	public ContentItemComponentProcessor(final String dialectPrefix) {
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
		final Object content = context.getVariable(CONTENT_VAR_NAME);
		Assertion.check().isNotNull(content, "'" + CONTENT_VAR_NAME + "' variable missing. For loop use th:each=\"" + CONTENT_VAR_NAME + ":contentTags\".");
		//-----
		removeCurrentTag(model);
		final IModel defaultModel = model.cloneModel(); //default is the body of the content tag
		final IModel mergedModel;
		if (content instanceof Optional) {
			final Optional<IModel> contentModel = (Optional<IModel>) content;
			if (((Optional) content).isPresent()) {
				//We merge models : ie replace vu:content tag in component fragment by the body of tag in call page
				mergedModel = contentModel.get();
			} else {
				mergedModel = defaultModel; //We use default value (in vu:content tag)
			}
		} else if (content instanceof NamedComponentContentComponent) {
			//We merge models : ie replace vu:content tag in component fragment by the body of tag in call page
			mergedModel = ((NamedComponentContentComponent) content).getModel();
		} else {
			throw new VSystemException("Content variable type not supported ({0})", content.getClass().getName());
		}
		model.reset();//we prepared the model to be set
		model.addModel(mergedModel);
		structureHandler.setLocalVariable("contentAttrs", attributes);
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
