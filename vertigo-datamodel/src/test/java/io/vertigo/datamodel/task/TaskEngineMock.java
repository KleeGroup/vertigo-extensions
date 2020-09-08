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
package io.vertigo.datamodel.task;

import io.vertigo.datamodel.task.model.TaskEngine;

/**
 * This class defines the engine with several inputs and an output.
 * Each input is a required integer
 *
 * This class is a parameterized function : (Integer, Integer, Integer) -> Integer
 * The parameter is defined by the "request" and represents the operation to execute.
 * @author dchallas
 */
public final class TaskEngineMock extends TaskEngine {
	/** entier 1. */
	public static final String ATTR_IN_INT_1 = "attrInInt1";
	/** entier 2. */
	public static final String ATTR_IN_INT_2 = "attrInInt2";
	/** entier 3. */
	public static final String ATTR_IN_INT_3 = "attrInInt3";
	/** Somme. */
	public static final String ATTR_OUT = "attrOut";

	private Integer getValue1() {
		return getValue(ATTR_IN_INT_1);
	}

	private Integer getValue2() {
		return getValue(ATTR_IN_INT_2);
	}

	private Integer getValue3() {
		return getValue(ATTR_IN_INT_3);
	}

	private void setOutput(final Integer result) {
		setResult(result);
	}

	/** {@inheritDoc} */
	@Override
	public void execute() {
		final int outPut;
		switch (getTaskDefinition().getRequest()) {
			case "+":
				outPut = getValue1() + getValue2() + getValue3();
				break;
			case "*":
				outPut = getValue1() * getValue2() * getValue3();
				break;
			default:
				throw new IllegalArgumentException("Operateur non reconnu.");
		}
		setOutput(outPut);
	}
}
