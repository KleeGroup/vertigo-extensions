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

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.Node;

public class VSpringDispatcherServlet extends DispatcherServlet {

	private static final long serialVersionUID = 1836587973110870111L;

	private AnalyticsManager analyticsManager;

	public VSpringDispatcherServlet(final WebApplicationContext webApplicationContext) {
		super(webApplicationContext);
	}

	@Override
	protected void doDispatch(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final HandlerExecutionChain handlerChain = getHandler(request);
		if (handlerChain != null) {
			final Object handler = handlerChain.getHandler();
			if (handler instanceof HandlerMethod) {
				final HandlerMethod handlerMethod = (HandlerMethod) handler;
				final RequestMapping beanRequestMapping = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequestMapping.class);
				final RequestMapping methodRequestMapping = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequestMapping.class);
				final String path = beanRequestMapping.path()[0] + methodRequestMapping.path()[0];
				getAnalyticsManager().trace(
						"page",
						path,
						tracer -> {
							try {
								super.doDispatch(request, response);
							} catch (final Exception e) {
								throw WrappedException.wrap(e);
							}
						});
			} else {
				super.doDispatch(request, response);
			}

		} else {
			super.doDispatch(request, response);
		}

	}

	private AnalyticsManager getAnalyticsManager() {
		if (analyticsManager == null) {
			setAnalyticsManager();
		}
		return analyticsManager;
	}

	private synchronized void setAnalyticsManager() {
		analyticsManager = Node.getNode().getComponentSpace().resolve(AnalyticsManager.class);
	}

}
