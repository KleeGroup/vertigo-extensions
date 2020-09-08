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
package io.vertigo.database.impl.sql.vendor.h2;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.StringUtil;
import io.vertigo.database.sql.vendor.SqlDialect;

final class H2SqlDialect implements SqlDialect {

	/** {@inheritDoc} */
	@Override
	public String createInsertQuery(
			final String idFieldName,
			final List<String> dataFieldsName,
			final String sequencePrefix,
			final String tableName) {
		Assertion.check()
				.isNotBlank(idFieldName)
				.isNotNull(dataFieldsName)
				.isNotBlank(sequencePrefix)
				.isNotBlank(tableName);
		//---
		return new StringBuilder()
				.append("insert into ").append(tableName).append(" (")
				.append(StringUtil.camelToConstCase(idFieldName)).append(", ")
				.append(dataFieldsName
						.stream()
						.map(StringUtil::camelToConstCase)
						.collect(Collectors.joining(", ")))
				.append(") values (")
				.append("nextval('").append(sequencePrefix).append(tableName).append("'), ")
				.append(dataFieldsName
						.stream()
						.map(fieldName -> " #dto." + fieldName + '#')
						.collect(Collectors.joining(", ")))
				.append(");")
				.toString();
	}

	/** {@inheritDoc} */
	@Override
	public void appendListState(final StringBuilder query, final Integer maxRows, final int skipRows, final String sortFieldName, final boolean sortDesc) {
		if (sortFieldName != null) {
			query.append(" order by ").append(StringUtil.camelToConstCase(sortFieldName));
			query.append(sortDesc ? " desc" : "");
		}

		if (maxRows != null) {
			query.append(" limit ").append(maxRows);
		}
		if (skipRows > 0) {
			query.append(" offset ").append(skipRows);
		}
	}

	/** {@inheritDoc} */
	@Override
	public GenerationMode getGenerationMode() {
		return GenerationMode.GENERATED_COLUMNS;
	}
}
