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
package io.vertigo.orchestra.plugins.services;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import io.vertigo.commons.codec.Codec;

/**
 * Encodes a map to a simple json (as a string).
 * Decodes a json (as a string) into a simple map.
 * @author pchretien
 */
public final class MapCodec implements Codec<Map<String, String>, String> {
	private static final Gson GSON = new GsonBuilder().create();

	@Override
	public Map<String, String> decode(final String toEncode) {
		if (toEncode == null) {
			return Collections.emptyMap();
		}
		return new JsonParser()
				.parse(toEncode)
				.getAsJsonObject()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> entry.getValue().getAsString()));
	}

	@Override
	public String encode(final Map<String, String> encoded) {
		return GSON.toJson(encoded);
	}
}
