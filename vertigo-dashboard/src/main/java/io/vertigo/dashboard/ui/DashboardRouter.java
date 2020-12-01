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
package io.vertigo.dashboard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.core.Configurable;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.javalin.Javalin;
import io.vertigo.core.node.Node;
import io.vertigo.core.util.FileUtil;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.dashboard.ui.commons.CommonsDashboardControler;
import io.vertigo.dashboard.ui.dynamo.DynamoDashboardControler;
import io.vertigo.dashboard.ui.vega.VegaDashboardControler;
import io.vertigo.dashboard.ui.vui.VUiDashboardControler;

public final class DashboardRouter {
	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardRouter.class);
	private final Configuration configuration;

	private static final Map<String, Class<? extends DashboardModuleControler>> controlerMap = new HashMap<>();

	static {
		controlerMap.put("vertigo-commons", CommonsDashboardControler.class);
		controlerMap.put("vertigo-dynamo", DynamoDashboardControler.class);
		controlerMap.put("vertigo-vega", VegaDashboardControler.class);
		controlerMap.put("vertigo-ui", VUiDashboardControler.class);
	}

	private final Node node;

	/**
	 * Creates a new studio for an existing app
	 * @param node the node we are working on
	 */
	public DashboardRouter(final Node node) {
		configuration = new Configuration(Configuration.VERSION_2_3_23);
		configuration.setTemplateLoader(new ClassTemplateLoader(DashboardRouter.class, "/"));
		configuration.setClassForTemplateLoading(DashboardRouter.class, "");
		final BeansWrapperBuilder beansWrapperBuilder = new BeansWrapperBuilder(Configuration.VERSION_2_3_23);
		beansWrapperBuilder.setSimpleMapWrapper(true);
		configuration.setObjectWrapper(beansWrapperBuilder.build());
		try {
			configuration.setSetting(Configurable.NUMBER_FORMAT_KEY, "computer");
		} catch (final TemplateException e) {
			LOGGER.error("Error putting settings", e);
		}
		this.node = node;
	}

	/**
	 * Start method of the server
	 */
	public void route(final Javalin javalin) {

		javalin.get("/dashboard/static/:fileName", (ctx) -> {
			try (InputStream inputStream = DashboardRouter.class.getResource("/static/" + ctx.pathParam(":fileName")).openStream()) {
				try (final OutputStream output = ctx.res.getOutputStream()) {
					FileUtil.copy(inputStream, output);
				}
			}
		});

		javalin.get("/dashboard/", (ctx) ->

		{
			final List<String> modules = Arrays.asList("vertigo-commons", "vertigo-dynamo", "vertigo-vega", "vertugo-ui");
			final Map<String, Object> model = new HashMap<>();
			model.put("modules", modules);
			model.put("contextName", ctx.contextPath());
			render(ctx.res, "templates/home.ftl", model);
		});

		javalin.get("/dashboard/modules/:moduleName", (ctx) -> {
			final String moduleName = ctx.pathParam(":moduleName");
			final DashboardModuleControler controler = InjectorUtil.newInstance(controlerMap.get(moduleName));
			final Map<String, Object> model = controler.buildModel(node, moduleName);
			model.put("contextName", ctx.contextPath());
			render(ctx.res, "templates/" + moduleName + ".ftl", model);
		});

	}

	private void render(final HttpServletResponse response, final String templateName, final Map<String, Object> model) throws TemplateException, IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html");

		final StringWriter stringWriter = new StringWriter();

		final Template template = configuration.getTemplate(templateName);
		template.process(model, stringWriter);

		try (OutputStream outputStream = response.getOutputStream()) {
			outputStream.write(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
		}
	}

}
