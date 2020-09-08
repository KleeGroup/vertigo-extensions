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
package io.vertigo.ui.impl.springmvc.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import io.vertigo.ui.impl.springmvc.controller.AbstractVSpringMvcController;
import io.vertigo.ui.impl.springmvc.util.UiRequestUtil;

public final class VSpringMvcViewContextInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			final HandlerMethod handlerMethod = (HandlerMethod) handler;
			if (AbstractVSpringMvcController.class.isAssignableFrom(handlerMethod.getBeanType())) {
				((AbstractVSpringMvcController) handlerMethod.getBean()).prepareContext(request);
			}
		}
		return true;
	}

	@Override
	public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final ModelAndView modelAndView) throws Exception {
		if (handler instanceof HandlerMethod) {
			final HandlerMethod handlerMethod = (HandlerMethod) handler;
			if (AbstractVSpringMvcController.class.isAssignableFrom(handlerMethod.getBeanType())) {
				final AbstractVSpringMvcController controller = (AbstractVSpringMvcController) handlerMethod.getBean();
				if (!controller.isViewContextDirty()) {
					controller.makeUnmodifiable();
				}
			}
		}
	}

	@Override
	public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex) throws Exception {
		if (handler instanceof HandlerMethod) {
			final HandlerMethod handlerMethod = (HandlerMethod) handler;
			if (AbstractVSpringMvcController.class.isAssignableFrom(handlerMethod.getBeanType())) {
				final AbstractVSpringMvcController controller = (AbstractVSpringMvcController) handlerMethod.getBean();
				if (!controller.isViewContextDirty()) {
					controller.storeContext();
				}
				if (response.getStatus() / 100 == 2 || UiRequestUtil.isJsonRequest(request) || response.getStatus() / 100 == 4) {
					//we reset uiMessageStack only in case of viewable page 2xx or 4xx or ajax request. We are sure to keep it on error page 5xx or redirect 3xx.
					UiRequestUtil.removeCurrentUiMessageStack();
				}
			}
		}
	}

}
