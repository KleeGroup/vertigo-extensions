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
package io.vertigo.ui.core.encoders;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import io.vertigo.basics.formatter.Resources;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.node.Node;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.structure.definitions.FormatterException;

/**
 * Encoder de date et de date time
 * @author mlaroche
 */
public final class EncoderDate {

	private static final String pattern = "dd/MM/yyyy HH:mm";
	private static final String localDatePattern = "dd/MM/yyyy";

	/**
	 * Constructor.
	 */
	private EncoderDate() {
	}

	public static String valueToString(final Object objValue, final BasicType dataType) {
		Assertion.check().isTrue(dataType.isAboutDate(), "this formatter only applies on date formats");
		//-----
		if (objValue == null) {
			return ""; //Affichage d'une date non renseignée;
		}
		switch (dataType) {
			case LocalDate:
				return localDateToString((LocalDate) objValue);
			case Instant:
				return instantToString((Instant) objValue);
			default:
				throw new IllegalStateException();
		}
	}

	public static Object stringToValue(final String strValue, final BasicType dataType) throws FormatterException {
		Assertion.check().isTrue(dataType.isAboutDate(), "Formatter ne s'applique qu'aux dates");
		//-----
		if (StringUtil.isBlank(strValue)) {
			return null;
		}
		final String sValue = strValue.trim();
		switch (dataType) {
			case LocalDate:
				return applyStringToObject(sValue, EncoderDate::doStringToLocalDate);
			case Instant:
				return applyStringToObject(sValue, EncoderDate::doStringToInstant);
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 *  Cycles through patterns to try and parse given String into a Date | LocalDate | Instant
	 */
	private static <T> T applyStringToObject(final String dateString, final Function<String, T> fun) throws FormatterException {
		//StringToDate renvoit null si elle n'a pas réussi à convertir la date
		T dateValue ;
		try {
			dateValue = fun.apply(dateString);
		} catch (final Exception e) {
			dateValue = null;
		}
		//A null dateValue means all conversions have failed
		if (dateValue == null) {
			throw new FormatterException(Resources.DYNAMOX_DATE_NOT_FORMATTED);
		}
		return dateValue;
	}

	/**
	 * Converts a String to a LocalDate according to a given pattern
	 */
	private static LocalDate doStringToLocalDate(final String dateString) {
		final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(localDatePattern);
		return LocalDate.parse(dateString, dateTimeFormatter);
	}

	/**
	 * Converts a String to a Instant according to a given pattern
	 */
	private static Instant doStringToInstant(final String dateString) {
		return DateTimeFormatter.ofPattern(pattern)
				.withZone(getLocaleManager().getCurrentZoneId())
				.parse(dateString, Instant::from);
	}

	private static String localDateToString(final LocalDate localDate) {
		return DateTimeFormatter.ofPattern(localDatePattern)
				.format(localDate);
	}

	private static String instantToString(final Instant instant) {
		return DateTimeFormatter.ofPattern(pattern)
				.withZone(getLocaleManager().getCurrentZoneId())
				.format(instant);
	}

	private static LocaleManager getLocaleManager() {
		return Node.getNode().getComponentSpace().resolve(LocaleManager.class);
	}
}
