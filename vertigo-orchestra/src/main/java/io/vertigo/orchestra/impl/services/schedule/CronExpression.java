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
package io.vertigo.orchestra.impl.services.schedule;

import java.text.ParseException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
/**
 *
 * This File is a modified and simplified version of the quartz CronExpression.
 * Thanks to the original authors, feel free to reuse or contriubute to this version as it's redistributed in Apache 2.0 Licence
 *
 *
 * Provides a parser and evaluator for unix-like cron expressions. Cron
 * expressions provide the ability to specify complex time combinations such as
 * &quot;At 8:00am every Monday through Friday&quot; or &quot;At 1:30am every
 * last Friday of the month&quot;.
 * <P>
 * Cron expressions are comprised of 6 required fields and one optional field
 * separated by white space. The fields respectively are described as follows:
 *
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L W</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-11 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L #</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>empty, 1970-2199</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
 * <P>
 * The '*' character is used to specify all values. For example, &quot;*&quot;
 * in the minute field means &quot;every minute&quot;.
 * <P>
 * The '?' character is allowed for the day-of-month and day-of-week fields. It
 * is used to specify 'no specific value'. This is useful when you need to
 * specify something in one of the two fields, but not the other.
 * <P>
 * The '-' character is used to specify ranges For example &quot;10-12&quot; in
 * the hour field means &quot;the hours 10, 11 and 12&quot;.
 * <P>
 * The ',' character is used to specify additional values. For example
 * &quot;MON,WED,FRI&quot; in the day-of-week field means &quot;the days Monday,
 * Wednesday, and Friday&quot;.
 * <P>
 * The '/' character is used to specify increments. For example &quot;0/15&quot;
 * in the seconds field means &quot;the seconds 0, 15, 30, and 45&quot;. And
 * &quot;5/15&quot; in the seconds field means &quot;the seconds 5, 20, 35, and
 * 50&quot;.  Specifying '*' before the  '/' is equivalent to specifying 0 is
 * the value to start with. Essentially, for each field in the expression, there
 * is a set of numbers that can be turned on or off. For seconds and minutes,
 * the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to
 * 31, and for months 0 to 11 (JAN to DEC). The &quot;/&quot; character simply helps you turn
 * on every &quot;nth&quot; value in the given set. Thus &quot;7/6&quot; in the
 * month field only turns on month &quot;7&quot;, it does NOT mean every 6th
 * month, please note that subtlety.
 * <P>
 * The 'L' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for &quot;last&quot;, but it has different
 * meaning in each of the two fields. For example, the value &quot;L&quot; in
 * the day-of-month field means &quot;the last day of the month&quot; - day 31
 * for January, day 28 for February on non-leap years. If used in the
 * day-of-week field by itself, it simply means &quot;7&quot; or
 * &quot;SAT&quot;. But if used in the day-of-week field after another value, it
 * means &quot;the last xxx day of the month&quot; - for example &quot;6L&quot;
 * means &quot;the last friday of the month&quot;. You can also specify an offset
 * from the last day of the month, such as "L-3" which would mean the third-to-last
 * day of the calendar month. <i>When using the 'L' option, it is important not to
 * specify lists, or ranges of values, as you'll get confusing/unexpected results.</i>
 * <P>
 * The 'W' character is allowed for the day-of-month field.  This character
 * is used to specify the weekday (Monday-Friday) nearest the given day.  As an
 * example, if you were to specify &quot;15W&quot; as the value for the
 * day-of-month field, the meaning is: &quot;the nearest weekday to the 15th of
 * the month&quot;. So if the 15th is a Saturday, the trigger will fire on
 * Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the
 * 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th.
 * However if you specify &quot;1W&quot; as the value for day-of-month, and the
 * 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not
 * 'jump' over the boundary of a month's days.  The 'W' character can only be
 * specified when the day-of-month is a single day, not a range or list of days.
 * <P>
 * The 'L' and 'W' characters can also be combined for the day-of-month
 * expression to yield 'LW', which translates to &quot;last weekday of the
 * month&quot;.
 * <P>
 * The '#' character is allowed for the day-of-week field. This character is
 * used to specify &quot;the nth&quot; XXX day of the month. For example, the
 * value of &quot;6#3&quot; in the day-of-week field means the third Friday of
 * the month (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month).
 * Other examples: &quot;2#1&quot; = the first Monday of the month and
 * &quot;4#5&quot; = the fifth Wednesday of the month. Note that if you specify
 * &quot;#5&quot; and there is not 5 of the given day-of-week in the month, then
 * no firing will occur that month.  If the '#' character is used, there can
 * only be one expression in the day-of-week field (&quot;3#1,6#3&quot; is
 * not valid, since there are two expressions).
 * <P>
 * <!--The 'C' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for "calendar". This means values are
 * calculated against the associated calendar, if any. If no calendar is
 * associated, then it is equivalent to having an all-inclusive calendar. A
 * value of "5C" in the day-of-month field means "the first day included by the
 * calendar on or after the 5th". A value of "1C" in the day-of-week field
 * means "the first day included by the calendar on or after Sunday".-->
 * <P>
 * The legal characters and the names of months and days of the week are not
 * case sensitive.
 *
 * <p>
 * <b>NOTES:</b>
 * <ul>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in one of these fields).
 * </li>
 * <li>Overflowing ranges is supported - that is, having a larger number on
 * the left hand side than the right. You might do 22-2 to catch 10 o'clock
 * at night until 2 o'clock in the morning, or you might have NOV-FEB. It is
 * very important to note that overuse of overflowing ranges creates ranges
 * that don't make sense and no effort has been made to determine which
 * interpretation CronExpression chooses. An example would be
 * "0 0 14-6 ? * FRI-MON". </li>
 * </ul>
 * </p>
 *
 *
 * @author Sharada Jambula, James House
 * @author Contributions from Mads Henderson
 * @author Refactoring from CronTrigger to CronExpression by Aaron Craven
 */
