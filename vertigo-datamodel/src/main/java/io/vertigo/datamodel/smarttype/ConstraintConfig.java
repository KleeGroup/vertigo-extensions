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
package io.vertigo.datamodel.smarttype;

import io.vertigo.datamodel.structure.definitions.Constraint;

public class ConstraintConfig {

	private final Class<? extends Constraint> clazz;
	private final String arg;
	private final String msg;

	public ConstraintConfig(final Class<? extends Constraint> clazz, final String arg, final String msg) {
		this.clazz = clazz;
		this.arg = arg;
		this.msg = msg;
	}

	public Class<? extends Constraint> getConstraintClass() {
		return clazz;
	}

	public String getArg() {
		return arg;
	}

	public String getMsg() {
		return msg;
	}

}
