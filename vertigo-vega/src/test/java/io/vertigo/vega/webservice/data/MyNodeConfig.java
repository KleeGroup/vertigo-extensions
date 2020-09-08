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
package io.vertigo.vega.webservice.data;

import java.util.Arrays;
import java.util.Iterator;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.plugins.authorization.loaders.JsonSecurityDefinitionProvider;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.plugins.app.infos.http.HttpAppNodeInfosPlugin;
import io.vertigo.connectors.javalin.JavalinFeatures;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datafactory.DataFactoryFeatures;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.vega.VegaFeatures;
import io.vertigo.vega.engines.webservice.cmd.ComponentCmdWebServices;
import io.vertigo.vega.webservice.data.domain.Address;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactCriteria;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.data.domain.ContactView;
import io.vertigo.vega.webservice.data.search.ContactSearchClient;
import io.vertigo.vega.webservice.data.user.TestUserSession;
import io.vertigo.vega.webservice.data.ws.AdvancedTestWebServices;
import io.vertigo.vega.webservice.data.ws.AnonymousTestWebServices;
import io.vertigo.vega.webservice.data.ws.CommonWebServices;
import io.vertigo.vega.webservice.data.ws.ContactsSecuredWebServices;
import io.vertigo.vega.webservice.data.ws.ContactsWebServices;
import io.vertigo.vega.webservice.data.ws.FileDownloadWebServices;
import io.vertigo.vega.webservice.data.ws.LoginSecuredWebServices;
import io.vertigo.vega.webservice.data.ws.SearchTestWebServices;
import io.vertigo.vega.webservice.data.ws.SimplerTestWebServices;
import io.vertigo.vega.webservice.data.ws.ValidationsTestWebServices;

public final class MyNodeConfig {
	public static final int WS_PORT = 8088;

	public static final class DtDefinitions implements Iterable<Class<?>> {
		@Override
		public Iterator<Class<?>> iterator() {
			return Arrays.asList(new Class<?>[] {
					Contact.class, ContactCriteria.class,
					Address.class, ContactView.class
			}).iterator();
		}
	}

	public static NodeConfig config(final boolean isEmbedded) {
		final JavalinFeatures javalinFeatures = new JavalinFeatures();
		if (isEmbedded) {
			javalinFeatures.withEmbeddedServer(Param.of("port", WS_PORT));
		} else {
			javalinFeatures.withStandalone();
		}

		final VegaFeatures vegaFeatures = new VegaFeatures()
				.withWebServices()
				.withJavalinWebServerPlugin()
				.withWebServicesTokens(Param.of("tokens", "tokens"))
				.withWebServicesSecurity()
				.withWebServicesRateLimiting()
				.withWebServicesSwagger()
				.withWebServicesCatalog();

		return NodeConfig.builder()
				.withEndPoint("http://localhost:" + WS_PORT)
				.withBoot(BootConfig.builder()
						.withLocales("fr")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures()
						.withNodeInfosPlugin(HttpAppNodeInfosPlugin.class)
						.build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withMemoryCache()
						.withEntityStore()
						.withKVStore()
						.withDelayedMemoryKV(
								Param.of("collections", "tokens"),
								Param.of("timeToLiveSeconds", "120"))
						.build())
				.addModule(new DataFactoryFeatures().build())
				.addModule(new AccountFeatures()
						.withSecurity(Param.of("userSessionClassName", TestUserSession.class.getName()))
						.withAuthorization()
						.build())
				.addModule(javalinFeatures.build())
				.addModule(vegaFeatures.build())
				//-----
				.addModule(ModuleConfig.builder("dao-app")
						.addComponent(ContactDao.class)
						.addComponent(ContactSearchClient.class)
						.build())
				.addModule(ModuleConfig.builder("webservices-app")
						.addComponent(ComponentCmdWebServices.class)
						.addComponent(CommonWebServices.class)
						.addComponent(ContactsWebServices.class)
						.addComponent(ContactsSecuredWebServices.class)
						.addComponent(LoginSecuredWebServices.class)
						.addComponent(SimplerTestWebServices.class)
						.addComponent(ValidationsTestWebServices.class)
						.addComponent(AdvancedTestWebServices.class)
						.addComponent(AnonymousTestWebServices.class)
						.addComponent(FileDownloadWebServices.class)
						.addComponent(SearchTestWebServices.class)
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", VegaTestSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
								.build())
						.addDefinitionProvider(DefinitionProviderConfig.builder(JsonSecurityDefinitionProvider.class)
								.addDefinitionResource("security", "io/vertigo/vega/webservice/data/ws-auth-config.json")
								.build())
						.build())
				.build();
	}
}
