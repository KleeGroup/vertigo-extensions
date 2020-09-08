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
package io.vertigo.datafactory.search.data;

import java.math.BigDecimal;
import java.time.Instant;

import io.vertigo.basics.formatter.FormatterDefault;
import io.vertigo.core.lang.BasicType;
import io.vertigo.datafactory.search.data.domain.GeoPoint;
import io.vertigo.datafactory.search.data.domain.GeoPointAdapter;
import io.vertigo.datamodel.smarttype.annotations.Adapter;
import io.vertigo.datamodel.smarttype.annotations.Formatter;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeDefinition;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeProperty;

public enum TestSearchSmartTypes {

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterDefault.class)
	Identifiant,

	@SmartTypeDefinition(Integer.class)
	@Formatter(clazz = FormatterDefault.class)
	Integer,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "indexType", value = "text_fr:sortable")
	String,

	@SmartTypeDefinition(Instant.class)
	@Formatter(clazz = FormatterDefault.class)
	DateTime,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "indexType", value = "text_fr:facetable")
	FullText,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "indexType", value = "code:keyword")
	Keyword,

	@SmartTypeDefinition(BigDecimal.class)
	@Formatter(clazz = FormatterDefault.class)
	Conso,

	@SmartTypeDefinition(GeoPoint.class)
	@Adapter(targetBasicType = BasicType.String, clazz = GeoPointAdapter.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "indexType", value = ":geo_point")
	GeoPoint;

}
