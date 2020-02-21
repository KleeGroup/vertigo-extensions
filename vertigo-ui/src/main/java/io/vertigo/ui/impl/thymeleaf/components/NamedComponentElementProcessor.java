/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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

import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.IText;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.AssignationSequence;
import org.thymeleaf.standard.expression.AssignationUtils;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.standard.expression.VariableExpression;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.EvaluationUtils;
import org.thymeleaf.util.StringUtils;

import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

public class NamedComponentElementProcessor extends AbstractElementModelProcessor {
	private static final String VARIABLE_PLACEHOLDER_SEPARATOR = "_";
	private static final String SLOTS_SUFFIX = "slot";
	private static final String ATTRS_SUFFIX = "attrs";
	private static final String CONTENT_TAGS = "contentTags";

	private static final int PRECEDENCE = 350;

	private final Set<String> excludeAttributes = singleton("params");
	private final String componentName;
	private final Optional<VariableExpression> selectionExpression;
	private final List<String> parameterNames;
	private final Set<String> slotNames;
	private final List<String> placeholderPrefixes;
	private final Optional<String> unnamedPlaceholderPrefix;
	private final String frag;

	/**
	 * Constructor
	 *
	 * @param dialectPrefix Dialect prefix (tc)
	 * @param tagName Tag name to search for (e.g. panel)
	 * @param componentName Fragment to search for
	 */
	public NamedComponentElementProcessor(final String dialectPrefix, final NamedComponentDefinition thymeleafComponent) {
		super(TemplateMode.HTML, dialectPrefix, thymeleafComponent.getName(), true, null, false, PRECEDENCE);
		componentName = thymeleafComponent.getFragmentTemplate();
		frag = thymeleafComponent.getFrag();

		selectionExpression = thymeleafComponent.getSelectionExpression();
		parameterNames = thymeleafComponent.getParameters();

		slotNames = parameterNames.stream()
				.filter(key -> key.endsWith(VARIABLE_PLACEHOLDER_SEPARATOR + SLOTS_SUFFIX))
				.collect(Collectors.toSet());

		placeholderPrefixes = parameterNames.stream()
				.filter(parameterName -> parameterName.endsWith(VARIABLE_PLACEHOLDER_SEPARATOR + ATTRS_SUFFIX))
				.map(parameterName -> parameterName.substring(0, parameterName.length() - ATTRS_SUFFIX.length()))
				.collect(Collectors.toList());
		unnamedPlaceholderPrefix = placeholderPrefixes.isEmpty() ? Optional.empty() : Optional.of(placeholderPrefixes.get(placeholderPrefixes.size() - 1));
	}

	@Override
	protected void doProcess(final ITemplateContext context, final IModel model, final IElementModelStructureHandler structureHandler) {
		if (!selectionExpression.isPresent() //no selector
				|| (boolean) selectionExpression.get().execute(context)) { //or selector valid

			final IProcessableElementTag tag = processElementTag(context, model);
			final Map<String, String> attributes = processAttribute(model, context, structureHandler);

			final String param = attributes.get("params");

			final IModel contentModel = cloneAndCleanModel(model);
			removeContainerTag(contentModel);

			if (parameterNames.contains(CONTENT_TAGS)) {
				structureHandler.setLocalVariable(CONTENT_TAGS, tag instanceof IStandaloneElementTag ? Collections.emptyList() : asList(contentModel, context));
			}
			if (!slotNames.isEmpty()) {
				final Map<String, IModel> slotContents = removeAndExtractSlots(contentModel, context);
				for (final Map.Entry<String, IModel> entry : slotContents.entrySet()) {
					Assertion.checkState(slotNames.contains(entry.getKey()), "Component {0} have no slot {1} (accepted slots : {3})", componentName, entry.getKey(), slotNames);
					//-----
					structureHandler.setLocalVariable(entry.getKey(), entry.getValue());
				}
			}

			final String fragmentToUse = "~{" + componentName + " :: " + frag + "}";
			final IModel fragmentModel = FragmentUtil.getFragmentModel(context, fragmentToUse + (param == null ? "" : "(" + param + ")"), structureHandler);
			final IModel clonedFragmentModel = fragmentModel.cloneModel(); //le clone change l'index des éléments

			//final IModel replacedContentSlotModel = replaceContentSlotTags(clonedFragmentModel, slotContents);
			final IModel replacedContentFragmentModel = replaceContentTag(clonedFragmentModel, tag instanceof IStandaloneElementTag ? Optional.empty() : Optional.ofNullable(contentModel));

			//We replace the whole model
			model.reset();
			model.addModel(replacedContentFragmentModel);

			processVariables(attributes, context, structureHandler, excludeAttributes);
		} // else nothing

	}

