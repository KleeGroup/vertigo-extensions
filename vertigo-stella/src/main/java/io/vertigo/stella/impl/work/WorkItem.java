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

import io.vertigo.core.lang.Assertion;
import io.vertigo.stella.work.WorkEngine;

/**
 *
 * @author pchretien
 * @param <R> result
 * @param <W> work
 */
public final class WorkItem<W, R> {
	private final W work;
	private final Class<? extends WorkEngine<W, R>> workEngineClass;
	private final String id;

	/**
	 * Constructor.
	 * This workItem is used to define a synchronous work.
	 * @param id Id
	 * @param work Travail dont on représente l'état.
	 */
	public WorkItem(final String id, final W work, final Class<? extends WorkEngine<W, R>> workEngineClass) {
		Assertion.check()
				.isNotNull(id)
				//work can be null
				.isNotNull(workEngineClass);
		//-----
		this.id = id;
		this.work = work;
		this.workEngineClass = workEngineClass;
	}

	/**
	 * @return WorkItem id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Permet de récupérer les informations pour réaliser un traitement.
	 * @return le work
	 */
	public W getWork() {
		return work;
	}

	/**
	 * @return WorkEngine provider
	 */
	public Class<? extends WorkEngine<W, R>> getWorkEngineClass() {
		return workEngineClass;
	}
}
