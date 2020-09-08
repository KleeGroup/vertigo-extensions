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
package io.vertigo.basics.data;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.vertigo.basics.constraint.ConstraintNumberMaximum;
import io.vertigo.basics.constraint.ConstraintNumberMinimum;
import io.vertigo.basics.constraint.ConstraintRegex;
import io.vertigo.basics.constraint.ConstraintStringLength;
import io.vertigo.basics.formatter.FormatterDate;
import io.vertigo.basics.formatter.FormatterDefault;
import io.vertigo.basics.formatter.FormatterId;
import io.vertigo.basics.formatter.FormatterString;
import io.vertigo.datamodel.smarttype.annotations.Constraint;
import io.vertigo.datamodel.smarttype.annotations.Formatter;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeDefinition;

public enum TestSmartTypes {

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterId.class)
	Id,

	@SmartTypeDefinition(String.class)
	String,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	MultipleIds,

	@SmartTypeDefinition(LocalDate.class)
	@Formatter(clazz = FormatterDate.class, arg = "dd/MM/yyyy")
	Localdate,

	@SmartTypeDefinition(BigDecimal.class)
	@Formatter(clazz = FormatterDefault.class)
	Currency,

	@SmartTypeDefinition(Integer.class)
	@Constraint(clazz = ConstraintNumberMinimum.class, arg = "0", msg = "")
	@Constraint(clazz = ConstraintNumberMaximum.class, arg = "100", msg = "")
	Health,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "100", msg = "")
	Label,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "350", msg = "")
	Description,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintRegex.class, arg = "^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*(\\.[a-zA-Z0-9-]{2,3})+$", msg = "L'email n'est pas valide")
	Email,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	Url,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "100", msg = "")
	Code,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	Label2,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "14", msg = "")
	Label3,

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterDefault.class)
	Size,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "14", msg = "")
	Siret,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterString.class, arg = "UPPER")
	@Constraint(clazz = ConstraintStringLength.class, arg = "14", msg = "")
	Siret2,

}
