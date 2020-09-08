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
package io.vertigo.commons.eventbus.definitions;

import java.util.function.Consumer;

import io.vertigo.commons.eventbus.Event;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;

/**
 * This defintion defines a subscripter in the eventbus pattern.
 * A endpoint is  :
 * 			- a type of event
 * 			- a way to consume the event.
 * @author pchretien
 *
 * @param <E> type of event
 */
@DefinitionPrefix(EventBusSubscriptionDefinition.PREFIX)
public final class EventBusSubscriptionDefinition<E extends Event> extends AbstractDefinition {
	public static final String PREFIX = "Evt";
	private final Class<E> eventType;
	private final Consumer<E> eventListener;

	/**
	 * Constructor
	 * @param name the name (must be unique)
	 * @param eventType the type of event subscribed
	 * @param eventListener the consumer of the event (what will be done with it)
	 */
	public EventBusSubscriptionDefinition(final String name, final Class<E> eventType, final Consumer<E> eventListener) {
		super(name);
		//---
		Assertion.check()
				.isNotNull(eventType)
				.isNotNull(eventListener);
		//---
		this.eventType = eventType;
		this.eventListener = eventListener;
	}

	/**
	 * Return if an event matches the event type of the actual subscription
	 * @param event the type of event to test
	 * @return true if it matches
	 */
	public boolean match(final Event event) {
		Assertion.check().isNotNull(event);
		//-----
		return eventType.isInstance(event);
	}

	/**
	 * Return the consumer of the event
	 * @return the consumer
	 */
	public Consumer<E> getListener() {
		return eventListener;
	}

	public Class<E> getEventType() {
		return eventType;
	}
}
