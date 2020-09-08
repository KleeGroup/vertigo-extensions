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
package io.vertigo.social.comment.webservices;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.social.MyNodeConfig;
import io.vertigo.social.comment.Comment;
import io.vertigo.social.comment.CommentManager;
import io.vertigo.social.data.MockIdentities;
import io.vertigo.vega.engines.webservice.json.UTCDateUtil;
import redis.clients.jedis.Jedis;

public final class CommentWebServicesTest {
	private static final int WS_PORT = 8088;
	private final SessionFilter sessionFilter = new SessionFilter();
	private AutoCloseableNode node;

	private static String CONCEPT_KEY_NAME;
	private static UID<Account> account1Uri;
	private static UID<KeyConcept> keyConcept1Uri;
	private static UID<KeyConcept> keyConcept2Uri;

	@Inject
	private RedisConnector redisConnector;
	@Inject
	private CommentManager commentServices;
	@Inject
	private MockIdentities mockIdentities;

	@BeforeEach
	public void setUp() {
		//RestAsssured init
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = WS_PORT;
		//---
		node = new AutoCloseableNode(MyNodeConfig.vegaConfig());
		InjectorUtil.injectMembers(this);
		//---
		try (final Jedis jedis = redisConnector.getClient()) {
			jedis.flushAll();
		}
		mockIdentities.initData();
		account1Uri = MockIdentities.createAccountURI("1");

		//on triche un peu, car AcountGroup n'est pas un KeyConcept
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(AccountGroup.class);
		keyConcept1Uri = UID.of(dtDefinition, "10");
		keyConcept2Uri = UID.of(dtDefinition, "20");
		CONCEPT_KEY_NAME = dtDefinition.getClassSimpleName();

		preTestLogin();
	}

	@AfterEach
	public void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private void preTestLogin() {
		RestAssured.registerParser("plain/text", Parser.TEXT);
		RestAssured.given()
				.filter(sessionFilter)
				.get("/test/login?id=1");
	}

	@Test
	public void testGetComments() {
		final Comment comment = Comment.builder()
				.withAuthor(account1Uri)
				.withMsg("Lorem ipsum")
				.build();
		commentServices.publish(account1Uri, comment, keyConcept1Uri);

		//Check we got this comment
		RestAssured.given().filter(sessionFilter)
				.expect()
				.body("size()", Matchers.greaterThanOrEqualTo(1))
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/api/comments?concept=" + CONCEPT_KEY_NAME + "&id=" + keyConcept1Uri.getId());
	}

	@Test
	public void testPublishComment() {
		final Comment newComment = Comment.builder()
				.withAuthor(account1Uri)
				.withMsg("Lorem ipsum")
				.build();

		RestAssured.given().filter(sessionFilter)
				.body(commentToMap(newComment))
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.post("/x/comment/api/comments?concept=" + CONCEPT_KEY_NAME + "&id=" + keyConcept1Uri.getId());

		RestAssured.given().filter(sessionFilter)
				.expect()
				.body("size()", Matchers.greaterThanOrEqualTo(1))
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/api/comments?concept=" + CONCEPT_KEY_NAME + "&id=" + keyConcept1Uri.getId());
	}

	@Test
	public void testEditComment() {
		final Comment newComment = Comment.builder()
				.withAuthor(account1Uri)
				.withMsg("Lorem ipsum")
				.build();

		RestAssured.given().filter(sessionFilter)
				.body(commentToMap(newComment))
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.post("/x/comment/api/comments?concept=" + CONCEPT_KEY_NAME + "&id=" + keyConcept1Uri.getId());

		final Response response = RestAssured.given().filter(sessionFilter)
				.expect()
				.body("size()", Matchers.greaterThanOrEqualTo(1))
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/api/comments?concept=" + CONCEPT_KEY_NAME + "&id=" + keyConcept1Uri.getId());
		final String uuid = response.body().path("get(0).uuid");

		final Comment editComment = Comment.builder()
				.withUuid(UUID.fromString(uuid))
				.withAuthor(account1Uri)
				.withCreationDate(newComment.getCreationDate())
				.withMsg("edited Lorem ipsum edited")
				.build();

		RestAssured.given().filter(sessionFilter)
				.body(commentToMap(editComment))
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.put("/x/comment/api/comments/" + uuid);

		RestAssured.given().filter(sessionFilter)
				.expect()
				.body("size()", Matchers.greaterThanOrEqualTo(1))
				.body("get(0).msg", Matchers.equalTo("edited Lorem ipsum edited"))
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/api/comments?concept=" + CONCEPT_KEY_NAME + "&id=" + keyConcept1Uri.getId());

	}

	@Test
	public void testSeparationComment() {
		final Comment newComment = Comment.builder()
				.withAuthor(account1Uri)
				.withMsg("Lorem ipsum")
				.build();

		RestAssured.given().filter(sessionFilter)
				.body(commentToMap(newComment))
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.post("/x/comment/api/comments?concept=" + CONCEPT_KEY_NAME + "&id=" + keyConcept1Uri.getId());

		RestAssured.given().filter(sessionFilter)
				.expect()
				.body("size()", Matchers.equalTo(0))
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/api/comments?concept=" + CONCEPT_KEY_NAME + "&id=" + keyConcept2Uri.getId());
	}

	@Test
	public void testGetStatus() {
		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/status");
	}

	@Test
	public void testGetStats() {
		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/stats");
	}

	@Test
	public void testGetConfig() {
		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/config");
	}

	@Test
	public void testGetHelp() {
		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.log().ifError()
				.when()
				.get("/x/comment/help");
	}

	/*private static String convertDate(final String dateStr) throws ParseException {
		if (dateStr == null) {
			return null;
		}
		final Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateStr);
		return convertDate(date);
	}*/

	private static String convertDate(final Instant instant) {
		return instant == null ? null : UTCDateUtil.formatInstant(instant);
	}

	private static Map<String, Object> commentToMap(final Comment comment) {
		return new MapBuilder<String, Object>()
				.put("uuid", comment.getUuid())
				.put("author", comment.getAuthor().getId())
				.put("msg", comment.getMsg())
				.put("creationDate", convertDate(comment.getCreationDate()))
				.putNullable("lastModified", convertDate(comment.getLastModified()))
				.build();
	}
}
