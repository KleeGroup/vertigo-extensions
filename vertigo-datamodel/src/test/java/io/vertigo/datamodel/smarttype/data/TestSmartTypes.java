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
package io.vertigo.datamodel.smarttype.data;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.vertigo.core.lang.BasicType;
import io.vertigo.datamodel.smarttype.annotations.Adapter;
import io.vertigo.datamodel.smarttype.annotations.Constraint;
import io.vertigo.datamodel.smarttype.annotations.Formatter;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeDefinition;
import io.vertigo.datamodel.smarttype.data.domain.GeoPoint;
import io.vertigo.datamodel.smarttype.data.domain.GeoPointMapper;

public enum TestSmartTypes {

	@SmartTypeDefinition(Long.class)
	Id,

	@SmartTypeDefinition(String.class)
	@Constraint(clazz = ConstraintTest.class, arg = "", msg = "")
	String,

	@SmartTypeDefinition(String.class)
	MultipleIds,

	@SmartTypeDefinition(LocalDate.class)
	Localdate,

	@SmartTypeDefinition(BigDecimal.class)
	Currency,

	@SmartTypeDefinition(Integer.class)
	Health,

	@SmartTypeDefinition(String.class)
	Label,

	@SmartTypeDefinition(String.class)
	Description,

	@SmartTypeDefinition(String.class)
	Email,

	@SmartTypeDefinition(String.class)
	Url,

	@SmartTypeDefinition(String.class)
	Code,

	@SmartTypeDefinition(String.class)
	Label2,

	@SmartTypeDefinition(String.class)
	Label3,

	@SmartTypeDefinition(Long.class)
	Size,

	@SmartTypeDefinition(String.class)
	Siret,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterTest.class)
	@Constraint(clazz = ConstraintTest.class, arg = "", msg = "")
	Siret2,

	@SmartTypeDefinition(GeoPoint.class)
	@Adapter(targetBasicType = BasicType.String, clazz = GeoPointMapper.class)
	GeoPoint
}
