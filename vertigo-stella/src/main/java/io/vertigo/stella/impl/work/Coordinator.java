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
package io.vertigo.stella.impl.work;

import java.util.concurrent.Future;

import io.vertigo.stella.master.WorkResultHandler;

/**
 * Interface d'un Worker threadsafe.
 * Permet d'exécuter un travail de façon
 * - synchrone
 * - asynchrone
 *
 * @author pchretien, npiedeloup
 */
public interface Coordinator {
	/**
	 * Exécution d'un travail de façon asynchrone.
	 * @param <W> Type de Work (Travail)
	 * @param <R> result type
	 * @param workItem Travail à exécuter
	 * @param workResultHandler Result handler
	 * @return Future for this result
	 */
	<W, R> Future<R> submit(final WorkItem<W, R> workItem, final WorkResultHandler<R> workResultHandler);
}
