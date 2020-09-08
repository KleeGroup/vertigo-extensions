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
package io.vertigo.social.plugins.handle.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.handle.Handle;
import io.vertigo.social.impl.handle.HandlePlugin;

public class MemoryHandlePlugin implements HandlePlugin {

	private final Map<UID, Handle> handlesPerUid = new HashMap<>();
	private final Map<String, Handle> handlesPerCode = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public void add(final List<Handle> handles) {
		handles.forEach(handle -> {
			handlesPerUid.put(handle.getUid(), handle);
			handlesPerCode.put(handle.getCode(), handle);
		});
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final List<UID> uids) {
		uids.forEach(uid -> {
			final Handle handle = handlesPerUid.remove(uid);
			if (handle != null) {
				handlesPerCode.remove(handle.getCode());
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public List<Handle> search(final String prefix) {
		return handlesPerUid.values().stream()
				.filter(handle -> handle.getCode().startsWith(prefix))
				.limit(10)
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public Handle getByCode(final String handleCode) {
		return handlesPerCode.get(handleCode);
	}

	/** {@inheritDoc} */
	@Override
	public Handle getByUid(final UID uid) {
		return handlesPerUid.get(uid);
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll() {
		handlesPerUid.clear();
		handlesPerCode.clear();
	}

}
