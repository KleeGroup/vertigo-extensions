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
package io.vertigo.orchestra.services.execution.engine;

import io.vertigo.orchestra.impl.services.execution.AbstractActivityEngine;
import io.vertigo.orchestra.services.execution.ActivityExecutionWorkspace;

/**
 * TODO : Description de la classe.
 *
 * @author mlaroche.
 * @version $Id$
 */
public class DumbLoggedActivityEngine extends AbstractActivityEngine {

	/** {@inheritDoc} */
	@Override
	public ActivityExecutionWorkspace execute(final ActivityExecutionWorkspace workspace) {
		getLogger().info("Info 1");
		getLogger().info("Info 2");
		workspace.setAttachment("/testPath");
		workspace.setSuccess();
		try {
			Thread.sleep(1000 * 5);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return workspace;
	}

}
