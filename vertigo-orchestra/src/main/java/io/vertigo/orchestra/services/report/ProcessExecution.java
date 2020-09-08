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
package io.vertigo.orchestra.services.report;

import java.io.Serializable;
import java.time.Instant;

import io.vertigo.core.lang.Assertion;

public final class ProcessExecution implements Serializable {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private final Long preId;
	private final Instant beginTime;
	private final Instant endTime;
	private final Integer executionTime;
	private final String status;
	private final Boolean checked;
	private final Instant checkingDate;
	private final String checkingComment;
	private final Boolean hasLogFile;

	/**
	 * Constructor.
	 * @param preId id of execution
	 * @param beginTime begin time
	 * @param endTime end time
	 * @param executionTime execution time in seconds
	 * @param status status of the execution
	 * @param checked if the execution is check (for errors)
	 * @param checkingDate the date of checking
	 * @param checkingComment the checking comm
	 * @param hasLogFile if the execution has an associated log
	 */
	public ProcessExecution(
			final Long preId,
			final Instant beginTime,
			final Instant endTime,
			final Integer executionTime,
			final String status,
			final Boolean checked,
			final Instant checkingDate,
			final String checkingComment,
			final Boolean hasLogFile) {
		Assertion.check().isNotNull(preId);
		// ---
		this.preId = preId;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.executionTime = executionTime;
		this.status = status;
		this.checked = checked;
		this.checkingDate = checkingDate;
		this.checkingComment = checkingComment;
		this.hasLogFile = hasLogFile;
	}

	public Long getPreId() {
		return preId;
	}

	public Instant getBeginTime() {
		return beginTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public Integer getExecutionTime() {
		return executionTime;
	}

	public String getStatus() {
		return status;
	}

	public Boolean getChecked() {
		return checked;
	}

	public Instant getCheckingDate() {
		return checkingDate;
	}

	public String getCheckingComment() {
		return checkingComment;
	}

	public Boolean getHasLogFile() {
		return hasLogFile;
	}

}
