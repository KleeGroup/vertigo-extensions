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
package io.vertigo.datafactory.impl.search.dsl.rules;

import java.util.List;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.datafactory.impl.search.dsl.model.DslField;

/**
 * Parsing rule for Field.
 * (preField)(fieldName)(postField):
 * @author npiedeloup
 */
final class DslFieldRule extends AbstractRule<DslField, List<Object>> {

	DslFieldRule() {
		super(createMainRule());
	}

	private static PegRule<List<Object>> createMainRule() {
		return PegRules.sequence(
				DslSyntaxRules.PRE_MODIFIER_VALUE, //0
				DslSyntaxRules.FIELD_NAME, //1
				DslSyntaxRules.POST_MODIFIER_VALUE); //2
	}

	/** {@inheritDoc} */
	@Override
	protected DslField handle(final List<Object> parsing) {
		final String preField = (String) parsing.get(0);
		final String fieldName = (String) parsing.get(1);
		final String postField = (String) parsing.get(2);
		return new DslField(preField, fieldName, postField);
	}

}
