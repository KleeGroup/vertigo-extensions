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

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.vega.webservice.stereotype.QueryParam;

public class VFileMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		return VFile.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(
			final MethodParameter parameter,
			final ModelAndViewContainer mavContainer,
			final NativeWebRequest webRequest,
			final WebDataBinderFactory binderFactory) throws Exception {
		final HttpServletRequest request = getRequest(webRequest);
		final QueryParam requestParam = parameter.getParameterAnnotation(QueryParam.class);
		Assertion.check().isNotNull(requestParam, "File name wasn't found. Use @QueryParam('myFileRequestParam') in your controller.");
		return VFileUtil.readQueryFile(request, requestParam.value());
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
