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
package io.vertigo.ui.impl.springmvc.argumentresolvers;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

import javax.inject.Inject;

import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;

import io.vertigo.core.lang.Assertion;
import io.vertigo.vega.engines.webservice.json.JsonEngine;

/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter}
 * that can read and write JSON using the Vega JsonEngine
 */
public class VegaJsonHttpMessageConverter extends AbstractJsonHttpMessageConverter {

	private final JsonEngine jsonEngine;

	/**
	 * Construct a new converter
	 * @param jsonEngine vertigo-vega json component
	 */
	@Inject
	public VegaJsonHttpMessageConverter(final JsonEngine jsonEngine) {
		Assertion.check().isNotNull(jsonEngine);
		//---
		this.jsonEngine = jsonEngine;
	}

	@Override
	protected Object readInternal(final Type resolvedType, final Reader reader) throws Exception {
		return jsonEngine.fromJson(reader, resolvedType);
	}

	@Override
	protected void writeInternal(final Object o, final Type type, final Writer writer) throws Exception {
		writer.append(jsonEngine.toJson(o));
	}

}