	private static Map<String, IModel> removeAndExtractSlots(final IModel contentModel, final ITemplateContext context) {
		final Map<String, IModel> slotContents = new HashMap<>();
		final IModel buildingModel = contentModel.cloneModel(); //contains each first level tag (and all it's sub-tags)
		buildingModel.reset();
		int tapDepth = 0;
		String slotName = null;
		final int fullContentSize = contentModel.size();
		for (int i = 0; i < fullContentSize; i++) {
			final ITemplateEvent templateEvent = contentModel.get(0); //get always first (because we remove it)
			if (templateEvent instanceof IOpenElementTag) {
				if ("vu:slot".equals(((IElementTag) templateEvent).getElementCompleteName())) {
					Assertion.checkState(tapDepth == 0, "Can't parse slot {0} it contains another slot", slotName);
					slotName = ((IProcessableElementTag) templateEvent).getAttributeValue("name");
				} else if (tapDepth == 0) {
					break; //slots must be set at first
				}
				tapDepth++;
			} else if (templateEvent instanceof ICloseElementTag) {
				tapDepth--;
			} else if (templateEvent instanceof IStandaloneElementTag) {
				if ("vu:slot".equals(((IElementTag) templateEvent).getElementCompleteName())) {
					//we accept empty slot (to clear a component default slot)
					Assertion.checkState(tapDepth == 0, "Can't parse slot {0} it contains another slot", slotName);
					slotName = ((IProcessableElementTag) templateEvent).getAttributeValue("name");
				} else if (tapDepth == 0) {
					break;
				}
			}
			buildingModel.add(templateEvent); //add first
			contentModel.remove(0); //remove first : in fact we move slot's tags from content model to building model

			if (tapDepth == 0) {
				if ("vu:slot".equals(((IElementTag) templateEvent).getElementCompleteName())) {
					Assertion.checkNotNull(slotName);
					//Si on est à la base, on ajout que le model qu'on a préparé, on le close et on reset pour la boucle suivante
					final IModel firstLevelTagModel = buildingModel.cloneModel();
					if (isVisible(context, firstLevelTagModel)) {
						removeContainerTag(firstLevelTagModel); //we remove the slot tag itself
						slotContents.put(slotName, firstLevelTagModel);
					}
					buildingModel.reset(); //we prepare next buildingModel
				} else {
					break; //slots must be set at first
				}
			}
		}
		Assertion.checkState(tapDepth == 0, "Can't extract component slots, tags may be missclosed in slot {0}", slotName);
		return slotContents;
	}

	private static IModel replaceContentTag(final IModel fragmentModel, final Optional<IModel> contentModel) {
		final int index = findTagIndex("vu:content", 0, fragmentModel, IProcessableElementTag.class);//Open or standalone
		if (index > -1) {
			final int indexEnd = findTagIndex("vu:content", index, fragmentModel, ICloseElementTag.class);
			if (indexEnd > -1) {
				fragmentModel.remove(indexEnd);
			}
			if (contentModel.isPresent()) {
				//We remove old body
				if (indexEnd > -1) {
					for (int i = indexEnd - 1; i > index; i--) {
						fragmentModel.remove(i);
					}
				}
				//We insert new body after content tag (index+1)
				fragmentModel.insertModel(index + 1, contentModel.get());
			} else {
				//
			}
			fragmentModel.remove(index);
		}
		return fragmentModel;
	}

