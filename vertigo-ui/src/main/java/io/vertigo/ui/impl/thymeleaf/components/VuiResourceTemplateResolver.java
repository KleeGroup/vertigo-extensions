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
package io.vertigo.ui.impl.thymeleaf.components;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring5.templateresource.SpringResourceTemplateResource;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

public class VuiResourceTemplateResolver
		extends AbstractConfigurableTemplateResolver
		implements ApplicationContextAware {

	private ApplicationContext applicationContext = null;

	public VuiResourceTemplateResolver() {
		super();
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	protected ITemplateResource computeTemplateResource(
			final IEngineConfiguration configuration,
			final String ownerTemplate,
			final String template,
			final String resourceName,
			final String characterEncoding,
			final Map<String, Object> templateResolutionAttributes) {
		return new SpringResourceTemplateResource(applicationContext, resourceName, characterEncoding);
	}

	public ITemplateResource resolveResource(final String resourceName) {
		return computeTemplateResource(null, null, resourceName, Collections.emptyMap());
	}

}
