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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import io.vertigo.connectors.spring.EnableVertigoSpringBridge;
import io.vertigo.ui.controllers.ListAutocompleteController;
import io.vertigo.ui.impl.springmvc.argumentresolvers.DtListStateMethodArgumentResolver;
import io.vertigo.ui.impl.springmvc.argumentresolvers.FileInfoURIConverterValueHandler;
import io.vertigo.ui.impl.springmvc.argumentresolvers.UiMessageStackMethodArgumentResolver;
import io.vertigo.ui.impl.springmvc.argumentresolvers.VFileMethodArgumentResolver;
import io.vertigo.ui.impl.springmvc.argumentresolvers.VFileReturnValueHandler;
import io.vertigo.ui.impl.springmvc.argumentresolvers.ViewAttributeMethodArgumentResolver;
import io.vertigo.ui.impl.springmvc.argumentresolvers.ViewContextReturnValueAndArgumentResolver;
import io.vertigo.ui.impl.springmvc.controller.VSpringMvcControllerAdvice;
import io.vertigo.ui.impl.thymeleaf.VUiStandardDialect;
import io.vertigo.ui.impl.thymeleaf.components.NamedComponentDefinition;
import io.vertigo.ui.impl.thymeleaf.components.NamedComponentParser;
import io.vertigo.ui.impl.thymeleaf.components.VuiResourceTemplateResolver;
import nz.net.ultraq.thymeleaf.LayoutDialect;

@Configuration
@EnableVertigoSpringBridge
@EnableWebMvc
public class VSpringWebConfig implements WebMvcConfigurer, ApplicationContextAware {

	@Autowired
	private ApplicationContext applicationContext;

	private static final String COMPONENT_PATH_PREFIX = "io/vertigo/ui/";
	private static final String[] STANDARD_UI_COMPONENTS_NAME = {
			"utils/vue-data", "utils/include-data", //technical components
			"layout/page", "layout/head", "layout/form", "layout/modal", "layout/block", //layout components
			"layout/grid", "layout/grid-cell", //grid
			"layout/messages", //messages
			"inputs/label", "inputs/text-field", "inputs/text-area", "inputs/checkbox", "inputs/checkbox-multiple", "inputs/slider", "inputs/knob", "inputs/fileupload", //standard controls components
			"inputs/select", "inputs/radio", //select controls components
			"inputs/autocomplete", "inputs/date", "inputs/datetime", "inputs/chips-autocomplete", //with client-worflow controls components
			"inputs/geolocation", // geoLocation
			"table/table", "table/column", //table
			"collections/collection", "collections/list", "collections/cards", "collections/field-read", // collections
			"collections/search", "collections/facets", //search
			"buttons/button-submit", "buttons/button-link", "buttons/buttons-group" //buttons
	};

	/*
	* STEP 1 - Create SpringResourceTemplateResolver
	* */
	@Bean
	public SpringResourceTemplateResolver templateResolver() {
		final SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setApplicationContext(applicationContext);
		templateResolver.setPrefix("/WEB-INF/views/");
		templateResolver.setSuffix(".html");
		// for dev purpose
		templateResolver.setCacheable(!isDevMode());
		return templateResolver;
	}

	private VuiResourceTemplateResolver componentsResolver() {
		final VuiResourceTemplateResolver templateResolver = new VuiResourceTemplateResolver();
		templateResolver.setApplicationContext(applicationContext);
		templateResolver.setPrefix("classpath://" + COMPONENT_PATH_PREFIX);
		templateResolver.setSuffix(".html");
		templateResolver.setResolvablePatterns(Collections.singleton("components/*"));
		// for dev purpose
		templateResolver.setCacheable(!isDevMode());
		return templateResolver;
	}

	private VuiResourceTemplateResolver customComponentsResolver() {
		final VuiResourceTemplateResolver templateResolver = new VuiResourceTemplateResolver();
		templateResolver.setApplicationContext(applicationContext);
		templateResolver.setPrefix("classpath://" + getCustomComponentsPathPrefix());
		templateResolver.setSuffix(".html");
		templateResolver.setResolvablePatterns(Collections.singleton("components/*"));
		// for dev purpose
		templateResolver.setCacheable(!isDevMode());
		return templateResolver;
	}

