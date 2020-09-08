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
package io.vertigo.commons;

import io.vertigo.commons.app.AppManager;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.command.CommandManager;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.impl.app.AppManagerImpl;
import io.vertigo.commons.impl.app.AppNodeInfosPlugin;
import io.vertigo.commons.impl.app.AppNodeRegistryPlugin;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.commons.impl.command.CommandManagerImpl;
import io.vertigo.commons.impl.eventbus.EventBusManagerImpl;
import io.vertigo.commons.impl.script.ScriptManagerImpl;
import io.vertigo.commons.impl.transaction.VTransactionAspect;
import io.vertigo.commons.impl.transaction.VTransactionManagerImpl;
import io.vertigo.commons.plugins.app.infos.http.HttpAppNodeInfosPlugin;
import io.vertigo.commons.plugins.app.registry.db.DbAppNodeRegistryPlugin;
import io.vertigo.commons.plugins.app.registry.redis.RedisAppNodeRegistryPlugin;
import io.vertigo.commons.plugins.script.janino.JaninoExpressionEvaluatorPlugin;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;

/**
 * Defines commons module.
 * @author pchretien
 */
public final class CommonsFeatures extends Features<CommonsFeatures> {

	/**
	 * Constructor.
	 */
	public CommonsFeatures() {
		super("vertigo-commons");
	}

	/**
	 * Activates script with a default plugin.
	 *
	 * @return these features
	 */
	@Feature("script")
	public CommonsFeatures withScript() {
		getModuleConfigBuilder()
				.addComponent(ScriptManager.class, ScriptManagerImpl.class);
		return this;
	}

	/**
	 * Activates script with a default plugin.
	 *
	 * @return these features
	 */
	@Feature("script.janino")
	public CommonsFeatures withJaninoScript() {
		getModuleConfigBuilder()
				.addPlugin(JaninoExpressionEvaluatorPlugin.class);
		return this;
	}

	@Feature("app.dbRegistry")
	public CommonsFeatures withDbAppNodeRegistryPlugin(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(DbAppNodeRegistryPlugin.class, params);
		return this;

	}

	@Feature("app.redisRegistry")
	public CommonsFeatures withRedisAppNodeRegistryPlugin() {
		getModuleConfigBuilder()
				.addPlugin(RedisAppNodeRegistryPlugin.class);
		return this;

	}

	@Feature("app.httpInfos")
	public CommonsFeatures withHttpAppNodeInfosPlugin() {
		getModuleConfigBuilder()
				.addPlugin(HttpAppNodeInfosPlugin.class);
		return this;

	}

	@Feature("command")
	public CommonsFeatures withCommand() {
		getModuleConfigBuilder()
				.addComponent(CommandManager.class, CommandManagerImpl.class);
		return this;

	}

	/**
	 * Adds a NodeRegistryPlugin
	 * @param nodeRegistryPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures withNodeRegistryPlugin(final Class<? extends AppNodeRegistryPlugin> nodeRegistryPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(nodeRegistryPluginClass, params);
		return this;

	}

	/**
	 * Adds a NodeInfosPlugin
	 * @param nodeInfosPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures withNodeInfosPlugin(final Class<? extends AppNodeInfosPlugin> nodeInfosPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(nodeInfosPluginClass, params);
		return this;

	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(CodecManager.class, CodecManagerImpl.class)
				.addComponent(EventBusManager.class, EventBusManagerImpl.class)
				.addComponent(AppManager.class, AppManagerImpl.class)
				.addComponent(VTransactionManager.class, VTransactionManagerImpl.class)
				.addAspect(VTransactionAspect.class);
	}
}
