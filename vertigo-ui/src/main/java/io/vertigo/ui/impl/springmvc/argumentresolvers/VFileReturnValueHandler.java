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

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpOutputMessage;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datastore.filestore.model.VFile;

public class VFileReturnValueHandler implements HandlerMethodReturnValueHandler {

	@Override
	public boolean supportsReturnType(final MethodParameter returnType) {
		return VFile.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public void handleReturnValue(@NonNull final Object returnValue, final MethodParameter returnType, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest) throws Exception {
		Assertion.check()
				.isNotNull(returnValue)
				.isNotNull(mavContainer);
		//---
		mavContainer.setRequestHandled(true);
		final HttpServletResponse response = getResponse(webRequest);
		VFileUtil.sendVFile((VFile) returnValue, response);
	}

	/**
	 * Creates a new {@link HttpOutputMessage} from the given {@link NativeWebRequest}.
	 * @param webRequest the web request to create an output message from
	 * @return the output message
	 */
	private static HttpServletResponse getResponse(final NativeWebRequest webRequest) {
		final HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		Assert.state(response != null, "No HttpServletResponse");
		return response;
	}

}
