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
package io.vertigo.vega.webservice.data;

import java.time.Instant;
import java.time.LocalDate;

import io.vertigo.basics.constraint.ConstraintRegex;
import io.vertigo.basics.constraint.ConstraintStringLength;
import io.vertigo.basics.formatter.FormatterDefault;
import io.vertigo.datamodel.smarttype.annotations.Constraint;
import io.vertigo.datamodel.smarttype.annotations.Formatter;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeDefinition;

public enum VegaTestSmartTypes {

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "50", msg = "")
	Texte50,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintRegex.class, arg = "^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*(\\.[a-zA-Z0-9-]{2,3})+$", msg = "Le courriel n'est pas valide")
	Email,

	@SmartTypeDefinition(Instant.class)
	@Formatter(clazz = FormatterDefault.class)
	Instant,

	@SmartTypeDefinition(LocalDate.class)
	@Formatter(clazz = FormatterDefault.class)
	LocalDate,

	@SmartTypeDefinition(Boolean.class)
	@Formatter(clazz = FormatterDefault.class)
	Boolean,

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterDefault.class)
	Id,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	Code;

}
