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
package io.vertigo.social.notification;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.account.AccountManager;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.data.MockIdentities;
import io.vertigo.social.impl.notification.NotificationPlugin;
import io.vertigo.social.plugins.notification.memory.MemoryNotificationPlugin;
import io.vertigo.social.plugins.notification.redis.RedisNotificationPlugin;

public abstract class AbstractNotificationManagerTest {

	@Inject
	private MockIdentities mockIdentities;
	@Inject
	private AccountManager identityManager;
	@Inject
	private NotificationManager NotificationManager;

	private UID<Account> accountUID0;
	private UID<Account> accountUID1;
	private UID<Account> accountUID2;
	private UID<AccountGroup> groupURI;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		accountUID0 = createAccountUID("0");
		accountUID1 = createAccountUID("1");
		accountUID2 = createAccountUID("2");
		groupURI = UID.of(AccountGroup.class, "100");

		mockIdentities.initData();
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	protected abstract NodeConfig buildNodeConfig();

	protected final Node getApp() {
		return node;
	}

	private static UID<Account> createAccountUID(final String id) {
		return UID.of(Account.class, id);
	}

	@Test
	public void testNotifications() {

		for (int i = 0; i < 10; i++) {
			final Notification notification = Notification.builder()
					.withSender(accountUID0.urn())
					.withType("Test")
					.withTitle("news")
					.withContent("discover this amazing node !!")
					.withTargetUrl("#keyConcept@2")
					.build();
			NotificationManager.send(notification, identityManager.getAccountUIDs(groupURI));
		}

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(10, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(10, NotificationManager.getCurrentNotifications(accountUID2).size());
	}

	@Test
	public void testNotificationsWithRemove() {
		final Notification notification = Notification.builder()
				.withSender(accountUID0.urn())
				.withType("Test")
				.withTitle("news")
				.withTargetUrl("#keyConcept@2")
				.withContent("discover this amazing node !!")
				.build();

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID2).size());

		NotificationManager.send(notification, identityManager.getAccountUIDs(groupURI));

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());

		NotificationManager.remove(accountUID1, NotificationManager.getCurrentNotifications(accountUID1).get(0).getUuid());

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());

	}

	@Test
	public void testNotificationsWithRemoveFromTargetUrl() {
		final Notification notification = Notification.builder()
				.withSender(accountUID0.urn())
				.withType("Test")
				.withTitle("news")
				.withTargetUrl("#keyConcept@2")
				.withContent("discover this amazing node !!")
				.build();

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID2).size());

		NotificationManager.send(notification, identityManager.getAccountUIDs(groupURI));

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());

		NotificationManager.removeAll("Test", "#keyConcept@2");

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID2).size());

	}

	@Test
	public void testNotificationsUpdateUserContent() {
		final Notification notification = Notification.builder()
				.withSender(accountUID0.urn())
				.withType("Test")
				.withTitle("news")
				.withTargetUrl("#keyConcept@2")
				.withContent("discover this amazing node !!")
				.withUserContent("defaultUserContent")
				.build();

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID2).size());

		NotificationManager.send(notification, identityManager.getAccountUIDs(groupURI));

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());

		NotificationManager.updateUserContent(accountUID1, notification.getUuid(), "myUserContent1");

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals("myUserContent1", NotificationManager.getCurrentNotifications(accountUID1).get(0).getUserContent().get());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());
		Assertions.assertEquals("defaultUserContent", NotificationManager.getCurrentNotifications(accountUID2).get(0).getUserContent().get());

		NotificationManager.updateUserContent(accountUID2, notification.getUuid(), "myUserContent2");

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals("myUserContent1", NotificationManager.getCurrentNotifications(accountUID1).get(0).getUserContent().get());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());
		Assertions.assertEquals("myUserContent2", NotificationManager.getCurrentNotifications(accountUID2).get(0).getUserContent().get());

		NotificationManager.updateUserContent(accountUID2, notification.getUuid(), ""); // means no userContent => Optional.empty

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals("myUserContent1", NotificationManager.getCurrentNotifications(accountUID1).get(0).getUserContent().get());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());
		Assertions.assertEquals(false, NotificationManager.getCurrentNotifications(accountUID2).get(0).getUserContent().isPresent());

		NotificationManager.updateUserContent(accountUID2, notification.getUuid(), null); // means no userContent => Optional.empty

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals("myUserContent1", NotificationManager.getCurrentNotifications(accountUID1).get(0).getUserContent().get());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());
		Assertions.assertEquals(false, NotificationManager.getCurrentNotifications(accountUID2).get(0).getUserContent().isPresent());
	}

	@Test
	public void testNotificationsWithTTL() {
		final Notification notification = Notification.builder()
				.withSender(accountUID0.urn())
				.withType("Test")
				.withTitle("news")
				.withTargetUrl("#keyConcept@2")
				.withTTLInSeconds(5) //5s
				.withContent("discover this amazing node !!")
				.build();

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID2).size());

		NotificationManager.send(notification, identityManager.getAccountUIDs(groupURI));

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());

		sleep(3000);

		//not expired yet
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());

		sleep(3000);

		//expired
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID2).size());

	}

	@Test
	public void testNotificationsWithTTLDaemon() {
		final Notification notification = Notification.builder()
				.withSender(accountUID0.urn())
				.withType("Test")
				.withTitle("news")
				.withTargetUrl("#keyConcept@2")
				.withTTLInSeconds(4) //4s
				.withContent("discover this amazing node !!")
				.build();

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID2).size());

		NotificationManager.send(notification, identityManager.getAccountUIDs(groupURI));

		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(1, NotificationManager.getCurrentNotifications(accountUID2).size());

		sleep(4100);

		final NotificationPlugin notificationPlugin = Node.getNode().getComponentSpace().resolve("notificationPlugin", NotificationPlugin.class);
		if (notificationPlugin instanceof RedisNotificationPlugin) {
			((RedisNotificationPlugin) notificationPlugin).cleanTooOldNotifications();
		} else if (notificationPlugin instanceof MemoryNotificationPlugin) {
			((MemoryNotificationPlugin) notificationPlugin).cleanTooOldNotifications();
		}

		//Daemon expired
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID0).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID1).size());
		Assertions.assertEquals(0, NotificationManager.getCurrentNotifications(accountUID2).size());

	}

	private static void sleep(final int time) {
		try {
			Thread.sleep(time);
		} catch (final InterruptedException e) {
			//nothing
		}
	}
}