public final class CronExpression {
	private static final int SECOND = 0;
	private static final int MINUTE = 1;
	private static final int HOUR = 2;
	private static final int DAY_OF_MONTH = 3;
	private static final int MONTH = 4;
	private static final int DAY_OF_WEEK = 5;
	private static final int YEAR = 6;
	private static final int ALL_SPEC_INT = 99; // '*'
	private static final int NO_SPEC_INT = 98; // '?'
	private static final Integer ALL_SPEC = ALL_SPEC_INT;
	private static final Integer NO_SPEC = NO_SPEC_INT;

	enum MONTHS {
		JAN(0, 31),
		FEB(1, 28),
		MAR(2, 31),
		APR(3, 30),
		MAY(4, 31),
		JUN(5, 30),
		JUL(6, 31),
		AUG(7, 31),
		SEP(8, 30),
		OCT(9, 31),
		NOV(10, 30),
		DEC(11, 31);

		private final int index;
		private final int maxDay;

		/**
		 * Constructeur.
		 * @param index
		 * @param maxDay
		 */
		private MONTHS(final int index, final int maxDay) {
			this.index = index;
			this.maxDay = maxDay;
		}

		private int getIndex() {
			return index;
		}

		public static int getMonthNumber(final String name) {
			try {
				return MONTHS.valueOf(name).index;
			} catch (final IllegalArgumentException e) {
				return -1;
			}
		}

		public static MONTHS getMonthByIndex(final int index) {
			for (final MONTHS month : MONTHS.values()) {
				if (month.getIndex() == index) {
					return month;
				}
			}
			throw new RuntimeException("No month found with index : " + index);
		}

		public static MONTHS getMonthByNumber(final int number) {
			return getMonthByIndex(number - 1);
		}

