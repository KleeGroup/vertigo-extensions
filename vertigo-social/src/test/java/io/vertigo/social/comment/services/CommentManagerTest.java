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
package io.vertigo.social.comment.services;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.social.MyNodeConfig;
import io.vertigo.social.comment.Comment;
import io.vertigo.social.comment.CommentManager;
import io.vertigo.social.data.MockIdentities;
import redis.clients.jedis.Jedis;

public class CommentManagerTest {

	@Inject
	private MockIdentities mockIdentities;
	@Inject
	private CommentManager commentServices;
	@Inject
	private RedisConnector redisConnector;

	private AutoCloseableNode node;
	private UID<KeyConcept> keyConcept1Uri;

	private UID<Account> accountURI1;

	@BeforeEach
	public void setUp() {
		node = new AutoCloseableNode(MyNodeConfig.vegaConfig());
		InjectorUtil.injectMembers(this);
		try (final Jedis jedis = redisConnector.getClient()) {
			jedis.flushAll();
		}
		accountURI1 = MockIdentities.createAccountURI("1");
		mockIdentities.initData();

		//on triche un peu, car AcountGroup n'est pas un KeyConcept
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(AccountGroup.class);
		keyConcept1Uri = UID.of(dtDefinition, "10");
		keyConcept1Uri = UID.of(dtDefinition, "20");
	}

	@AfterEach
	public void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	@Test
	public void testComments() {
		final Comment comment = Comment.builder()
				.withAuthor(accountURI1)
				.withMsg("Tu as bien fait de partir, Arthur Rimbaud! Tes dix-huit ans réfractaires à l'amitié, à la malveillance, à la sottise des poètes de Paris ainsi qu'au ronronnement d'abeille stérile de ta famille ardennaise un peu folle, tu as bien fait de les éparpiller aux vents du large..")
				.build();
		for (int i = 0; i < 10; i++) {
			commentServices.publish(accountURI1, comment, keyConcept1Uri);
		}

		Assertions.assertEquals(10, commentServices.getComments(keyConcept1Uri).size());
	}
}
