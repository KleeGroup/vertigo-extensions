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

import java.util.HashMap;
import java.util.Map;

import io.vertigo.core.lang.Assertion;

/**
 * Util to read workType conf.
 * <workType>^<workerCount>;...
 * @author npiedeloup
 */
public final class WorkDispatcherConfUtil {

	private WorkDispatcherConfUtil() {
		//private
	}

	/**
	 * Read workTypes configuration to Map<workTypeName, workDispatsherCount>.
	 * @param workTypes Initial conf
	 * @return configuration as Map<workTypeName, workDispatsherCount>.
	 */
	public static Map<String, Integer> readWorkTypeConf(final String workTypes) {
		Assertion.check().isNotBlank(workTypes);
		//-----
		final Map<String, Integer> workTypesConf = new HashMap<>();
		for (final String workTypeConf : workTypes.trim().split(";")) {
			final int dispatcherCountIdx = workTypeConf.indexOf('^');
			Assertion.check().isTrue(dispatcherCountIdx > 0, "Each workType must set the dispatcher count");
			//-----
			final int dispatcherCount = Integer.parseInt(workTypeConf.substring(dispatcherCountIdx + 1));
			final String workType = workTypeConf.substring(0, dispatcherCountIdx);
			workTypesConf.put(workType, dispatcherCount);
		}
		return workTypesConf;
	}

}
