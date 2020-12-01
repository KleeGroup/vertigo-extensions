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
package io.vertigo.ui.data;

import java.time.Instant;
import java.time.LocalDate;

import io.vertigo.basics.constraint.ConstraintNumberMaximum;
import io.vertigo.basics.constraint.ConstraintNumberMinimum;
import io.vertigo.basics.constraint.ConstraintRegex;
import io.vertigo.basics.constraint.ConstraintStringLength;
import io.vertigo.basics.formatter.FormatterBoolean;
import io.vertigo.basics.formatter.FormatterDate;
import io.vertigo.basics.formatter.FormatterDefault;
import io.vertigo.basics.formatter.FormatterNumber;
import io.vertigo.basics.formatter.FormatterString;
import io.vertigo.datamodel.smarttype.annotations.Constraint;
import io.vertigo.datamodel.smarttype.annotations.Formatter;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeDefinition;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeProperty;

public enum VuiTestSmartTypes {

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(500)")
	@SmartTypeProperty(property = "indexType", value = "multiple_code")
	MultiValues,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(250)")
	@SmartTypeProperty(property = "indexType", value = "text_fr_not_tokenized")
	TextNotTokenized,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(10000)")
	@SmartTypeProperty(property = "indexType", value = "text_fr")
	Text,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterString.class, arg = "UPPER")
	@Constraint(clazz = ConstraintStringLength.class, arg = "30", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(30)")
	@SmartTypeProperty(property = "indexType", value = "code")
	Code,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintRegex.class, arg = "^[0-9]{5}$", msg = "Code postal erroné")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(5)")
	CodePostal,

	@SmartTypeDefinition(LocalDate.class)
	@Formatter(clazz = FormatterDate.class, arg = "dd/MM/yyyy")
	@SmartTypeProperty(property = "storeType", value = "DATE")
	Date,

	@SmartTypeDefinition(Instant.class)
	@Formatter(clazz = FormatterDate.class, arg = "dd/MM/yyyy HH:mm;dd/MM/yy HH:mm")
	@SmartTypeProperty(property = "storeType", value = "TIMESTAMP")
	LastModified,

	@SmartTypeDefinition(Integer.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintNumberMinimum.class, arg = "1500", msg = "")
	@Constraint(clazz = ConstraintNumberMaximum.class, arg = "2500", msg = "")
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	Year,

	@SmartTypeDefinition(Integer.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	Duration,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintRegex.class, arg = "^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*\\.[a-zA-Z0-9-]{2,3}$", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(255)")
	Email,

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterNumber.class, arg = "###0")
	@SmartTypeProperty(property = "storeType", value = "BIGINT")
	Id,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "30", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(100)")
	@SmartTypeProperty(property = "indexType", value = "text_fr:facetable")
	Label,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "50", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(50)")
	@SmartTypeProperty(property = "indexType", value = "text_fr:facetable")
	LabelShort,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "250", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(250)")
	LabelLong,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "4000", msg = "")
	@SmartTypeProperty(property = "storeType", value = "TEXT")
	Comment,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "50", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(50)")
	Name,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "50", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(50)")
	Firstname,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(250)")
	Password,

	@SmartTypeDefinition(Integer.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "INT")
	Rating,

	@SmartTypeDefinition(Boolean.class)
	@Formatter(clazz = FormatterBoolean.class, arg = "Oui;Non")
	@SmartTypeProperty(property = "storeType", value = "BOOLEAN")
	OuiNon,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(250)")
	MultipleIds;
}
