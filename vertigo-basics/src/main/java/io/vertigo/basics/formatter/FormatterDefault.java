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
package io.vertigo.basics.formatter;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.node.Node;
import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.structure.definitions.Formatter;
import io.vertigo.datamodel.structure.definitions.FormatterException;

/**
 * Default formatter for boolean, number, date and string.
 * It's possible to override default formatting args by registering specifics parameters with a conventional name.
 * fmtStringDefaultArgs, fmtLocalDateDefaultArgs, fmtInstantDefaultArgs, fmtBooleanDefaultArgs, fmtNumberDefaultArgs
 *
 * @author pchretien, npiedeloup
 */
public final class FormatterDefault implements Formatter {
	private static final String FMT_STRING_DEFAULT_ARGS = "FmtStringDefaultArgs";
	private static final String FMT_LOCAL_DATE_DEFAULT_ARGS = "FmtLocalDateDefaultArgs";
	private static final String FMT_INSTANT_DEFAULT_ARGS = "FmtInstantDefaultArgs";
	private static final String FMT_BOOLEAN_DEFAULT_ARGS = "FmtBooleanDefaultArgs";
	private static final String FMT_NUMBER_DEFAULT_ARGS = "FmtNumberDefaultArgs";

	private final Formatter booleanFormatter;
	private final Formatter numberformatter;
	private final Formatter localDateFormater;
	private final Formatter instantFormater;
	private final Formatter stringFormatter;

	/**
	 * Constructor.
	 */
	public FormatterDefault(final String args) {
		Assertion.check().isTrue(StringUtil.isBlank(args), "Les arguments pour la construction de FormatterDefault sont invalides");
		//-----
		final ParamManager paramManager = Node.getNode().getComponentSpace().resolve(ParamManager.class);
		booleanFormatter = obtainFormatterBoolean(paramManager);
		numberformatter = obtainFormatterNumber(paramManager);
		localDateFormater = obtainFormatterLocalDate(paramManager);
		instantFormater = obtainFormatterInstant(paramManager);
		stringFormatter = obtainFormatterString(paramManager);
	}

	/**
	 *
	 * @param dataType Type
	 * @return Formatter simple utilisé.
	 */
	public Formatter getFormatter(final BasicType dataType) {
		switch (dataType) {
			case String:
				return stringFormatter;
			case LocalDate:
				return localDateFormater;
			case Instant:
				return instantFormater;
			case Boolean:
				return booleanFormatter;
			case Integer:
			case Long:
			case Double:
			case BigDecimal:
				return numberformatter;
			case DataStream:
			default:
				throw new IllegalArgumentException(dataType + " n'est pas géré par ce formatter");
		}
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(final Object objValue, final BasicType dataType) {
		return getFormatter(dataType).valueToString(objValue, dataType);
	}

	/** {@inheritDoc} */
	@Override
	public Object stringToValue(final String strValue, final BasicType dataType) throws FormatterException {
		return getFormatter(dataType).stringToValue(strValue, dataType);
	}

	private static Formatter obtainFormatterBoolean(final ParamManager paramManager) {
		return new FormatterBoolean(
				paramManager.getOptionalParam(FMT_BOOLEAN_DEFAULT_ARGS)
						.orElse(Param.of(FMT_BOOLEAN_DEFAULT_ARGS, "Oui; Non"))
						.getValueAsString());
	}

	private static Formatter obtainFormatterNumber(final ParamManager paramManager) {
		return new FormatterNumber(
				paramManager.getOptionalParam(FMT_NUMBER_DEFAULT_ARGS)
						.orElse(Param.of(FMT_NUMBER_DEFAULT_ARGS, "#,###.##"))
						.getValueAsString());
	}

	private static Formatter obtainFormatterLocalDate(final ParamManager paramManager) {
		return new FormatterDate(
				paramManager.getOptionalParam(FMT_LOCAL_DATE_DEFAULT_ARGS)
						.orElse(Param.of(FMT_LOCAL_DATE_DEFAULT_ARGS, "dd/MM/yyyy"))
						.getValueAsString());
	}

	private static Formatter obtainFormatterInstant(final ParamManager paramManager) {
		return new FormatterDate(
				paramManager.getOptionalParam(FMT_INSTANT_DEFAULT_ARGS)
						.orElse(Param.of(FMT_INSTANT_DEFAULT_ARGS, "dd/MM/yyyy HH:mm"))
						.getValueAsString());
	}

	private static Formatter obtainFormatterString(final ParamManager paramManager) {
		//Fonctionnement de base (pas de formatage)
		return new FormatterString(
				paramManager.getOptionalParam(FMT_STRING_DEFAULT_ARGS)
						.orElse(Param.of(FMT_STRING_DEFAULT_ARGS, "BASIC"))
						.getValueAsString());
	}

}
