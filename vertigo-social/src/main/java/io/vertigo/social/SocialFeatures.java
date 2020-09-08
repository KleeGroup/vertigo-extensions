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
package io.vertigo.social;

import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.social.comment.CommentManager;
import io.vertigo.social.handle.HandleManager;
import io.vertigo.social.impl.comment.CommentManagerImpl;
import io.vertigo.social.impl.handle.HandleManagerImpl;
import io.vertigo.social.impl.mail.MailManagerImpl;
import io.vertigo.social.impl.notification.NotificationManagerImpl;
import io.vertigo.social.mail.MailManager;
import io.vertigo.social.notification.NotificationManager;
import io.vertigo.social.plugins.comment.memory.MemoryCommentPlugin;
import io.vertigo.social.plugins.comment.redis.RedisCommentPlugin;
import io.vertigo.social.plugins.handle.memory.MemoryHandlePlugin;
import io.vertigo.social.plugins.handle.redis.RedisHandlePlugin;
import io.vertigo.social.plugins.mail.javax.JavaxSendMailPlugin;
import io.vertigo.social.plugins.notification.memory.MemoryNotificationPlugin;
import io.vertigo.social.plugins.notification.redis.RedisNotificationPlugin;
import io.vertigo.social.webservices.account.AccountWebServices;
import io.vertigo.social.webservices.comment.CommentWebServices;
import io.vertigo.social.webservices.handle.HandleWebServices;
import io.vertigo.social.webservices.notification.NotificationWebServices;

/**
 * Defines the 'social' extension
 * @author pchretien
 */
public final class SocialFeatures extends Features<SocialFeatures> {

	private boolean commentsEnabled;
	private boolean notificationsEnabled;
	private boolean handlesEnabled;
	private boolean webapiEnabled;
	private boolean mailEnabled;

	/**
	 * Constructor;
	 */
	public SocialFeatures() {
		super("vertigo-social");
	}

	/**
	 * Activates notifications
	 * @return the features
	 */
	@Feature("notifications")
	public SocialFeatures withNotifications() {
		notificationsEnabled = true;
		return this;
	}

	@Feature("notifications.redis")
	public SocialFeatures withRedisNotifications(final Param... params) {
		getModuleConfigBuilder().addPlugin(RedisNotificationPlugin.class, params);
		return this;
	}

	@Feature("notifications.memory")
	public SocialFeatures withMemoryNotifications() {
		getModuleConfigBuilder().addPlugin(MemoryNotificationPlugin.class);
		return this;
	}

	/**
	 * Activates comments
	 * @return the features
	 */
	@Feature("comments")
	public SocialFeatures withComments() {
		commentsEnabled = true;
		return this;
	}

	@Feature("comments.redis")
	public SocialFeatures withRedisComments(final Param... params) {
		getModuleConfigBuilder().addPlugin(RedisCommentPlugin.class, params);
		return this;
	}

	@Feature("comments.memory")
	public SocialFeatures withMemoryComments() {
		getModuleConfigBuilder().addPlugin(MemoryCommentPlugin.class);
		return this;
	}

	/**
	 * Activates mail
	 * @return the features
	 */
	@Feature("mail")
	public SocialFeatures withMails() {
		mailEnabled = true;
		return this;
	}

	@Feature("mail.javax")
	public SocialFeatures withJavaxMail(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(JavaxSendMailPlugin.class, params);

		return this;

	}

	/**
	 * Activates handles
	 * @return the features
	 */
	@Feature("handles")
	public SocialFeatures withHandles() {
		handlesEnabled = true;
		return this;
	}

	@Feature("handles.redis")
	public SocialFeatures withRedisHandles(final Param... params) {
		getModuleConfigBuilder().addPlugin(RedisHandlePlugin.class, params);
		return this;
	}

	@Feature("handles.memory")
	public SocialFeatures withMemoryHandles() {
		getModuleConfigBuilder().addPlugin(MemoryHandlePlugin.class);
		return this;
	}

	/**
	 * Activates comments
	 * @return the features
	 */
	@Feature("webapi")
	public SocialFeatures withWebApi() {
		webapiEnabled = true;
		return this;
	}

	@Override
	protected void buildFeatures() {
		if (notificationsEnabled) {
			getModuleConfigBuilder()
					.addComponent(NotificationManager.class, NotificationManagerImpl.class);
			if (webapiEnabled) {
				getModuleConfigBuilder()
						.addComponent(AccountWebServices.class)
						.addComponent(NotificationWebServices.class);
			}
		}
		if (commentsEnabled) {
			getModuleConfigBuilder()
					.addComponent(CommentManager.class, CommentManagerImpl.class);
			if (webapiEnabled) {
				getModuleConfigBuilder()
						.addComponent(CommentWebServices.class);
			}
		}
		if (handlesEnabled) {
			getModuleConfigBuilder()
					.addComponent(HandleManager.class, HandleManagerImpl.class);
			if (webapiEnabled) {
				getModuleConfigBuilder()
						.addComponent(HandleWebServices.class);
			}
		}
		if (mailEnabled) {
			getModuleConfigBuilder().addComponent(MailManager.class, MailManagerImpl.class);
		}

	}
}
