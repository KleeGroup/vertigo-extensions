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
package io.vertigo.commons.impl.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.commons.eventbus.Event;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.eventbus.EventBusSubscribed;
import io.vertigo.commons.eventbus.definitions.EventBusSubscriptionDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.component.AopPlugin;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.StringUtil;

/**
 * @author pchretien, npiedeloup
 */
public final class EventBusManagerImpl implements EventBusManager, Activeable, SimpleDefinitionProvider {
	private final List<EventBusSubscriptionDefinition> subscriptions = new ArrayList<>();
	private final List<Consumer<Event>> deadEventListeners = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public EventBusManagerImpl() {
		Node.getNode().registerPreActivateFunction(this::registerAllSubscribers);
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		// we need to unwrap the component to scan the real class and not the enhanced version
		final AopPlugin aopPlugin = Node.getNode().getNodeConfig().getBootConfig().getAopPlugin();
		return Node.getNode().getComponentSpace().keySet()
				.stream()
				.flatMap(id -> createEventSubscriptions(id, Node.getNode().getComponentSpace().resolve(id, CoreComponent.class), aopPlugin).stream())
				.collect(Collectors.toList());
	}

	/**
	 * Registers all methods annotated with @Suscriber on the object
	 * @param suscriberInstance
	 */
	private static List<EventBusSubscriptionDefinition> createEventSubscriptions(final String componentId, final CoreComponent subscriberInstance, final AopPlugin aopPlugin) {
		Assertion.check().isNotNull(subscriberInstance);
		//-----
		//1. search all methods
		return Stream.of(aopPlugin.unwrap(subscriberInstance).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(EventBusSubscribed.class))
				.map(method -> {
					Assertion.check()
							.isTrue(void.class.equals(method.getReturnType()), "subscriber's methods  of class {0} must be void instead of {1}", subscriberInstance.getClass(), method.getReturnType())
							.isTrue(method.getName().startsWith("on"), "subscriber's methods of class {0} must start with on", subscriberInstance.getClass())
							.isTrue(method.getParameterTypes().length == 1, "subscriber's methods of class {0} must be void onXXX(Event e)", subscriberInstance.getClass())
							.isTrue(Event.class.isAssignableFrom(method.getParameterTypes()[0]), "subscriber's methods of class {0} must be 'void onXXX(E extends Event)'", subscriberInstance.getClass());
					//-----
					//2. For each method register a listener
					final Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];
					final String subscriptionName = "Evt" + StringUtil.first2UpperCase(componentId) + "$" + StringUtil.first2LowerCase(eventType.getSimpleName());
					return new EventBusSubscriptionDefinition<>(subscriptionName, eventType, event -> ClassUtil.invoke(subscriberInstance, method, event));
				})
				.collect(Collectors.toList());

	}

	@Override
	public void start() {
		// nothing
	}

	@Override
	public void stop() {
		subscriptions.clear();
		deadEventListeners.clear();
	}

	private void registerAllSubscribers() {
		subscriptions.addAll(Node.getNode().getDefinitionSpace().getAll(EventBusSubscriptionDefinition.class));

	}

	/** {@inheritDoc} */
	@Override
	public void post(final Event event) {
		Assertion.check().isNotNull(event);
		//-----
		final long emitted = subscriptions.stream()
				.filter(subscription -> subscription.match(event))
				.peek(subscription -> subscription.getListener().accept(event))
				.count();

		//manages dead event
		if (emitted == 0) {
			deadEventListeners
					.forEach(deadEventlistener -> deadEventlistener.accept(event));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void registerDead(final Consumer<Event> eventConsumer) {
		Assertion.check().isNotNull(eventConsumer);
		//-----
		deadEventListeners.add(eventConsumer);
	}
}
