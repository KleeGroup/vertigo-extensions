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
 * Range query definition.
 * (preBody)[\[\{](termQuery|fixedQuery) to (termQuery|fixedQuery)[\}\]](postBody)
 * @author npiedeloup
 */
public final class DslRangeQuery implements DslQuery {
	private final String preBody;
	private final String startRange;
	private final DslQuery startQueryDefinitions;
	private final DslQuery endQueryDefinitions;
	private final String endRange;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param startQueryDefinitions Start query
	 * @param endQueryDefinitions End query
	 * @param postBody String after body
	 */
	public DslRangeQuery(
			final String preBody,
			final String startRange,
			final DslQuery startQueryDefinitions,
			final DslQuery endQueryDefinitions,
			final String endRange,
			final String postBody) {
		Assertion.check()
				.isNotNull(preBody)
				.isNotBlank(startRange)
				.isNotNull(startQueryDefinitions)
				.isNotNull(endQueryDefinitions)
				.isNotBlank(endRange)
				.isNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.startRange = startRange;
		this.startQueryDefinitions = startQueryDefinitions;
		this.endQueryDefinitions = endQueryDefinitions;
		this.endRange = endRange;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(preBody).append(startRange)
				.append(startQueryDefinitions)
				.append(" to ")
				.append(endQueryDefinitions)
				.append(endRange).append(postBody)
				.toString();
	}

	/**
	 * @return preBody
	 */
	public String getPreBody() {
		return preBody;
	}

	/**
	 * @return startRange
	 */
	public String getStartRange() {
		return startRange;
	}

	/**
	 * @return startQueryDefinitions
	 */
	public DslQuery getStartQueryDefinitions() {
		return startQueryDefinitions;
	}

	/**
	 * @return endQueryDefinitions
	 */
	public DslQuery getEndQueryDefinitions() {
		return endQueryDefinitions;
	}

	/**
	 * @return endRange
	 */
	public String getEndRange() {
		return endRange;
	}

	/**
	 * @return postBody
	 */
	public String getPostBody() {
		return postBody;
	}
}