		private static boolean isLeapYear(final int year) {
			return ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0));
		}

		public int getLastDayOfMonth(final int year) {
			if (this == FEB && isLeapYear(year)) {
				return 29;
			}
			return maxDay;
		}
	}

	enum DAYS {
		// @formatter:off
		SUN(1),
		MON(2),
		TUE(3),
		WED(4),
		THU(5),
		FRI(6),
		SAT(7);
		// @formatter:on

		private final int index;

		/**
		 * Constructeur.
		 * @param index
		 */
		private DAYS(final int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public static int getDayOfWeekNumber(final String name) {
			try {
				return DAYS.valueOf(name).index;
			} catch (final IllegalArgumentException e) {
				return -1;
			}
		}

		public static DAYS getDayByIndex(final int index) {
			for (final DAYS day : DAYS.values()) {
				if (day.getIndex() == index) {
					return day;
				}
			}
			throw new RuntimeException("No day with index : " + index);
		}

	}

	private final String cronExpression;
	private TimeZone timeZone = null;
	private final TreeSet<Integer> seconds = new TreeSet<>();
	private final TreeSet<Integer> minutes = new TreeSet<>();
	private final TreeSet<Integer> hours = new TreeSet<>();
	private final TreeSet<Integer> daysOfMonth = new TreeSet<>();
	private final TreeSet<Integer> months = new TreeSet<>();
	private final TreeSet<Integer> daysOfWeek = new TreeSet<>();
	private final TreeSet<Integer> years = new TreeSet<>();

	private boolean lastdayOfWeek = false;
	private int nthdayOfWeek = 0;
	private boolean lastdayOfMonth = false;
	private boolean nearestWeekday = false;
	private int lastdayOffset = 0;

	private static final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 100;

	/**
	 * Constructs a new <CODE>CronExpression</CODE> based on the specified
	 * parameter.
	 *
	 * @param cronExpression String representation of the cron expression the
	 *                       new object should represent
	 * @throws java.text.ParseException
	 *         if the string expression cannot be parsed into a valid
	 *         <CODE>CronExpression</CODE>
	 */
	public CronExpression(final String cronExpression) throws ParseException {
		Assertion.check().isNotNull(cronExpression, "cronExpression cannot be null");
		//--

		this.cronExpression = cronExpression.toUpperCase(Locale.FRENCH);
		buildExpression(this.cronExpression);
	}

	/**
	 * Returns the time zone for which this <code>CronExpression</code>
	 * will be resolved.
	 */
	public TimeZone getTimeZone() {
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}

		return timeZone;
	}

	/**
	 * Sets the time zone for which  this <code>CronExpression</code>
	 * will be resolved.
	 */
	public void setTimeZone(final TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * Returns the string representation of the <CODE>CronExpression</CODE>
	 *
	 * @return a string representation of the <CODE>CronExpression</CODE>
	 */
	@Override
	public String toString() {
		return cronExpression;
	}

	/**
	* Indicates whether the given date satisfies the cron expression. Note that
	* milliseconds are ignored, so two Dates falling on different milliseconds
	* of the same second will always have the same result here.
	*
	* @param date the date to evaluate
	* @return a boolean indicating whether the given date satisfies the cron
	*         expression
	*/
	public boolean isSatisfiedBy(final Date date) {
		final Calendar testDateCal = Calendar.getInstance(getTimeZone());
		testDateCal.setTime(date);
		testDateCal.set(Calendar.MILLISECOND, 0);
		final Instant originalDate = testDateCal.toInstant();

		testDateCal.add(Calendar.SECOND, -1);

		final Instant timeAfter = getNextValidTimeAfter(testDateCal.toInstant());

		return ((timeAfter != null) && (timeAfter.equals(originalDate)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expression Parsing Functions
	//
	////////////////////////////////////////////////////////////////////////////

	private void buildExpression(final String expression) throws ParseException {
		try {
			int exprOn = SECOND;

			final StringTokenizer exprsTok = new StringTokenizer(expression, " \t", false);

			while (exprsTok.hasMoreTokens() && exprOn <= YEAR) {
				final String expr = exprsTok.nextToken().trim();

				// throw an exception if L is used with other days of the month
				if (exprOn == DAY_OF_MONTH && expr.indexOf('L') != -1 && expr.length() > 1 && expr.contains(",")) {
					throw new ParseException("Support for specifying 'L' and 'LW' with other days of the month is not implemented", -1);
				}
				// throw an exception if L is used with other days of the week
				if (exprOn == DAY_OF_WEEK && expr.indexOf('L') != -1 && expr.length() > 1 && expr.contains(",")) {
					throw new ParseException("Support for specifying 'L' with other days of the week is not implemented", -1);
				}
				if (exprOn == DAY_OF_WEEK && expr.indexOf('#') != -1 && expr.indexOf('#', expr.indexOf('#') + 1) != -1) {
					throw new ParseException("Support for specifying multiple \"nth\" days is not implemented.", -1);
				}

				final StringTokenizer vTok = new StringTokenizer(expr, ",");
				while (vTok.hasMoreTokens()) {
					final String v = vTok.nextToken();
					storeExpressionVals(0, v, exprOn);
				}

				exprOn++;
			}

			if (exprOn <= DAY_OF_WEEK) {
				throw new ParseException("Unexpected end of expression.", expression.length());
			}

			if (exprOn <= YEAR) {
				storeExpressionVals(0, "*", YEAR);
			}

			final TreeSet<Integer> dow = getSet(DAY_OF_WEEK);
			final TreeSet<Integer> dom = getSet(DAY_OF_MONTH);

			// Copying the logic from the UnsupportedOperationException below
			final boolean dayOfMSpec = !dom.contains(NO_SPEC);
			final boolean dayOfWSpec = !dow.contains(NO_SPEC);

			if (!dayOfMSpec || dayOfWSpec) {
				if (!dayOfWSpec || dayOfMSpec) {
					throw new ParseException(
							"Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.", 0);
				}
			}
		} catch (final ParseException pe) {
			throw pe;
		} catch (final Exception e) {
			throw new ParseException("Illegal cron expression format ("
					+ e.toString() + ")", 0);
		}
	}

	private int storeExpressionVals(final int pos, final String s, final int type) throws ParseException {
		int incr = 0;
		int i = skipWhiteSpace(pos, s);
		if (i >= s.length()) {
			return i;
		}
		char c = s.charAt(i);
		if ((c >= 'A') && (c <= 'Z') && (!s.equals("L")) && (!s.equals("LW")) && (!s.matches("^L-[0-9]*[W]?"))) {
			String sub = s.substring(i, i + 3);
			int sval = -1;
			int eval = -1;
			if (type == MONTH) {
				sval = MONTHS.getMonthNumber(sub) + 1;
				if (sval <= 0) {
					throw new ParseException("Invalid Month value: '" + sub + "'", i);
				}
				if (s.length() > i + 3) {
					c = s.charAt(i + 3);
					if (c == '-') {
						i += 4;
						sub = s.substring(i, i + 3);
						eval = MONTHS.getMonthNumber(sub) + 1;
						if (eval <= 0) {
							throw new ParseException("Invalid Month value: '" + sub + "'", i);
						}
					}
				}
			} else if (type == DAY_OF_WEEK) {
				sval = DAYS.getDayOfWeekNumber(sub);
				if (sval < 0) {
					throw new ParseException("Invalid Day-of-Week value: '"
							+ sub + "'", i);
				}
				if (s.length() > i + 3) {
					c = s.charAt(i + 3);
					if (c == '-') {
						i += 4;
						sub = s.substring(i, i + 3);
						eval = DAYS.getDayOfWeekNumber(sub);
						if (eval < 0) {
							throw new ParseException(
									"Invalid Day-of-Week value: '" + sub
											+ "'",
									i);
						}
					} else if (c == '#') {
						try {
							i += 4;
							nthdayOfWeek = Integer.parseInt(s.substring(i));
							if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
								throw new Exception();
							}
						} catch (final Exception e) {
							throw new ParseException(
									"A numeric value between 1 and 5 must follow the '#' option",
									i);
						}
					} else if (c == 'L') {
						lastdayOfWeek = true;
						i++;
					}
				}

			} else {
				throw new ParseException(
						"Illegal characters for this position: '" + sub + "'",
						i);
			}
			if (eval != -1) {
				incr = 1;
			}
			addToSet(sval, eval, incr, type);
			return (i + 3);
		}

		if (c == '?') {
			i++;
			if ((i + 1) < s.length()
					&& (s.charAt(i) != ' ' && s.charAt(i + 1) != '\t')) {
				throw new ParseException("Illegal character after '?': "
						+ s.charAt(i), i);
			}
			if (type != DAY_OF_WEEK && type != DAY_OF_MONTH) {
				throw new ParseException(
						"'?' can only be specfied for Day-of-Month or Day-of-Week.",
						i);
			}
			if (type == DAY_OF_WEEK && !lastdayOfMonth) {
				final int val = daysOfMonth.last();
				if (val == NO_SPEC_INT) {
					throw new ParseException(
							"'?' can only be specfied for Day-of-Month -OR- Day-of-Week.",
							i);
				}
			}

			addToSet(NO_SPEC_INT, -1, 0, type);
			return i;
		}

		if (c == '*' || c == '/') {
			if (c == '*' && (i + 1) >= s.length()) {
				addToSet(ALL_SPEC_INT, -1, incr, type);
				return i + 1;
			} else if (c == '/'
					&& ((i + 1) >= s.length() || s.charAt(i + 1) == ' ' || s
							.charAt(i + 1) == '\t')) {
				throw new ParseException("'/' must be followed by an integer.", i);
			} else if (c == '*') {
				i++;
			}
			c = s.charAt(i);
			if (c == '/') { // is an increment specified?
				i++;
				if (i >= s.length()) {
					throw new ParseException("Unexpected end of string.", i);
				}

				incr = getNumericValue(s, i);

				i++;
				if (incr > 10) {
					i++;
				}
				if (incr > 59 && (type == SECOND || type == MINUTE)) {
					throw new ParseException("Increment > 60 : " + incr, i);
				} else if (incr > 23 && (type == HOUR)) {
					throw new ParseException("Increment > 24 : " + incr, i);
				} else if (incr > 31 && (type == DAY_OF_MONTH)) {
					throw new ParseException("Increment > 31 : " + incr, i);
				} else if (incr > 7 && (type == DAY_OF_WEEK)) {
					throw new ParseException("Increment > 7 : " + incr, i);
				} else if (incr > 12 && (type == MONTH)) {
					throw new ParseException("Increment > 12 : " + incr, i);
				}
			} else {
				incr = 1;
			}

			addToSet(ALL_SPEC_INT, -1, incr, type);
			return i;
		} else if (c == 'L') {
			i++;
			if (type == DAY_OF_MONTH) {
				lastdayOfMonth = true;
			}
			if (type == DAY_OF_WEEK) {
				addToSet(7, 7, 0, type);
			}
			if (type == DAY_OF_MONTH && s.length() > i) {
				c = s.charAt(i);
				if (c == '-') {
					final ValueSet vs = getValue(0, s, i + 1);
					lastdayOffset = vs.value;
					if (lastdayOffset > 30) {
						throw new ParseException("Offset from last day must be <= 30", i + 1);
					}
					i = vs.pos;
				}
				if (s.length() > i) {
					c = s.charAt(i);
					if (c == 'W') {
						nearestWeekday = true;
						i++;
					}
				}
			}
			return i;
		} else if (c >= '0' && c <= '9') {
			int val = Integer.parseInt(String.valueOf(c));
			i++;
			if (i >= s.length()) {
				addToSet(val, -1, -1, type);
			} else {
				c = s.charAt(i);
				if (c >= '0' && c <= '9') {
					final ValueSet vs = getValue(val, s, i);
					val = vs.value;
					i = vs.pos;
				}
				i = checkNext(i, s, val, type);
				return i;
			}
		} else {
			throw new ParseException("Unexpected character: " + c, i);
		}

		return i;
	}

	private int checkNext(final int pos, final String s, final int val, final int type) throws ParseException {
		int end = -1;
		int i = pos;

		if (i >= s.length()) {
			addToSet(val, end, -1, type);
			return i;
		}

		char c = s.charAt(pos);

		if (c == 'L') {
			if (type == DAY_OF_WEEK) {
				if (val < 1 || val > 7) {
					throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
				}
				lastdayOfWeek = true;
			} else {
				throw new ParseException("'L' option is not valid here. (pos=" + i + ")", i);
			}
			final TreeSet<Integer> set = getSet(type);
			set.add(val);
			i++;
			return i;
		}

		if (c == 'W') {
			if (type == DAY_OF_MONTH) {
				nearestWeekday = true;
			} else {
				throw new ParseException("'W' option is not valid here. (pos=" + i + ")", i);
			}
			if (val > 31) {
				throw new ParseException("The 'W' option does not make sense with values larger than 31 (max number of days in a month)", i);
			}
			final TreeSet<Integer> set = getSet(type);
			set.add(val);
			i++;
			return i;
		}

		if (c == '#') {
			if (type != DAY_OF_WEEK) {
				throw new ParseException("'#' option is not valid here. (pos=" + i + ")", i);
			}
			i++;
			try {
				nthdayOfWeek = Integer.parseInt(s.substring(i));
				if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
					throw new Exception();
				}
			} catch (final Exception e) {
				throw new ParseException(
						"A numeric value between 1 and 5 must follow the '#' option",
						i);
			}

			final TreeSet<Integer> set = getSet(type);
			set.add(val);
			i++;
			return i;
		}

		if (c == '-') {
			i++;
			c = s.charAt(i);
			final int v = Integer.parseInt(String.valueOf(c));
			end = v;
			i++;
			if (i >= s.length()) {
				addToSet(val, end, 1, type);
				return i;
			}
			c = s.charAt(i);
			if (c >= '0' && c <= '9') {
				final ValueSet vs = getValue(v, s, i);
				end = vs.value;
				i = vs.pos;
			}
			if (i < s.length() && ((c = s.charAt(i)) == '/')) {
				i++;
				c = s.charAt(i);
				final int v2 = Integer.parseInt(String.valueOf(c));
				i++;
				if (i >= s.length()) {
					addToSet(val, end, v2, type);
					return i;
				}
				c = s.charAt(i);
				if (c >= '0' && c <= '9') {
					final ValueSet vs = getValue(v2, s, i);
					final int v3 = vs.value;
					addToSet(val, end, v3, type);
					i = vs.pos;
					return i;
				}
				addToSet(val, end, v2, type);
				return i;

			}
			addToSet(val, end, 1, type);
			return i;
		}

		if (c == '/') {
			i++;
			c = s.charAt(i);
			final int v2 = Integer.parseInt(String.valueOf(c));
			i++;
			if (i >= s.length()) {
				addToSet(val, end, v2, type);
				return i;
			}
			c = s.charAt(i);
			if (c >= '0' && c <= '9') {
				final ValueSet vs = getValue(v2, s, i);
				final int v3 = vs.value;
				addToSet(val, end, v3, type);
				i = vs.pos;
				return i;
			}
			throw new ParseException("Unexpected character '" + c + "' after '/'", i);
		}

		addToSet(val, end, 0, type);
		i++;
		return i;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public String getExpressionSummary() {
		return new StringBuilder()
				.append("seconds: ").append(getExpressionSetSummary(seconds)).append("\n")
				.append("minutes: ").append(getExpressionSetSummary(minutes)).append("\n")
				.append("hours: ").append(getExpressionSetSummary(hours)).append("\n")
				.append("daysOfMonth: ").append(getExpressionSetSummary(daysOfMonth)).append("\n")
				.append("months: ").append(getExpressionSetSummary(months)).append("\n")
				.append("daysOfWeek: ").append(getExpressionSetSummary(daysOfWeek)).append("\n")
				.append("lastdayOfWeek: ").append(lastdayOfWeek).append("\n")
				.append("nearestWeekday: ").append(nearestWeekday).append("\n")
				.append("NthDayOfWeek: ").append(nthdayOfWeek).append("\n")
				.append("lastdayOfMonth: ").append(lastdayOfMonth).append("\n")
				.append("years: ").append(getExpressionSetSummary(years)).append("\n")
				.toString();
	}

	private static String getExpressionSetSummary(final java.util.Set<Integer> set) {
		if (set.contains(NO_SPEC)) {
			return "?";
		}
		if (set.contains(ALL_SPEC)) {
			return "*";
		}

		return set
				.stream()
				.map(iVal -> iVal.toString())
				.collect(Collectors.joining(","));
	}

	private static int skipWhiteSpace(final int i, final String s) {
		int j = i;
		for (; j < s.length() && (s.charAt(j) == ' ' || s.charAt(j) == '\t'); j++) {
			//
		}

		return j;
	}

	private static int findNextWhiteSpace(final int i, final String s) {
		int j = i;
		for (; j < s.length() && (s.charAt(j) != ' ' || s.charAt(j) != '\t'); j++) {
			//
		}
		return j;
	}

	private void addToSet(final int val, final int end, final int incr, final int type) throws ParseException {

		int incr2 = incr;
		final TreeSet<Integer> set = getSet(type);

		switch (type) {
			case SECOND:
			case MINUTE:
				if ((val < 0 || val > 59 || end > 59) && (val != ALL_SPEC_INT)) {
					throw new ParseException(
							"Minute and Second values must be between 0 and 59",
							-1);
				}
				break;
			case HOUR:
				if ((val < 0 || val > 23 || end > 23) && (val != ALL_SPEC_INT)) {
					throw new ParseException(
							"Hour values must be between 0 and 23", -1);
				}
				break;
			case DAY_OF_MONTH:
				if ((val < 1 || val > 31 || end > 31) && (val != ALL_SPEC_INT)
						&& (val != NO_SPEC_INT)) {
					throw new ParseException(
							"Day of month values must be between 1 and 31", -1);
				}
				break;
			case MONTH:
				if ((val < 1 || val > 12 || end > 12) && (val != ALL_SPEC_INT)) {
					throw new ParseException("Month values must be between 1 and 12", -1);
				}
				break;
			case DAY_OF_WEEK:
				if ((val == 0 || val > 7 || end > 7) && (val != ALL_SPEC_INT)
						&& (val != NO_SPEC_INT)) {
					throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
				}
				break;
			default:
				break;
		}

		if ((incr2 == 0 || incr2 == -1) && val != ALL_SPEC_INT) {
			if (val != -1) {
				set.add(val);
			} else {
				set.add(NO_SPEC);
			}

			return;
		}

		int startAt = val;
		int stopAt = end;

		if (val == ALL_SPEC_INT && incr2 <= 0) {
			incr2 = 1;
			set.add(ALL_SPEC); // put in a marker, but also fill values
		}

		switch (type) {
			case SECOND:
			case MINUTE:
				if (stopAt == -1) {
					stopAt = 59;
				}
				if (startAt == -1 || startAt == ALL_SPEC_INT) {
					startAt = 0;
				}
				break;
			case HOUR:
				if (stopAt == -1) {
					stopAt = 23;
				}
				if (startAt == -1 || startAt == ALL_SPEC_INT) {
					startAt = 0;
				}
				break;
			case DAY_OF_MONTH:
				if (stopAt == -1) {
					stopAt = 31;
				}
				if (startAt == -1 || startAt == ALL_SPEC_INT) {
					startAt = 1;
				}
				break;
			case MONTH:
				if (stopAt == -1) {
					stopAt = 12;
				}
				if (startAt == -1 || startAt == ALL_SPEC_INT) {
					startAt = 1;
				}
				break;
			case DAY_OF_WEEK:
				if (stopAt == -1) {
					stopAt = 7;
				}
				if (startAt == -1 || startAt == ALL_SPEC_INT) {
					startAt = 1;
				}
				break;
			case YEAR:
				if (stopAt == -1) {
					stopAt = MAX_YEAR;
				}
				if (startAt == -1 || startAt == ALL_SPEC_INT) {
					startAt = 1970;
				}
				break;
			default:
				//On fait quoi
		}

		// if the end of the range is before the start, then we need to overflow into
		// the next day, month etc. This is done by adding the maximum amount for that
		// type, and using modulus max to determine the value being added.
		int max = -1;
		if (stopAt < startAt) {
			switch (type) {
				case SECOND:
					max = 60; //max 60 seconds
					break;
				case MINUTE:
					max = 60; //max 60 minutes
					break;
				case HOUR:
					max = 24;
					break;
				case MONTH:
					max = 12;
					break;
				case DAY_OF_WEEK:
					max = 7;
					break;
				case DAY_OF_MONTH:
					max = 31;
					break;
				case YEAR:
					throw new IllegalArgumentException("Start year must be less than stop year");
				default:
					throw new IllegalArgumentException("Unexpected type encountered");
			}
			stopAt += max;
		}

		for (int i = startAt; i <= stopAt; i += incr2) {
			if (max == -1) {
				// ie: there's no max to overflow over
				set.add(i);
			} else {
				// take the modulus to get the real value
				int i2 = i % max;

				// 1-indexed ranges should not include 0, and should include their max
				if (i2 == 0 && (type == MONTH || type == DAY_OF_WEEK || type == DAY_OF_MONTH)) {
					i2 = max;
				}

				set.add(i2);
			}
		}
	}

	private TreeSet<Integer> getSet(final int type) {
		switch (type) {
			case SECOND:
				return seconds;
			case MINUTE:
				return minutes;
			case HOUR:
				return hours;
			case DAY_OF_MONTH:
				return daysOfMonth;
			case MONTH:
				return months;
			case DAY_OF_WEEK:
				return daysOfWeek;
			case YEAR:
				return years;
			default:
				return new TreeSet<>();
		}
	}

	private static ValueSet getValue(final int v, final String s, final int i) {
		int j = i;
		char c = s.charAt(j);
		final StringBuilder s1 = new StringBuilder(String.valueOf(v));
		while (c >= '0' && c <= '9') {
			s1.append(c);
			j++;
			if (j >= s.length()) {
				break;
			}
			c = s.charAt(j);
		}
		final ValueSet val = new ValueSet();

		val.pos = (j < s.length()) ? j : j + 1;
		val.value = Integer.parseInt(s1.toString());
		return val;
	}

	private static int getNumericValue(final String s, final int i) {
		final int endOfVal = findNextWhiteSpace(i, s);
		final String val = s.substring(i, endOfVal);
		return Integer.parseInt(val);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Computation Functions
	//
	////////////////////////////////////////////////////////////////////////////

	public Instant getNextValidTimeAfter(final Instant afterInstant) {

		final Date afterTime = new Date(afterInstant.toEpochMilli());

		// Computation is based on Gregorian year only.
		final Calendar cl = new java.util.GregorianCalendar(getTimeZone());

		// move ahead one second, since we're computing the time *after* the
		// given time
		// CronTrigger does not deal with milliseconds
		cl.setTime(new Date(afterInstant.toEpochMilli() + 1000));
		cl.set(Calendar.MILLISECOND, 0);

		boolean gotOne = false;
		// loop until we've computed the next time, or we've past the endTime
		while (!gotOne) {

			//if (endTime != null && cl.getTime().after(endTime)) return null;
			if (cl.get(Calendar.YEAR) > 2999) { // prevent endless loop...
				return null;
			}

			SortedSet<Integer> st = null;
			int t = 0;

			int sec = cl.get(Calendar.SECOND);
			int min = cl.get(Calendar.MINUTE);

			// get second.................................................
			st = seconds.tailSet(sec);
			if (st != null && st.size() != 0) {
				sec = st.first();
			} else {
				sec = seconds.first();
				min++;
				cl.set(Calendar.MINUTE, min);
			}
			cl.set(Calendar.SECOND, sec);

			min = cl.get(Calendar.MINUTE);
			int hr = cl.get(Calendar.HOUR_OF_DAY);
			t = -1;

			// get minute.................................................
			st = minutes.tailSet(min);
			if (st != null && st.size() != 0) {
				t = min;
				min = st.first();
			} else {
				min = minutes.first();
				hr++;
			}
			if (min != t) {
				cl.set(Calendar.SECOND, 0);
				cl.set(Calendar.MINUTE, min);
				setCalendarHour(cl, hr);
				continue;
			}
			cl.set(Calendar.MINUTE, min);

			hr = cl.get(Calendar.HOUR_OF_DAY);
			int day = cl.get(Calendar.DAY_OF_MONTH);
			t = -1;

			// get hour...................................................
			st = hours.tailSet(hr);
			if (st != null && st.size() != 0) {
				t = hr;
				hr = st.first();
			} else {
				hr = hours.first();
				day++;
			}
			if (hr != t) {
				cl.set(Calendar.SECOND, 0);
				cl.set(Calendar.MINUTE, 0);
				cl.set(Calendar.DAY_OF_MONTH, day);
				setCalendarHour(cl, hr);
				continue;
			}
			cl.set(Calendar.HOUR_OF_DAY, hr);

			day = cl.get(Calendar.DAY_OF_MONTH);
			int mon = cl.get(Calendar.MONTH) + 1;
			// '+ 1' because calendar is 0-based for this field, and we are
			// 1-based
			t = -1;
			int tmon = mon;

			// get day...................................................
			final boolean dayOfMSpec = !daysOfMonth.contains(NO_SPEC);
			final boolean dayOfWSpec = !daysOfWeek.contains(NO_SPEC);
			if (dayOfMSpec && !dayOfWSpec) { // get day by day of month rule
				st = daysOfMonth.tailSet(day);
				if (lastdayOfMonth) {
					if (!nearestWeekday) {
						t = day;
						day = MONTHS.getMonthByNumber(mon).getLastDayOfMonth(cl.get(Calendar.YEAR));
						day -= lastdayOffset;
						if (t > day) {
							mon++;
							if (mon > 12) {
								mon = 1;
								tmon = 3333; // ensure test of mon != tmon further below fails
								cl.add(Calendar.YEAR, 1);
							}
							day = 1;
						}
					} else {
						t = day;
						day = MONTHS.getMonthByNumber(mon).getLastDayOfMonth(cl.get(Calendar.YEAR));
						day -= lastdayOffset;

						final java.util.Calendar tcal = java.util.Calendar.getInstance(getTimeZone());
						tcal.set(Calendar.SECOND, 0);
						tcal.set(Calendar.MINUTE, 0);
						tcal.set(Calendar.HOUR_OF_DAY, 0);
						tcal.set(Calendar.DAY_OF_MONTH, day);
						tcal.set(Calendar.MONTH, mon - 1);
						tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));

						final int ldom = MONTHS.getMonthByNumber(mon).getLastDayOfMonth(cl.get(Calendar.YEAR));
						final int dow = tcal.get(Calendar.DAY_OF_WEEK);

						if (dow == Calendar.SATURDAY && day == 1) {
							day += 2;
						} else if (dow == Calendar.SATURDAY) {
							day -= 1;
						} else if (dow == Calendar.SUNDAY && day == ldom) {
							day -= 2;
						} else if (dow == Calendar.SUNDAY) {
							day += 1;
						}

						tcal.set(Calendar.SECOND, sec);
						tcal.set(Calendar.MINUTE, min);
						tcal.set(Calendar.HOUR_OF_DAY, hr);
						tcal.set(Calendar.DAY_OF_MONTH, day);
						tcal.set(Calendar.MONTH, mon - 1);
						final Date nTime = tcal.getTime();
						if (nTime.before(afterTime)) {
							day = 1;
							mon++;
						}
					}
				} else if (nearestWeekday) {
					t = day;
					day = daysOfMonth.first();

					final java.util.Calendar tcal = java.util.Calendar.getInstance(getTimeZone());
					tcal.set(Calendar.SECOND, 0);
					tcal.set(Calendar.MINUTE, 0);
					tcal.set(Calendar.HOUR_OF_DAY, 0);
					tcal.set(Calendar.DAY_OF_MONTH, day);
					tcal.set(Calendar.MONTH, mon - 1);
					tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));

					final int ldom = MONTHS.getMonthByNumber(mon).getLastDayOfMonth(cl.get(Calendar.YEAR));
					final int dow = tcal.get(Calendar.DAY_OF_WEEK);

					if (dow == Calendar.SATURDAY && day == 1) {
						day += 2;
					} else if (dow == Calendar.SATURDAY) {
						day -= 1;
					} else if (dow == Calendar.SUNDAY && day == ldom) {
						day -= 2;
					} else if (dow == Calendar.SUNDAY) {
						day += 1;
					}

					tcal.set(Calendar.SECOND, sec);
					tcal.set(Calendar.MINUTE, min);
					tcal.set(Calendar.HOUR_OF_DAY, hr);
					tcal.set(Calendar.DAY_OF_MONTH, day);
					tcal.set(Calendar.MONTH, mon - 1);
					final Date nTime = tcal.getTime();
					if (nTime.before(afterTime)) {
						day = daysOfMonth.first();
						mon++;
					}
				} else if (st != null && st.size() != 0) {
					t = day;
					day = st.first();
					// make sure we don't over-run a short month, such as february
					final int lastDay = MONTHS.getMonthByNumber(mon).getLastDayOfMonth(cl.get(Calendar.YEAR));
					if (day > lastDay) {
						day = daysOfMonth.first();
						mon++;
					}
				} else {
					day = daysOfMonth.first();
					mon++;
				}

				if (day != t || mon != tmon) {
					cl.set(Calendar.SECOND, 0);
					cl.set(Calendar.MINUTE, 0);
					cl.set(Calendar.HOUR_OF_DAY, 0);
					cl.set(Calendar.DAY_OF_MONTH, day);
					cl.set(Calendar.MONTH, mon - 1);
					// '- 1' because calendar is 0-based for this field, and we
					// are 1-based
					continue;
				}
			} else if (dayOfWSpec && !dayOfMSpec) { // get day by day of week rule
				if (lastdayOfWeek) { // are we looking for the last XXX day of
					// the month?
					final int dow = daysOfWeek.first(); // desired
					// d-o-w
					final int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
					int daysToAdd = 0;
					if (cDow < dow) {
						daysToAdd = dow - cDow;
					}
					if (cDow > dow) {
						daysToAdd = dow + (7 - cDow);
					}

					final int lDay = MONTHS.getMonthByNumber(mon).getLastDayOfMonth(cl.get(Calendar.YEAR));

					if (day + daysToAdd > lDay) { // did we already miss the
						// last one?
						cl.set(Calendar.SECOND, 0);
						cl.set(Calendar.MINUTE, 0);
						cl.set(Calendar.HOUR_OF_DAY, 0);
						cl.set(Calendar.DAY_OF_MONTH, 1);
						cl.set(Calendar.MONTH, mon);
						// no '- 1' here because we are promoting the month
						continue;
					}

					// find date of last occurrence of this day in this month...
					while ((day + daysToAdd + 7) <= lDay) {
						daysToAdd += 7;
					}

					day += daysToAdd;

					if (daysToAdd > 0) {
						cl.set(Calendar.SECOND, 0);
						cl.set(Calendar.MINUTE, 0);
						cl.set(Calendar.HOUR_OF_DAY, 0);
						cl.set(Calendar.DAY_OF_MONTH, day);
						cl.set(Calendar.MONTH, mon - 1);
						// '- 1' here because we are not promoting the month
						continue;
					}

				} else if (nthdayOfWeek != 0) {
					// are we looking for the Nth XXX day in the month?
					final int dow = daysOfWeek.first(); // desired
					// d-o-w
					final int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
					int daysToAdd = 0;
					if (cDow < dow) {
						daysToAdd = dow - cDow;
					} else if (cDow > dow) {
						daysToAdd = dow + (7 - cDow);
					}

					boolean dayShifted = false;
					if (daysToAdd > 0) {
						dayShifted = true;
					}

					day += daysToAdd;
					int weekOfMonth = day / 7;
					if (day % 7 > 0) {
						weekOfMonth++;
					}

					daysToAdd = (nthdayOfWeek - weekOfMonth) * 7;
					day += daysToAdd;
					if (daysToAdd < 0
							|| day > MONTHS.getMonthByNumber(mon).getLastDayOfMonth(cl.get(Calendar.YEAR))) {
						cl.set(Calendar.SECOND, 0);
						cl.set(Calendar.MINUTE, 0);
						cl.set(Calendar.HOUR_OF_DAY, 0);
						cl.set(Calendar.DAY_OF_MONTH, 1);
						cl.set(Calendar.MONTH, mon);
						// no '- 1' here because we are promoting the month
						continue;
					} else if (daysToAdd > 0 || dayShifted) {
						cl.set(Calendar.SECOND, 0);
						cl.set(Calendar.MINUTE, 0);
						cl.set(Calendar.HOUR_OF_DAY, 0);
						cl.set(Calendar.DAY_OF_MONTH, day);
						cl.set(Calendar.MONTH, mon - 1);
						// '- 1' here because we are NOT promoting the month
						continue;
					}
				} else {
					final int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
					int dow = daysOfWeek.first(); // desired
					// d-o-w
					st = daysOfWeek.tailSet(cDow);
					if (st != null && st.size() > 0) {
						dow = st.first();
					}

					int daysToAdd = 0;
					if (cDow < dow) {
						daysToAdd = dow - cDow;
					}
					if (cDow > dow) {
						daysToAdd = dow + (7 - cDow);
					}

					final int lDay = MONTHS.getMonthByNumber(mon).getLastDayOfMonth(cl.get(Calendar.YEAR));

					if (day + daysToAdd > lDay) { // will we pass the end of
						// the month?
						cl.set(Calendar.SECOND, 0);
						cl.set(Calendar.MINUTE, 0);
						cl.set(Calendar.HOUR_OF_DAY, 0);
						cl.set(Calendar.DAY_OF_MONTH, 1);
						cl.set(Calendar.MONTH, mon);
						// no '- 1' here because we are promoting the month
						continue;
					} else if (daysToAdd > 0) { // are we swithing days?
						cl.set(Calendar.SECOND, 0);
						cl.set(Calendar.MINUTE, 0);
						cl.set(Calendar.HOUR_OF_DAY, 0);
						cl.set(Calendar.DAY_OF_MONTH, day + daysToAdd);
						cl.set(Calendar.MONTH, mon - 1);
						// '- 1' because calendar is 0-based for this field,
						// and we are 1-based
						continue;
					}
				}
			} else { // dayOfWSpec && !dayOfMSpec
				throw new UnsupportedOperationException(
						"Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");
			}
			cl.set(Calendar.DAY_OF_MONTH, day);

			mon = cl.get(Calendar.MONTH) + 1;
			// '+ 1' because calendar is 0-based for this field, and we are
			// 1-based
			int year = cl.get(Calendar.YEAR);
			t = -1;

			// test for expressions that never generate a valid fire date,
			// but keep looping...
			if (year > MAX_YEAR) {
				return null;
			}

			// get month...................................................
			st = months.tailSet(mon);
			if (st != null && st.size() != 0) {
				t = mon;
				mon = st.first();
			} else {
				mon = months.first();
				year++;
			}
			if (mon != t) {
				cl.set(Calendar.SECOND, 0);
				cl.set(Calendar.MINUTE, 0);
				cl.set(Calendar.HOUR_OF_DAY, 0);
				cl.set(Calendar.DAY_OF_MONTH, 1);
				cl.set(Calendar.MONTH, mon - 1);
				// '- 1' because calendar is 0-based for this field, and we are
				// 1-based
				cl.set(Calendar.YEAR, year);
				continue;
			}
			cl.set(Calendar.MONTH, mon - 1);
			// '- 1' because calendar is 0-based for this field, and we are
			// 1-based

			year = cl.get(Calendar.YEAR);
			t = -1;

			// get year...................................................
			st = years.tailSet(year);
			if (st != null && st.size() != 0) {
				t = year;
				year = st.first();
			} else {
				return null; // ran out of years...
			}

			if (year != t) {
				cl.set(Calendar.SECOND, 0);
				cl.set(Calendar.MINUTE, 0);
				cl.set(Calendar.HOUR_OF_DAY, 0);
				cl.set(Calendar.DAY_OF_MONTH, 1);
				cl.set(Calendar.MONTH, 0);
				// '- 1' because calendar is 0-based for this field, and we are
				// 1-based
				cl.set(Calendar.YEAR, year);
				continue;
			}
			cl.set(Calendar.YEAR, year);

			gotOne = true;
		} // while( !done )

		return cl.toInstant();
	}

	/**
	 * Advance the calendar to the particular hour paying particular attention
	 * to daylight saving problems.
	 *
	 * @param cal the calendar to operate on
	 * @param hour the hour to set
	 */
	private static void setCalendarHour(final Calendar cal, final int hour) {
		cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
		if (cal.get(java.util.Calendar.HOUR_OF_DAY) != hour && hour != 24) {
			cal.set(java.util.Calendar.HOUR_OF_DAY, hour + 1);
		}
	}

}

class ValueSet {
	public int value;

	public int pos;
}
