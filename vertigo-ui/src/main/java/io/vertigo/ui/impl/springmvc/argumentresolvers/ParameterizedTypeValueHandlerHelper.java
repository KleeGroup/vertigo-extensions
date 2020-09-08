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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;

final class ParameterizedTypeValueHandlerHelper<V> {

	private final Class<V> supportedClazz;
	private final Function<String, V> defaultConverter;
	private final Map<Class<?>, Function<String, ?>> supportedParameterizedType = new HashMap<>();

	ParameterizedTypeValueHandlerHelper(final Class<V> supportedClazz, final Function<String, V> converter) {
		this.supportedClazz = supportedClazz;
		defaultConverter = converter;
	}

	public void addSupportedParameterizedType(final Class<?> parameterizedType) {
		if (Optional.class.equals(parameterizedType)) {
			addSupportedParameterizedType(parameterizedType, this::toOptional);
		} else if (List.class.equals(parameterizedType)) {
			addSupportedParameterizedType(parameterizedType, this::toList);
		} else {
			throw new IllegalArgumentException("Only Optional and List are supported by default. Use addSupportedParameterizedType(final Class<P> parameterizedType, final Function<String, P> converter)");
		}
	}

	public <P> void addSupportedParameterizedType(final Class<P> parameterizedType, final Function<String, ?> converter) { //should be P<V>
		supportedParameterizedType.put(parameterizedType, converter);
	}

	public boolean supportsType(final MethodParameter parameter) {
		return supportedClazz.isAssignableFrom(getParameterType(parameter));
	}

	public <O> O convert(final String requestValue, final MethodParameter parameter) {
		final Class<O> parameterTypeClazz = (Class<O>) parameter.getParameterType();
		final Function<String, ?> converter = supportedParameterizedType.get(parameterTypeClazz);
		if (converter != null) {
			return (O) converter.apply(requestValue);
		}
		return (O) defaultConverter.apply(requestValue);
	}

	private Class<?> getParameterType(final MethodParameter parameterType) {
		final Class<?> returnTypeClazz = parameterType.getParameterType();
		for (final Class<?> supportedTypeClazz : supportedParameterizedType.keySet()) {
			if (supportedTypeClazz.isAssignableFrom(returnTypeClazz)) {
				return castAsClass(((ParameterizedType) parameterType.getGenericParameterType()).getActualTypeArguments()[0]); //we known that these GenericParameterType has one parameterized type
			}
		}
		return returnTypeClazz;
	}

	private Optional<V> toOptional(final String strValue) {
		if (!StringUtils.isEmpty(strValue)) {
			return Optional.of(defaultConverter.apply(strValue));
		}
		return Optional.empty();
	}

	private List<V> toList(final String strValue) {
		if (!StringUtils.isEmpty(strValue)) {
			final String[] strValues = strValue.split(",");
			final List<V> result = new ArrayList<>(strValues.length);
			for (final String oneStrValue : strValues) {
				result.add(defaultConverter.apply(oneStrValue.trim()));
			}
			return result;
		}
		return Collections.emptyList();
	}

	private static Class<?> castAsClass(final Type testedType) {
		if (testedType instanceof Class) {
			return (Class<?>) testedType;
		} else if (testedType instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) testedType).getRawType();
		}
		throw new IllegalArgumentException("Parameters/Return Type must be Class or ParameterizedType, unsupported type:" + testedType);
	}

}