	private static int findTagIndex(final String tagName, final int from, final IModel model, final Class<? extends IElementTag> tagClass) {
		ITemplateEvent event = null;
		final int size = model.size();
		for (int i = from; i < size; i++) {
			event = model.get(i);
			if (tagClass.isInstance(event)
					&& ((IElementTag) event).getElementCompleteName().equals(tagName)) {
				return i;
			}
		}
		return -1;
	}

	private static IModel cloneAndCleanModel(final IModel model) {
		final IModel cleanerModel = model.cloneModel();
		final int size = cleanerModel.size();
		for (int i = size - 1; i > 0; i--) { //We loop decreasly for remove by index
			if (cleanerModel.get(i) instanceof IText) {
				final IText innerText = (IText) cleanerModel.get(i);
				if (StringUtil.isEmpty(innerText.getText())) {
					cleanerModel.remove(i);
				}
			}
		}
		return cleanerModel;
	}

	private static void removeContainerTag(final IModel contentModel) {
		//Remove container tag
		contentModel.remove(0);
		if (contentModel.size() > 1) {
			contentModel.remove(contentModel.size() - 1);
		}
	}

	private static List<NamedComponentContentComponent> asList(final IModel componentModel, final ITemplateContext context) {
		final List<NamedComponentContentComponent> asList = new ArrayList<>();
		final IModel buildingModel = componentModel.cloneModel(); //contains each first level tag (and all it's sub-tags)
		buildingModel.reset();
		int tapDepth = 0;
		for (int i = 0; i < componentModel.size(); i++) {
			final ITemplateEvent templateEvent = componentModel.get(i);
			buildingModel.add(templateEvent);
			if (templateEvent instanceof IOpenElementTag) {
				tapDepth++;
			} else if (templateEvent instanceof ICloseElementTag) {
				tapDepth--;
			}
			if (tapDepth == 0) {
				//Si on est à la base, on ajout que le model qu'on a préparé, on le close et on reset pour la boucle suivante
				final IModel firstLevelTagModel = buildingModel.cloneModel();
				//si on a un tag if, on l'evalue avant d'ajouter le contenu, pour n'avoir que des composants avec un rendu
				if (isVisible(context, firstLevelTagModel)) {
					final NamedComponentContentComponent contentComponent = new NamedComponentContentComponent(firstLevelTagModel);
					asList.add(contentComponent);
				}
				buildingModel.reset();
			}
		}
		return asList;
	}

	private static boolean isVisible(final ITemplateContext context, final IModel firstLevelTagModel) {
		final ITemplateEvent firstLevelTag = firstLevelTagModel.get(0);
		if (firstLevelTag instanceof IProcessableElementTag) {
			final IAttribute ifAttribute = ((IProcessableElementTag) firstLevelTag).getAttribute("th:if");
			if (ifAttribute != null) {
				final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(context.getConfiguration());
				final IStandardExpression expression = expressionParser.parseExpression(context, ifAttribute.getValue());
				final Object value = expression.execute(context);
				return EvaluationUtils.evaluateAsBoolean(value);
			}
		}
		return true;
	}

	private static IProcessableElementTag processElementTag(final ITemplateContext context, final IModel model) {
		final ITemplateEvent firstEvent = model.get(0);
		if (firstEvent instanceof IOpenElementTag) {
			final String elementCompleteName = ((IOpenElementTag) firstEvent).getElementCompleteName();
			final ITemplateEvent lastEvent = model.get(model.size() - 1);
			Assertion.checkArgument(lastEvent instanceof ICloseElementTag
					&& !((ICloseElementTag) lastEvent).isSynthetic()
					&& elementCompleteName.equals(((ICloseElementTag) lastEvent).getElementCompleteName()),
					"Can't find endTag of {0} in {1} line {2} col {3}", elementCompleteName, firstEvent.getTemplateName(), firstEvent.getLine(), firstEvent.getCol());
		}
		for (final IProcessableElementTag tag : context.getElementStack()) {
			if (locationMatches(firstEvent, tag)) {
				return tag;
			}
		}
		return null;
	}

