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
package io.vertigo.orchestra.definitions;

import java.time.Instant;

import io.vertigo.basics.constraint.ConstraintRegex;
import io.vertigo.basics.constraint.ConstraintStringLength;
import io.vertigo.basics.formatter.FormatterBoolean;
import io.vertigo.basics.formatter.FormatterDate;
import io.vertigo.basics.formatter.FormatterDefault;
import io.vertigo.datamodel.smarttype.annotations.Constraint;
import io.vertigo.datamodel.smarttype.annotations.Formatter;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeDefinition;
import io.vertigo.datamodel.smarttype.annotations.SmartTypeProperty;

public enum OrchestraSmartTypes {

	@SmartTypeDefinition(Long.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	OIdentifiant,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintRegex.class, arg = "([0-9A-Z_-]+)*$", msg = "La liste des identifiants n'est pas valide")
	@Constraint(clazz = ConstraintStringLength.class, arg = "20", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(20)")
	OCodeIdentifiant,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "100", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(100)")
	OLibelle,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "100", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(100)")
	OToken,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@Constraint(clazz = ConstraintStringLength.class, arg = "200", msg = "")
	@SmartTypeProperty(property = "storeType", value = "VARCHAR(200)")
	OClasse,

	@SmartTypeDefinition(Boolean.class)
	@Formatter(clazz = FormatterBoolean.class, arg = "Oui;Non")
	@SmartTypeProperty(property = "storeType", value = "BOOL")
	OBooleen,

	@SmartTypeDefinition(Instant.class)
	@Formatter(clazz = FormatterDate.class, arg = "dd/MM/yyyy HH:mm")
	@SmartTypeProperty(property = "storeType", value = "TIMESTAMP")
	OTimestamp,

	@SmartTypeDefinition(Integer.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "NUMERIC")
	ONombre,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "TEXT")
	OJsonText,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "TEXT")
	OMetadatas,

	@SmartTypeDefinition(String.class)
	@Formatter(clazz = FormatterDefault.class)
	@SmartTypeProperty(property = "storeType", value = "TEXT")
	OText

}
