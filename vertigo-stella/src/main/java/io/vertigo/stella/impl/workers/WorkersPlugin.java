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
package io.vertigo.stella.impl.workers;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.stella.impl.work.WorkItem;

/**
 * NodePlugin
 * @author pchretien
 */
public interface WorkersPlugin extends Plugin {

	/**
	 * Polling workitem.
	 * @param workType Type de tache
	 * @return Workitem or null (if timeout)
	 * @param <R> result
	 * @param <W> work
	 */
	<R, W> WorkItem<R, W> pollWorkItem(final String nodeId, final String workType);

	/**
	 * Send result or error if execution failed
	 * @param workId WorkId
	 * @param result Result (not null if execution succeeded)
	 * @param error Error ( not null if execution failed)
	 * @param <R> result
	 */
	<R> void putResult(final String workId, R result, Throwable error);

	void putStart(final String workId);
}