	private static boolean locationMatches(final ITemplateEvent a, final ITemplateEvent b) {
		return Objects.equals(a.getTemplateName(), b.getTemplateName())
				&& Objects.equals(a.getLine(), b.getLine())
				&& Objects.equals(a.getCol(), b.getCol());
	}

	private void processVariables(final Map<String, String> attributes,
			final ITemplateContext context,
			final IElementModelStructureHandler structureHandler,
			final Set<String> excludeAttr) {

		final Map<String, Map<String, Object>> placeholders = new HashMap<>();

		for (final Map.Entry<String, String> entry : attributes.entrySet()) {
			if (excludeAttr.contains(entry.getKey()) || isDynamicAttribute(entry.getKey(), getDialectPrefix())) {
				continue;
			}
			processWith(context, entry.getKey(), entry.getValue(), structureHandler, placeholders);
		}
		//we set placeholders as localvariables (inner components shouldn't affect these in case of name conflict)
		setLocalPlaceholderVariables(structureHandler, placeholders);
	}

	private static Object encodeAttributeValue(final Object attributeValue) {
		if (attributeValue == null) {
			return "${true}";
		} else if ("".equals(attributeValue)) {
			return "''";
		} else if (attributeValue instanceof String
				&& !((String) attributeValue).equalsIgnoreCase("true") //not boolean
				&& !((String) attributeValue).equalsIgnoreCase("false")
				&& (((String) attributeValue).matches("^[a-zA-Z\\s]+[^$#|]*") //almost text
						|| "".equals(((String) attributeValue).trim()))) { //or empty
			return "'" + ((String) attributeValue).replaceAll("'", "\\'") + "'"; //escape as text
		}
		return attributeValue;
	}

	private static String encodeAttributeName(final String attributeName, final Object attributeValue) {
		if (!attributeName.startsWith(":") && !attributeName.startsWith("'")
				&& (attributeValue == null
						|| attributeValue instanceof String
								&& ((String) attributeValue).equalsIgnoreCase("true") //boolean
								&& ((String) attributeValue).equalsIgnoreCase("false"))) {
			return "':" + attributeName + "'";
		} else if (attributeName.matches("^[^'a-zA-Z].*")) {
			return "'" + attributeName + "'";
		}
		return attributeName;
	}

	private void setLocalPlaceholderVariables(final IElementModelStructureHandler structureHandler, final Map<String, Map<String, Object>> placeholders) {
		for (final String placeholderPrefix : placeholderPrefixes) {
			final String placeholder = placeholderPrefix + ATTRS_SUFFIX;
			final String affectationString;
			if (placeholders.containsKey(placeholder)) {
				affectationString = placeholders.get(placeholder)
						.entrySet().stream()
						.map(entry1 -> entry1.getKey() + "=" + entry1.getValue())
						.collect(Collectors.joining(", "));
			} else {
				affectationString = "noOp=_";
			}
			structureHandler.setLocalVariable(placeholder, affectationString);
		}
	}

	private static Map<String, String> processAttribute(final IModel model, final ITemplateContext context, final IElementModelStructureHandler structureHandler) {
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
		final Map<String, String> contentAttrs = (Map<String, String>) context.getVariable("contentAttrs");
		if (contentAttrs != null && !contentAttrs.isEmpty()) {
			structureHandler.removeLocalVariable("contentAttrs");
			for (final Entry<String, String> attribute : contentAttrs.entrySet()) {
				if (shouldConcat(attribute.getKey())) {
					attributes.compute(attribute.getKey(), (k, v) -> (v == null ? "" : v + " ") + attribute.getValue());
				} else {
					attributes.put(attribute.getKey(), attribute.getValue());
				}
			}
		}
		return attributes;
	}

	private static boolean shouldConcat(final String key) {
		return "class".equals(key); //TODO : found the great rule
	}

	private void addPlaceholderVariable(final Map<String, Map<String, Object>> placeholders, final String prefixedVariableName, final Object value) {
		for (final String placeholderPrefix : placeholderPrefixes) {
			if (prefixedVariableName.startsWith(placeholderPrefix)) {
				final String attributeName = prefixedVariableName.substring(placeholderPrefix.length());
				addPlaceholderVariable(placeholders, placeholderPrefix, encodeAttributeName(attributeName, value), encodeAttributeValue(value));
			}
		}
	}

