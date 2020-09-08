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
package io.vertigo.datamodel.smarttype;

import java.util.Map;

import io.vertigo.core.lang.BasicTypeAdapter;
import io.vertigo.core.node.component.Manager;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.ConstraintException;
import io.vertigo.datamodel.structure.definitions.FormatterException;

public interface SmartTypeManager extends Manager {

	void checkValue(SmartTypeDefinition smartTypeDefinition, Object value);

	void checkConstraints(SmartTypeDefinition smartTypeDefinition, Object value) throws ConstraintException;

	String valueToString(SmartTypeDefinition smartTypeDefinition, Object objValue);

	Object stringToValue(SmartTypeDefinition smartTypeDefinition, String strValue) throws FormatterException;

	Map<Class, BasicTypeAdapter> getTypeAdapters(final String adapterType);

}
