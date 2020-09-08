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
package io.vertigo.database.impl.sql.vendor.oracle;

import io.vertigo.core.util.StringUtil;

final class Oracle11Dialect extends OracleDialect {

	/** {@inheritDoc} */
	@Override
	public void appendListState(final StringBuilder query, final Integer maxRows, final int skipRows, final String sortFieldName, final boolean sortDesc) {
		//syntax Oracle 11g
		if (sortFieldName != null) {
			query.append(" order by ").append(StringUtil.camelToConstCase(sortFieldName));
			query.append(sortDesc ? " desc" : "");
		}

		if (maxRows != null) {
			if (sortFieldName != null) {
				query.insert(0, "select ordered.* from (")
						.append(") ordered where rownum <= ").append(maxRows);
			} else {
				if (query.indexOf(" where ") != -1 || query.indexOf(" Where ") != -1 || query.indexOf(" WHERE ") != -1) {
					query.append(" and");
				} else {
					query.append(" where");
				}
				query.append(" rownum <= ").append(maxRows);
			}
		}

		if (skipRows > 0) {
			//unsupported
			/*
			 * query.insert(0, "select limited.* from ( select suboffset.*, rownum as rnum from (")
					.append(") suboffset ) limited where rnum > ").append(skipRows);
			 */
		}
	}
}
