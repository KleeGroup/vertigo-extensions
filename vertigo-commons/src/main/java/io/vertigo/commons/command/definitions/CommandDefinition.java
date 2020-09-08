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
package io.vertigo.commons.command.definitions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.commons.command.CommandResponse;
import io.vertigo.commons.command.GenericUID;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.core.util.StringUtil;

@DefinitionPrefix(CommandDefinition.PREFIX)
public final class CommandDefinition extends AbstractDefinition {
	public static final String PREFIX = "Cmd";
	private static final Pattern COMMAND_PATTERN = Pattern.compile("(\\/[a-zA-Z0-9]+)+");

	private final String command;
	private final String description;
	private final List<String> questions;
	private final Function<Object[], CommandResponse> action;
	private final List<CommandParam> commandParams;

	public CommandDefinition(
			final String command,
			final String description,
			final List<String> questions,
			final List<CommandParam> commandParams,
			final Function<Object[], CommandResponse> action) {
		super(buildName(command));
		//---
		Assertion.check()
				.isNotBlank(description)
				.isNotNull(questions)
				.isTrue(COMMAND_PATTERN.matcher(command).matches(), "handle '{0}' must respect the pattern '{1}'", command, COMMAND_PATTERN)
				.isNotNull(action);
		//---
		commandParams
				.forEach(commandParam -> {
					final Type type = commandParam.getType();
					if (type instanceof Class) {
						Assertion.check().isTrue(String.class.isAssignableFrom((Class) type), "Only ParamUID and String params are allowed for command");
					} else if (type instanceof ParameterizedType) {
						Assertion.check().isTrue(GenericUID.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()), "Only ParamUID and String params are allowed for command");
					}
				});

		//---
		this.command = command;
		this.description = description;
		this.questions = questions;
		this.commandParams = commandParams;
		this.action = action;
	}

	private static String buildName(final String command) {
		Assertion.check().isNotBlank(command);
		//---
		final String[] commandParts = command.substring(1).split("\\/"); // we remove the first slash
		return "Cmd"
				+ StringUtil.first2UpperCase(commandParts[0])
				+ Stream.of(commandParts).skip(1).collect(Collectors.joining("$"));

	}

	public String getDescription() {
		return description;
	}

	public String getCommand() {
		return command;
	}

	public List<String> getQuestions() {
		return questions;
	}

	public List<CommandParam> getParams() {
		return commandParams;
	}

	public Function<Object[], CommandResponse> getAction() {
		return action;
	}
}
