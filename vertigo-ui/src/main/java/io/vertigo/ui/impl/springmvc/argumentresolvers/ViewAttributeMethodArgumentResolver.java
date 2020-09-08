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
package io.vertigo.ui.impl.springmvc.argumentresolvers;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.ui.core.UiSelectedFacetValues;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.impl.springmvc.util.UiRequestUtil;
import io.vertigo.vega.engines.webservice.json.SelectedFacetValuesDeserializer;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.stereotype.Validate;
import io.vertigo.vega.webservice.validation.DefaultDtObjectValidator;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.ValidationUserException;

public final class ViewAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private final List<DtObjectValidator<DtObject>> defaultDtObjectValidators = Collections.singletonList(new DefaultDtObjectValidator<>());
	private final Gson gson = new GsonBuilder().registerTypeAdapter(SelectedFacetValues.class, new SelectedFacetValuesDeserializer()).create();

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		return parameter.hasParameterAnnotation(ViewAttribute.class);
	}

	@Override
	public Object resolveArgument(
			final MethodParameter parameter,
			final ModelAndViewContainer mavContainer,
			final NativeWebRequest webRequest,
			final WebDataBinderFactory binderFactory) throws Exception {
		final ViewContext viewContext = UiRequestUtil.getCurrentViewContext();
		final UiMessageStack uiMessageStack = UiRequestUtil.obtainCurrentUiMessageStack();
		Assertion.check().isNotNull(viewContext);
		//---
		final String contextKey = parameter.getParameterAnnotation(ViewAttribute.class).value();
		//---
		if (UiObject.class.isAssignableFrom(parameter.getParameterType())) {
			return viewContext.getUiObject(() -> contextKey);
		} else if (SelectedFacetValues.class.isAssignableFrom(parameter.getParameterType())) {
			final String jsonSelectedFacets = webRequest.getParameter("selectedFacets");
			if (jsonSelectedFacets != null) {// param present
				final SelectedFacetValues selectedFacetValues = gson.fromJson(jsonSelectedFacets, SelectedFacetValues.class);
				final Collection<String> facetNames = ((List<Map<String, Serializable>>) viewContext.asMap().get(contextKey + "_facets"))
						.stream()
						.map(map -> (String) map.get("code"))
						.collect(Collectors.toSet());
				viewContext.asMap().put(contextKey + "_selectedFacets", new UiSelectedFacetValues(selectedFacetValues, facetNames));
				return selectedFacetValues;
			}
			return viewContext.getSelectedFacetValues(() -> contextKey);
		} else if (DtObject.class.isAssignableFrom(parameter.getParameterType()) || DtList.class.isAssignableFrom(parameter.getParameterType())) {
			Assertion.check().isNotNull(uiMessageStack);
			//---
			final Object value;
			if (DtObject.class.isAssignableFrom(parameter.getParameterType())) {
				//object
				if (viewContext.getUiObject(() -> contextKey).checkFormat(uiMessageStack)) {
					value = viewContext.getUiObject(() -> contextKey).mergeAndCheckInput(getDtObjectValidators(parameter), uiMessageStack);
				} else {
					value = null;
				}
			} else {
				//list
				if (viewContext.getUiList(() -> contextKey).checkFormat(uiMessageStack)) {
					value = viewContext.getUiList(() -> contextKey).mergeAndCheckInput(getDtObjectValidators(parameter), uiMessageStack);
				} else {
					value = null;
				}
			}
			if (!isNotLastDt(parameter) && uiMessageStack.hasErrors()) {
				// if we are the last one
				throw new ValidationUserException();
			}
			return value;
		}
		return viewContext.get(contextKey);// for primitive or other objects
	}

	private List<DtObjectValidator<DtObject>> getDtObjectValidators(final MethodParameter parameter) {
		final Validate validateAnnotation = parameter.getParameterAnnotation(Validate.class);
		List<DtObjectValidator<DtObject>> validators;
		if (validateAnnotation != null) {
			validators = Stream.of(validateAnnotation.value())
					.map(dtObjectValidatorClass -> (DtObjectValidator<DtObject>) InjectorUtil.newInstance(dtObjectValidatorClass))
					.collect(Collectors.toList());
		} else {
			validators = defaultDtObjectValidators;
		}
		return validators;
	}

	private static boolean isNotLastDt(final MethodParameter parameter) {
		return Stream.of(parameter.getMethod().getParameters())
				.skip(parameter.getParameterIndex() + 1L)
				.anyMatch(remainingParam -> DtObject.class.isAssignableFrom(remainingParam.getType()) || DtList.class.isAssignableFrom(remainingParam.getType()));
	}

}
