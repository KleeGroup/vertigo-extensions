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
package io.vertigo.datastore.entitystore.data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import io.vertigo.basics.constraint.ConstraintStringLength;
import io.vertigo.basics.formatter.FormatterDefault;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.lang.DataStream;
import io.vertigo.datamodel.smarttype.annotations.Adapter;
import io.vertigo.datamodel.smarttype.annotations.Constraint;
import io.vertigo.datamodel.smarttype.annotations.Formatter;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeDefinition;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeProperty;
import io.vertigo.datastore.entitystore.data.domain.GeoPoint;
import io.vertigo.datastore.entitystore.data.domain.GeoPointAdapter;

public enum TestSmartTypes {

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	String,

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	Long,

	@SmartTypeDefinition(Double.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	Double,

	@SmartTypeDefinition(Integer.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	Integer,

	@SmartTypeDefinition(LocalDate.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "DATE")
	Date,

	@SmartTypeDefinition(Instant.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "TIMESTAMP")
	Instant,

	@SmartTypeDefinition(Boolean.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	Boolean,

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	Id,

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "250", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(250)")
	LibelleLong,

	@SmartTypeDefinition(DataStream.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "BLOB")
	Stream,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(2000)")
	FullText,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(150)")
	@SmartTypeProperty(property = "indexType", value = "code")
	Keyword,

	@SmartTypeDefinition(BigDecimal.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC(8,2)")
	Conso,

	@SmartTypeDefinition(GeoPoint.class)
	@Formatter(clazz = FormatterDefault.class)
	@Adapter(clazz = GeoPointAdapter.class, targetBasicType = BasicType.String)
	@SmartTypeProperty(property = "storeType", value = "TEXT")
	GeoPoint;

}
