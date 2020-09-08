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
package io.vertigo.datastore.entitystore;

import io.vertigo.commons.eventbus.Event;
import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.UID;

/**
 * This class defines the event that is emitted when the store deals with an object identified by an uri.
 *
 * @author pchretien
 */
public final class StoreEvent implements Event {
	/**
	 * Type of event.
	 */
	public enum Type {
		/** Creation. */
		CREATE,
		/** Update. */
		UPDATE,
		/** Delete. */
		DELETE
	}

	private final Type type;
	private final UID uid;

	/**
	 * Constructor.
	 * @param type Store type
	 * @param uid UID of stored element
	 */
	public StoreEvent(final Type type, final UID uid) {
		Assertion.check()
				.isNotNull(type)
				.isNotNull(uid);
		//-----
		this.type = type;
		this.uid = uid;
	}

	/**
	 * @return UID of stored element
	 */
	public UID getUID() {
		return uid;
	}

	/**
	 * @return Store type
	 */
	public Type getType() {
		return type;
	}
}
