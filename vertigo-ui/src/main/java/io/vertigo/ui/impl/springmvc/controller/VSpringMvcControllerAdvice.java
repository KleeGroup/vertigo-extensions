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
package io.vertigo.ui.impl.springmvc.controller;

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.core.lang.VUserException;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.core.ViewContextMap;
import io.vertigo.ui.impl.springmvc.util.UiRequestUtil;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.UiMessageStack.Level;
import io.vertigo.vega.webservice.validation.ValidationUserException;

@ControllerAdvice(assignableTypes = { AbstractVSpringMvcController.class })
public final class VSpringMvcControllerAdvice {

	@ModelAttribute
	public static void storeContext(final Model model) {
		final ViewContext viewContext = UiRequestUtil.getCurrentViewContext();
		final UiMessageStack uiMessageStack = UiRequestUtil.obtainCurrentUiMessageStack();
		//---
		model.addAttribute("model", viewContext.asMap());
		model.addAttribute("viewContextAsJson", new Supplier<String>() {
			@Override
			public String get() {
				return viewContext.getFilteredViewContextAsJson();
			}
		});
		model.addAttribute("uiMessageStack", uiMessageStack);
		// here we can retrieve anything and put it into the model or in our context
		// we can also use argument resolvers to retrieve attributes in our context for convenience (a DtObject or an UiObject can be retrieved as parameters
		// easily from our vContext since we have access to the modelandviewContainer in a parameterResolver...)
	}

	@ModelAttribute
	public void mapRequestParams(@ModelAttribute("model") final ViewContextMap viewContextMap, final Model model) {
		// just use springMVC value mapper

	}

	@ResponseBody
	@ExceptionHandler(SessionException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public static Object handleSessionException(final SessionException ex, final HttpServletRequest request) throws Throwable {
		return handleThrowable(ex, request);
	}

	@ResponseBody
	@ExceptionHandler(VSecurityException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public static Object handleSessionException(final VSecurityException ex, final HttpServletRequest request) throws Throwable {
		return handleThrowable(ex, request);
	}

	@ResponseBody
	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public static Object handleThrowable(final Throwable th, final HttpServletRequest request) throws Throwable {
		if (UiRequestUtil.isJsonRequest(request)) {
			final UiMessageStack uiMessageStack = UiRequestUtil.obtainCurrentUiMessageStack();
			final String exceptionMessage = th.getMessage() != null ? th.getMessage() : th.getClass().getSimpleName();
			uiMessageStack.addGlobalMessage(Level.ERROR, exceptionMessage);
			return uiMessageStack;
		}
		throw th;
	}

	@ResponseBody
	@ExceptionHandler(ValidationUserException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public static Object handleValidationUserException(final ValidationUserException ex, final HttpServletRequest request) {
		final UiMessageStack uiMessageStack = UiRequestUtil.obtainCurrentUiMessageStack();
		ex.flushToUiMessageStack(uiMessageStack);
		//---
		return handleVUserException(uiMessageStack, request);
	}

	@ResponseBody
	@ExceptionHandler(VUserException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public static Object handleVUserException(final VUserException ex, final HttpServletRequest request) {
		final UiMessageStack uiMessageStack = UiRequestUtil.obtainCurrentUiMessageStack();
		uiMessageStack.addGlobalMessage(Level.ERROR, ex.getMessage());
		//---
		return handleVUserException(uiMessageStack, request);
	}

	private static Object handleVUserException(final UiMessageStack uiMessageStack, final HttpServletRequest request) {
		//---
		final ViewContext viewContext = UiRequestUtil.getCurrentViewContext();
		//---
		if (UiRequestUtil.isJsonRequest(request)) {
			return uiMessageStack;
		}
		//---
		final ModelAndView modelAndView = new ModelAndView();
		viewContext.markDirty();
		modelAndView.addObject("model", viewContext.asMap());
		modelAndView.addObject("viewContextAsJson", new Supplier<String>() {
			@Override
			public String get() {
				return viewContext.getFilteredViewContextAsJson();
			}
		});
		modelAndView.addObject("uiMessageStack", uiMessageStack);
		return modelAndView;
	}

}
