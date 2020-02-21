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
package io.vertigo.ui.impl.springmvc.util;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.vertigo.lang.Assertion;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.impl.springmvc.controller.VSpringMvcUiMessageStack;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * Utilitaire d'accès à la Request.
 * @author npiedeloup
 */
public final class UiRequestUtil {

	private UiRequestUtil() {
		//rien
	}

	/**
	 * Invalide la session Http (ie Logout)
	 */
	public static void invalidateHttpSession() {
		final HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	public static ViewContext getCurrentViewContext() {
		final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		final ViewContext viewContext = (ViewContext) attributes.getAttribute("viewContext", RequestAttributes.SCOPE_REQUEST);
		//---
		Assertion.checkNotNull(viewContext);
		//---
		return viewContext;
	}

	public static UiMessageStack obtainCurrentUiMessageStack() {
		final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		UiMessageStack uiMessageStack = (UiMessageStack) attributes.getAttribute("uiMessageStack", RequestAttributes.SCOPE_SESSION);
		//---
		if (uiMessageStack == null) {
			uiMessageStack = new VSpringMvcUiMessageStack();
			attributes.setAttribute("uiMessageStack", uiMessageStack, RequestAttributes.SCOPE_SESSION);
		}
		//---
		return uiMessageStack;
	}

	public static void removeCurrentUiMessageStack() {
		final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		attributes.removeAttribute("uiMessageStack", RequestAttributes.SCOPE_SESSION);
	}

	public static void setRequestScopedAttribute(final String name, final Object value) {
		Assertion.checkArgNotEmpty(name);
		//---
		final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		attributes.setAttribute(name, value, RequestAttributes.SCOPE_REQUEST);
	}

	public static <O extends Object> Optional<O> getRequestScopedAttribute(final String name, final Class<O> valueClass) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(valueClass);
		//---
		final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		final O value = valueClass.cast(attributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST));
		return Optional.ofNullable(value);
	}
}
