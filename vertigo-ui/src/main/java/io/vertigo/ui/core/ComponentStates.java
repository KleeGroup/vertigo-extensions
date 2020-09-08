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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;

/**
 * Map of componentStates that need to be stored
 * @author mlaroche
 */
public final class ComponentStates extends HashMap<String, Serializable> {

	private static final long serialVersionUID = -8925934036136725151L;

	public ComponentState addComponentState(final String componentId) {
		Assertion.check().isNotBlank(componentId);
		//---
		final ComponentState componentState = new ComponentState();
		put(componentId, componentState);
		return componentState;
	}

	public static class ComponentState extends HashMap<String, Serializable> {

		private static final long serialVersionUID = -162303456261091792L;

		public List<Serializable> addList(final String key) {
			Assertion.check().isNotBlank(key);
			//---
			final ArrayList<Serializable> list = new ArrayList<>();
			put(key, list);
			return list;
		}

		public HashMap<String, Serializable> addObjectToList(final String listKey, final Map object) {
			Assertion.check()
					.isNotBlank(listKey)
					.isNotNull(object);
			//---
			final HashMap<String, Serializable> modifiableObject = new HashMap<>(object);// just to have a modifiable map
			((List) get(listKey)).add(modifiableObject);
			return modifiableObject;
		}

		public HashMap<String, Serializable> addObject(final String key) {
			return addObject(key, Collections.emptyMap());
		}

		public void addPrimitive(final String key, final Serializable value) {
			Assertion.check().isNotBlank(key);
			put(key, value);
		}

		public HashMap<String, Serializable> addObject(final String key, final Map object) {
			Assertion.check().isNotBlank(key);
			//---
			final HashMap<String, Serializable> map = new HashMap<>(object);
			put(key, map);
			return map;
		}

	}

}
