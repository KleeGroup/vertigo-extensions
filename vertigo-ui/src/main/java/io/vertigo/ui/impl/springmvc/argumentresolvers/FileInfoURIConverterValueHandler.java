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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.filestore.model.FileInfoURI;
import io.vertigo.ui.core.ProtectedValueUtil;
import io.vertigo.vega.webservice.stereotype.QueryParam;

public final class FileInfoURIConverterValueHandler extends AbstractMessageConverterMethodProcessor {

	//Used to support ParameterizedType parameters (not used for return value)
	private final ParameterizedTypeValueHandlerHelper<FileInfoURI> parameterizedTypeValueHandlerHelper;

	public FileInfoURIConverterValueHandler() {
		super(Collections.singletonList(new StringHttpMessageConverter(StandardCharsets.UTF_8)));
		parameterizedTypeValueHandlerHelper = new ParameterizedTypeValueHandlerHelper<>(FileInfoURI.class, FileInfoURIConverterValueHandler::toFileInfoURI);
		parameterizedTypeValueHandlerHelper.addSupportedParameterizedType(Optional.class);
		parameterizedTypeValueHandlerHelper.addSupportedParameterizedType(List.class);
	}

	@Override
	public boolean supportsReturnType(final MethodParameter returnType) {
		return FileInfoURI.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public void handleReturnValue(
			final Object returnValue,
			final MethodParameter returnType,
			final ModelAndViewContainer mavContainer,
			final NativeWebRequest webRequest) throws Exception {
		mavContainer.setRequestHandled(true);
		final ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		final ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);
		final String value = ProtectedValueUtil.generateProtectedValue((FileInfoURI) returnValue);

		// Try even with null return value. ResponseBodyAdvice could get involved.
		writeWithMessageConverters(value, returnType, inputMessage, outputMessage);
	}

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		return parameterizedTypeValueHandlerHelper.supportsType(parameter);
	}

	@Override
	public Object resolveArgument(
			final MethodParameter parameter,
			final ModelAndViewContainer mavContainer,
			final NativeWebRequest webRequest,
			final WebDataBinderFactory binderFactory) throws Exception {
		final HttpServletRequest request = getRequest(webRequest);
		final QueryParam requestParam = parameter.getParameterAnnotation(QueryParam.class);
		Assertion.check().isNotNull(requestParam, "Parameter name wasnt't found. Use @QueryParam('myFileParam') in your controller.");
		final String fileUriProtected = request.getParameter(requestParam.value());

		return parameterizedTypeValueHandlerHelper.convert(fileUriProtected, parameter);
	}

	private static FileInfoURI toFileInfoURI(final String requestValue) {
		return ProtectedValueUtil.readProtectedValue(requestValue, FileInfoURI.class);
	}

	/**
	 * Create a new {@link HttpInputMessage} from the given {@link NativeWebRequest}.
	 * @param webRequest the web request to create an input message from
	 * @return the input message
	 */
	private static HttpServletRequest getRequest(final NativeWebRequest webRequest) {
		final HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		Assert.state(servletRequest != null, "No HttpServletRequest");
		return servletRequest;
	}
}
