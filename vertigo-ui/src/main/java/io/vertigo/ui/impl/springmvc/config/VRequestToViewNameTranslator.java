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

import org.springframework.web.servlet.RequestToViewNameTranslator;

import io.vertigo.core.lang.Assertion;
import io.vertigo.ui.impl.springmvc.controller.AbstractVSpringMvcController;

public class VRequestToViewNameTranslator implements RequestToViewNameTranslator {

	@Override
	public String getViewName(final HttpServletRequest request) throws Exception {
		final String defaultViewName = (String) request.getAttribute(AbstractVSpringMvcController.DEFAULT_VIEW_NAME_ATTRIBUTE);
		Assertion.check().isNotNull(defaultViewName);
		//---
		return defaultViewName;
	}

}