	private static void addPlaceholderVariable(final Map<String, Map<String, Object>> placeholders, final String placeholderPrefix, final String attributeName, final Object value) {
		Map<String, Object> previousPlaceholderValues = placeholders.get(placeholderPrefix + ATTRS_SUFFIX);
		if (previousPlaceholderValues == null) {
			previousPlaceholderValues = new HashMap<>();
			placeholders.put(placeholderPrefix + ATTRS_SUFFIX, previousPlaceholderValues);
		}
		previousPlaceholderValues.put(encodeAttributeName(attributeName, value), encodeAttributeValue(value));
	}

	private boolean isPlaceholder(final String prefixedVariableName) {
		for (final String placeholderPrefix : placeholderPrefixes) {
			if (prefixedVariableName.startsWith(placeholderPrefix)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isDynamicAttribute(final String attribute, final String prefix) {
		return attribute.startsWith(prefix + ":") || attribute.startsWith("data-" + prefix + "-");
	}

	private void processWith(
			final ITemplateContext context,
			final String attributeKey,
			final Object attributeValue,
			final IElementModelStructureHandler structureHandler,
			final Map<String, Map<String, Object>> placeholders) {
		Assertion.checkArgNotEmpty(attributeKey, "Variable name can't be null or empty");
		//-----
		if (isPlaceholder(attributeKey)) {
			//We prepared prefixed placeholders variables.
			addPlaceholderVariable(placeholders, attributeKey, attributeValue);
		} else if (!parameterNames.contains(attributeKey)) {
			Assertion.checkState(
					unnamedPlaceholderPrefix.isPresent(),
					"Component '{0}' can't accept this parameter : '{1}' (accepted params : {2})", componentName, attributeKey, parameterNames);
			//We prepared unnamed placeholder variable
			addPlaceholderVariable(placeholders, unnamedPlaceholderPrefix.get(), attributeKey, attributeValue);
		} else {

			// Normally we would just allow the structure handler to be in charge of declaring the local variables
			// by using structureHandler.setLocalVariable(...) but in this case we want each variable defined at an
			// expression to be available for the next expressions, and that forces us to cast our ITemplateContext into
			// a more specific interface --which shouldn't be used directly except in this specific, special case-- and
			// put the local variables directly into it.
			IEngineContext engineContext = null;
			if (context instanceof IEngineContext) {
				// NOTE this interface is internal and should not be used in users' code
				engineContext = (IEngineContext) context;
			}

			final Object encodedAttributeValue = encodeAttributeValue(attributeValue);
			final AssignationSequence assignations = AssignationUtils.parseAssignationSequence(context, attributeKey + "=" + encodedAttributeValue, false /* no parameters without value */);
			if (assignations == null) {
				throw new TemplateProcessingException("Could not parse value as attribute assignations: \"" + attributeKey + "=" + encodedAttributeValue + "\"");
			}
			final List<Assignation> assignationValues = assignations.getAssignations();
			final int assignationValuesLen = assignationValues.size();

			for (int i = 0; i < assignationValuesLen; i++) {

				final Assignation assignation = assignationValues.get(i);

				final IStandardExpression leftExpr = assignation.getLeft();
				final Object leftValue = leftExpr.execute(context);

				final String newVariableName = leftValue == null ? null : leftValue.toString();
				if (StringUtils.isEmptyOrWhitespace(newVariableName)) {
					throw new TemplateProcessingException("Variable name expression evaluated as null or empty: \"" + leftExpr + "\"");
				}

				final IStandardExpression rightExpr = assignation.getRight();
				final Object rightValue = rightExpr.execute(context);
				if (engineContext != null) {
					// The advantage of this vs. using the structure handler is that we will be able to
					// use this newly created value in other expressions in the same 'th:with'
					engineContext.setVariable(newVariableName, rightValue);
				} else {
					// The problem is, these won't be available until we execute the next processor
					structureHandler.setLocalVariable(newVariableName, rightValue);
				}
			}
		}
	}
}