	/*
	* STEP 2 - Create SpringTemplateEngine
	* */
	@Bean
	public SpringTemplateEngine templateEngine() {
		final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		final SpringResourceTemplateResolver viewsResolvers = templateResolver();
		viewsResolvers.setOrder(3);
		templateEngine.setTemplateResolver(viewsResolvers);
		templateEngine.setEnableSpringELCompiler(true);
		//---
		// add components
		final VuiResourceTemplateResolver componentResolvers = componentsResolver();
		componentResolvers.setOrder(1);
		templateEngine.addTemplateResolver(componentResolvers);
		// add custom components
		final VuiResourceTemplateResolver customComponentResolvers = customComponentsResolver();
		customComponentResolvers.setOrder(2);
		templateEngine.addTemplateResolver(customComponentResolvers);
		//---
		final VUiStandardDialect dialect = new VUiStandardDialect(getUiComponents(componentResolvers));
		templateEngine.addDialect("vu", dialect);

		templateEngine.addDialect(new LayoutDialect());

		return templateEngine;
	}

	private Set<NamedComponentDefinition> getUiComponents(final VuiResourceTemplateResolver componentResolvers) {
		final NamedComponentParser parser = new NamedComponentParser("vu", componentResolvers);

		final Set<NamedComponentDefinition> standardUiComponents = new HashSet<>();
		//standard components
		for (final String componentName : STANDARD_UI_COMPONENTS_NAME) {
			standardUiComponents.addAll(parser.parseComponent(componentName));
		}
		// custom compenents
		for (final String componentName : getCustomComponentNames()) {
			standardUiComponents.addAll(parser.parseComponent(componentName));
		}
		return standardUiComponents;
	}

	protected Set<String> getCustomComponentNames() {
		return Collections.emptySet();
	}

	protected String getCustomComponentsPathPrefix() {
		return COMPONENT_PATH_PREFIX;
	}

	@Bean(DispatcherServlet.REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME)
	public RequestToViewNameTranslator customRequestToViewNameTranslator() {
		return new VRequestToViewNameTranslator();
	}

	@Bean
	public StandardServletMultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}

	/**
	 * STEP 3 - Register ThymeleafViewResolver
	 */
	@Override
	public void configureViewResolvers(final ViewResolverRegistry registry) {
		final ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setCharacterEncoding("UTF-8");
		resolver.setTemplateEngine(templateEngine());
		registry.viewResolver(resolver);
	}

	@Override
	public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new ViewAttributeMethodArgumentResolver());
		resolvers.add(new ViewContextReturnValueAndArgumentResolver());
		resolvers.add(new UiMessageStackMethodArgumentResolver());
		resolvers.add(new DtListStateMethodArgumentResolver());
		resolvers.add(new VFileMethodArgumentResolver());
		resolvers.add(new FileInfoURIConverterValueHandler());
	}

	@Override
	public void addReturnValueHandlers(final List<HandlerMethodReturnValueHandler> handlers) {
		handlers.add(new ViewContextReturnValueAndArgumentResolver());
		handlers.add(new VFileReturnValueHandler());
		handlers.add(new FileInfoURIConverterValueHandler());
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			//App.getApp().getComponentSpace().keySet()
			//		.stream()
			//		.forEach(key -> ((ConfigurableApplicationContext) applicationContext).getBeanFactory().registerSingleton(key, App.getApp().getComponentSpace().resolve(key, Component.class)));

			final VSpringMvcControllerAdvice controllerAdvice = ((ConfigurableApplicationContext) applicationContext).getBeanFactory().createBean(VSpringMvcControllerAdvice.class);
			((ConfigurableApplicationContext) applicationContext).getBeanFactory().registerSingleton("viewContextControllerAdvice", controllerAdvice);
			final ListAutocompleteController listAutocompleteController = ((ConfigurableApplicationContext) applicationContext).getBeanFactory().createBean(ListAutocompleteController.class);
			((ConfigurableApplicationContext) applicationContext).getBeanFactory().registerSingleton("listAutocompleteController", listAutocompleteController);
		}
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(new VSpringMvcViewContextInterceptor());
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/vertigo-ui/static/**")
				.addResourceLocations("classpath:/io/vertigo/ui/static/")
				.setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());

	}

	protected boolean isDevMode() {
		return true;
	}

}
