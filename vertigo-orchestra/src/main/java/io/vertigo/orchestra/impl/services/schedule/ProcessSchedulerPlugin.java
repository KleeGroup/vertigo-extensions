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
package io.vertigo.orchestra.impl.services.schedule;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.services.execution.ProcessExecutor;
import io.vertigo.orchestra.services.schedule.ProcessScheduler;

/**
 * Plugin de gestion de la planification.
 *
 * @author mlaroche.
 * @version $Id$
 */
public interface ProcessSchedulerPlugin extends ProcessScheduler, Plugin {

	/**
	 * Retourne le type de processus géré par le plugin
	 * @return le type de processus géré
	 */
	ProcessType getHandledProcessType();

	void setProcessExecutor(ProcessExecutor processExecutor);
}
