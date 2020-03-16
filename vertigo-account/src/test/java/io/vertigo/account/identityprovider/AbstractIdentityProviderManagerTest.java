/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.account.identityprovider;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.data.TestSmartTypes;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.data.model.DtDefinitions;
import io.vertigo.account.identityprovider.model.User;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.node.AutoCloseableApp;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.smarttype.ModelDefinitionProvider;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Implementation standard de la gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
abstract class AbstractIdentityProviderManagerTest {

	@Inject
	private VSecurityManager securityManager;

	@Inject
	private IdentityProviderManager identityProviderManager;

	private AutoCloseableApp app;

	@BeforeEach
	public final void setUp() throws Exception {
		app = new AutoCloseableApp(buildNodeConfig());
		DIInjector.injectMembers(this, app.getComponentSpace());
		//---
		securityManager.startCurrentUserSession(securityManager.createUserSession());
	}

	@AfterEach
	public final void tearDown() throws Exception {
		if (app != null) {
			try {
				app.close();
			} finally {
				securityManager.stopCurrentUserSession();
			}
		}
	}

	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures().build())
				.addModule(new AccountFeatures()
						.withSecurity(
								Param.of("userSessionClassName", TestUserSession.class.getName()))
						.withIdentityProvider()
						.withLdapIdentityProvider(
								Param.of("ldapServerHost", "docker-vertigo.part.klee.lan.net"),
								Param.of("ldapServerPort", "389"),
								Param.of("ldapAccountBaseDn", "dc=vertigo,dc=io"),
								Param.of("ldapReaderLogin", "cn=admin,dc=vertigo,dc=io"),
								Param.of("ldapReaderPassword", "v3rt1g0"),
								Param.of("ldapUserAuthAttribute", "cn"),
								Param.of("userDtDefinitionName", "DtUser"),
								Param.of("ldapUserAttributeMapping", "usrId:cn, fullName:description"))
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(
								DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
										.addDefinitionResource("smarttypes", TestSmartTypes.class.getName())
										.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
										.build())
						.build())
				.build();
	}

	@Test
	public void testUsersCount() {
		Assertions.assertEquals(userCountForTest(), identityProviderManager.getUsersCount());
	}

	@Test
	public void testAllUsers() {
		Assertions.assertEquals(userCountForTest(), identityProviderManager.getAllUsers().size());
	}

	@Test
	public void testPhoto() {
		final List<User> users = identityProviderManager.getAllUsers();
		//Before the photo is the default photo
		final Optional<VFile> photo = identityProviderManager.getPhoto(users.get(1).getUID());
		Assertions.assertTrue(photo.isPresent());
		Assertions.assertEquals("photo-jdoe.jpg", photo.get().getFileName());
		Assertions.assertEquals(Long.valueOf(23112L), photo.get().getLength());
	}

	@Test
	public void testNoPhoto() {
		final List<User> users = identityProviderManager.getAllUsers();
		//Before the photo is the default photo
		Assertions.assertFalse(identityProviderManager.getPhoto(users.get(0).getUID()).isPresent());
	}

	@Test
	public void testUserByAuthToken() {
		final User user = identityProviderManager.getUserByAuthToken(adminAuthToken());
		Assertions.assertNotNull(user, "Can't find user by login ");
	}

	protected abstract int userCountForTest();

	protected String adminAuthToken() {
		return "admin@yopmail.com";
	}

}
