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
package io.vertigo.orchestra.definitions;

import java.util.Map;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;

/**
 * Strategy for trigerring the process.
 * @author mlaroche
 *
 */
public final class ProcessTriggeringStrategy {
	private final Optional<String> cronExpression;
	private final Map<String, String> initialParams;
	private final boolean multiExecution;
	private final int rescuePeriodInSeconds;

	/**
	 * Constructor.
	 * @param cronExpression an optional cronexpression
	 * @param initialParams initialsParams added to the firstWorkspace
	 * @param multiExecution if the process accepts multiexecution at the same time
	 * @param rescuePeriodInSeconds the time in seconds a planification is still valid
	 */
	ProcessTriggeringStrategy(
			final Optional<String> cronExpression,
			final Map<String, String> initialParams,
			final boolean multiExecution,
			final int rescuePeriodInSeconds) {
		Assertion.check()
				.isNotNull(cronExpression)
				.isNotNull(initialParams);
		// --
		this.cronExpression = cronExpression;
		this.initialParams = initialParams;
		this.multiExecution = multiExecution;
		this.rescuePeriodInSeconds = rescuePeriodInSeconds;
	}

	public Optional<String> getCronExpression() {
		return cronExpression;
	}

	public Map<String, String> getInitialParams() {
		return initialParams;
	}

	public boolean isMultiExecution() {
		return multiExecution;
	}

	public int getRescuePeriod() {
		return rescuePeriodInSeconds;
	}

}
