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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.impl.springmvc.util.UiRequestUtil;

public class ViewContextReturnValueAndArgumentResolver extends AbstractMessageConverterMethodProcessor {

	public ViewContextReturnValueAndArgumentResolver() {
		super(Collections.singletonList(DIInjector.newInstance(VegaJsonHttpMessageConverter.class, Node.getNode().getComponentSpace())));
	}

	@Override
	public boolean supportsReturnType(final MethodParameter returnType) {
		return ViewContext.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public void handleReturnValue(@NonNull final Object returnValue, final MethodParameter returnType, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest) throws Exception {
		Assertion.check()
				.isNotNull(mavContainer)
				.isNotNull(returnValue);
		//---
		mavContainer.setRequestHandled(true);
		final ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		final ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

		// Try even with null return value. ResponseBodyAdvice could get involved.
		final Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("model", ((ViewContext) returnValue).asUpdatesMap());
		responseMap.put("uiMessageStack", UiRequestUtil.obtainCurrentUiMessageStack());
		writeWithMessageConverters(responseMap, returnType, inputMessage, outputMessage);

	}

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		return ViewContext.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
		return UiRequestUtil.getCurrentViewContext();
	}

}
