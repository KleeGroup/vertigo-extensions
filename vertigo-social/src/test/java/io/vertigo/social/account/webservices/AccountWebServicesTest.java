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
package io.vertigo.social.account.webservices;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.social.MyNodeConfig;
import io.vertigo.social.data.MockIdentities;
import redis.clients.jedis.Jedis;

/**
 * Account extension web services tests.
 * @author npiedeloup
 */
public final class AccountWebServicesTest {
	private static AutoCloseableNode node;

	@Inject
	private MockIdentities mockIdentities;
	@Inject
	private RedisConnector redisConnector;

	static {
		RestAssured.port = MyNodeConfig.WS_PORT;
	}

	@BeforeAll
	public static void setUp() {
		node = new AutoCloseableNode(MyNodeConfig.vegaConfig());
	}

	@BeforeEach
	public void setUpInstance() {
		InjectorUtil.injectMembers(this);
		//-----
		try (final Jedis jedis = redisConnector.getClient()) {
			jedis.flushAll();
		} //populate accounts
		mockIdentities.initData();
	}

	@AfterAll
	public static void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private static void assertStatusCode(final int expectedStatus, final String path) {
		RestAssured.given()
				.expect()
				.statusCode(expectedStatus)
				.log().ifError()
				.when()
				.get(path);
	}

	@Test
	public void testGetAccountById() {
		assertStatusCode(HttpStatus.SC_OK, "/x/accounts/api/1");
	}

	@Test
	public void testGetPhotoByAccountId() {
		assertStatusCode(HttpStatus.SC_OK, "/x/accounts/api/1/photo");
	}

	@Test
	public void testGetGroupById() {
		assertStatusCode(HttpStatus.SC_OK, "/x/accounts/api/groups/100");
	}

	@Test
	public void testGetStatus() {
		assertStatusCode(HttpStatus.SC_OK, "/x/accounts/infos/status");
	}

	@Test
	public void testGetStats() {
		assertStatusCode(HttpStatus.SC_OK, "/x/accounts/infos/stats");
	}

	@Test
	public void testGetConfig() {
		assertStatusCode(HttpStatus.SC_OK, "/x/accounts/infos/config");
	}

	@Test
	public void testGetHelp() {
		assertStatusCode(HttpStatus.SC_OK, "/x/accounts/infos/help");
	}

}
