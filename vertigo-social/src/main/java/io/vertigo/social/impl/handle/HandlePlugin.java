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
package io.vertigo.social.impl.handle;

import java.util.List;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.handle.Handle;

public interface HandlePlugin extends Plugin {

	void add(List<Handle> handles);

	List<Handle> search(String prefix);

	Handle getByCode(String handleCode);

	Handle getByUid(UID uid);

	void remove(List<UID> uids);

	void removeAll();

}
