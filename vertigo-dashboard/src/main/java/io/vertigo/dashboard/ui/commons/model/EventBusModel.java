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
package io.vertigo.dashboard.ui.commons.model;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.commons.eventbus.definitions.EventBusSubscriptionDefinition;
import io.vertigo.core.lang.Assertion;

public final class EventBusModel {
	private final Class eventType;
	private final List<EventBusSubscriptionDefinition> subscriptions;

	public EventBusModel(
			final Class eventType,
			final List<EventBusSubscriptionDefinition> subscriptions) {
		Assertion.check()
				.isNotNull(eventType)
				.isNotNull(subscriptions);
		//---
		this.eventType = eventType;
		this.subscriptions = subscriptions;
	}

	public String getName() {
		return eventType.getSimpleName();
	}

	public boolean isDeadEvent() {
		return subscriptions.isEmpty();
	}

	public List<String> getSubscribers() {
		return subscriptions
				.stream()
				.map(EventBusSubscriptionDefinition::getName)
				.collect(Collectors.toList());
	}

}
