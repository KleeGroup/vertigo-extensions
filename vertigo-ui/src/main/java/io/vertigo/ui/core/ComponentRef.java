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
package io.vertigo.ui.core;

import java.io.Serializable;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;

/**
 * Référence vers un composant.
 * Permet d'assurer le référencement du composant hors de l'injecteur.
 * Et eventuellement le référencement reporté au premier appel (lazyLoading).
 *
 * @author pchretien, npiedeloup
 * @param <T> Type du composant
 */
public final class ComponentRef<T> implements Serializable {
	private static final long serialVersionUID = -3692640153710870256L;
	private transient T instance;
	private final String componentId;
	private final Class<T> componentClazz;

	/**
	 * Constructeur.
	 * @param componentClazz Class du composant
	 * @param lazy Si référencement à la première demande
	 */
	private ComponentRef(final Class<T> componentClazz, final boolean lazy) {
		Assertion.check().isNotNull(componentClazz);
		//-----
		componentId = null;
		this.componentClazz = componentClazz;
		if (!lazy) {
			get();
		}
	}

	/**
	 * @param componentClazz Type du composant
	 * @return Référence vers ce composant
	 */
	public static <T> ComponentRef<T> makeRef(final Class<T> componentClazz) {
		return new ComponentRef<>(componentClazz, false);
	}

	/**
	 * @param componentClazz Type du composant
	 * @return Référence résolue en lazy loading vers ce composant
	 */
	public static <T> ComponentRef<T> makeLazyRef(final Class<T> componentClazz) {
		return new ComponentRef<>(componentClazz, true);
	}

	/**
	 * @return Element pointé par la référence
	 */
	//le synchronized à peu d'impact sur une référence qui à vocation à être instanciée à chaque usage
	// TODO a voir si on le retire.
	public synchronized T get() {
		if (instance == null) {
			if (componentId != null) {
				instance = Node.getNode().getComponentSpace().resolve(componentId, componentClazz);
			} else {
				instance = Node.getNode().getComponentSpace().resolve(componentClazz);
			}
		}
		return instance;
	}
}
