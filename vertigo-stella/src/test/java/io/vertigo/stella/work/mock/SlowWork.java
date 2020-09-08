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
package io.vertigo.stella.work.mock;

import java.io.Serializable;

public final class SlowWork implements Serializable {
	private static final long serialVersionUID = -5638180614179332598L;
	private final long sleepTime;

	public SlowWork(final long sleepTime) {
		this.sleepTime = sleepTime;
	}

	long getSleepTime() {
		return sleepTime;
	}
}
