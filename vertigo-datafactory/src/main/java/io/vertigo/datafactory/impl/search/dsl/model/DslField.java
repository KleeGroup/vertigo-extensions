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
package io.vertigo.datafactory.impl.search.dsl.model;

import io.vertigo.core.lang.Assertion;

/**
 * Single field definition.
 * (preBody)(fieldName)(postBody)
 * @author npiedeloup
 */
public final class DslField {
	private final String preBody;
	private final String fieldName;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param fieldName Index's fieldName
	 * @param postBody String after body
	 */
	public DslField(final String preBody, final String fieldName, final String postBody) {
		Assertion.check()
				.isNotNull(preBody)
				.isNotNull(fieldName)
				.isNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.fieldName = fieldName;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return preBody + fieldName + postBody;
	}

	/**
	 * @return preBody
	 */
	public String getPreBody() {
		return preBody;
	}

	/**
	 * @return fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @return postBody
	 */
	public String getPostBody() {
		return postBody;
	}

}
