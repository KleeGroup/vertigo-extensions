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
package io.vertigo.orchestra.services.schedule;

import java.util.Calendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.orchestra.impl.services.schedule.CronExpression;

/**
 * TODO : Description de la classe.
 *
 * @author mlaroche.
 * @version $Id$
 */
public class CronExpressionTest {

	@Test
	public void testSecondsFixed() throws Exception {
		final CronExpression cronExpression = new CronExpression("5 * * * * ?");

		final Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);

		final Calendar expectedCal = Calendar.getInstance();
		expectedCal.setTime(now.getTime());
		expectedCal.add(Calendar.SECOND, 5);

		final Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(now.getTime());
		fromCal.add(Calendar.MINUTE, -1);
		fromCal.add(Calendar.SECOND, +6);// CronExpression precision is limited to one second

		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		expectedCal.add(Calendar.MINUTE, 1);
		fromCal.add(Calendar.MINUTE, 1);
		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		expectedCal.add(Calendar.SECOND, 1);
		Assertions.assertNotEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

	}

	@Test
	public void testSecondsInterval() throws Exception {
		final CronExpression cronExpression = new CronExpression("*/5 * * * * ?");

		final Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);

		final Calendar expectedCal = Calendar.getInstance();
		expectedCal.setTime(now.getTime());

		final Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(now.getTime());
		fromCal.add(Calendar.SECOND, -3);

		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		fromCal.add(Calendar.SECOND, 5);
		expectedCal.add(Calendar.SECOND, 5);
		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		expectedCal.add(Calendar.SECOND, 1);
		Assertions.assertNotEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

	}

	@Test
	public void testMinutesFixed() throws Exception {
		final CronExpression cronExpression = new CronExpression("0 1 * * * ?");

		final Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MINUTE, 0);

		final Calendar expectedCal = Calendar.getInstance();
		expectedCal.setTime(now.getTime());
		expectedCal.add(Calendar.MINUTE, 1);

		final Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(now.getTime());

		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		expectedCal.add(Calendar.MINUTE, 1);
		Assertions.assertNotEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

	}

	@Test
	public void testMinutesInterval() throws Exception {
		final CronExpression cronExpression = new CronExpression("0 */2 * * * ?");

		final Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MINUTE, 0);

		final Calendar expectedCal = Calendar.getInstance();
		expectedCal.setTime(now.getTime());

		final Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(now.getTime());
		expectedCal.add(Calendar.MINUTE, 2);

		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		fromCal.add(Calendar.MINUTE, 2);
		expectedCal.add(Calendar.MINUTE, 2);
		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		expectedCal.add(Calendar.MINUTE, 1);
		Assertions.assertNotEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

	}

	@Test
	public void testHoursFixed() throws Exception {
		final CronExpression cronExpression = new CronExpression("0 0 1 * * ?");

		final Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.HOUR_OF_DAY, 0);

		final Calendar expectedCal = Calendar.getInstance();
		expectedCal.setTime(now.getTime());
		expectedCal.add(Calendar.HOUR_OF_DAY, 1);

		final Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(now.getTime());

		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		expectedCal.add(Calendar.HOUR_OF_DAY, 1);
		Assertions.assertNotEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

	}

	@Test
	public void testDayFixed() throws Exception {
		final CronExpression cronExpression = new CronExpression("0 0 0 2 * ?");

		final Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.DAY_OF_MONTH, 1);

		final Calendar expectedCal = Calendar.getInstance();
		expectedCal.setTime(now.getTime());
		expectedCal.add(Calendar.DAY_OF_MONTH, 1);

		final Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(now.getTime());

		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		expectedCal.add(Calendar.DAY_OF_MONTH, 1);
		Assertions.assertNotEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

	}

	@Test
	public void testMonthFixed() throws Exception {
		// Tous les 1er février
		final CronExpression cronExpression = new CronExpression("0 0 0 1 2 ?");

		final int year = 2015;

		final Calendar expectedCal = Calendar.getInstance();
		expectedCal.set(Calendar.MILLISECOND, 0);
		expectedCal.set(year, 1, 1, 0, 0, 0); // 1er fevrier année n

		final Calendar fromCal = Calendar.getInstance();
		fromCal.set(Calendar.MILLISECOND, 0);
		fromCal.set(year, 0, 5, 0, 0, 0); // 5 Janvier année n

		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

		expectedCal.set(year + 1, 1, 1, 0, 0, 0); // 1er fevrier année n+1
		fromCal.set(year, 1, 3, 0, 0, 0); // 3 février année n

		Assertions.assertEquals(expectedCal.toInstant(), cronExpression.getNextValidTimeAfter(fromCal.toInstant()));

	}

	@Test
	public void testLastDayOffset() throws Exception {
		CronExpression cronExpression = new CronExpression("0 15 10 L-2 * ? 2010");

		final Calendar cal = Calendar.getInstance();

		cal.set(2010, Calendar.OCTOBER, 29, 10, 15, 0); // last day - 2
		Assertions.assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));

		cal.set(2010, Calendar.OCTOBER, 28, 10, 15, 0);
		Assertions.assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));

		cronExpression = new CronExpression("0 15 10 L-5W * ? 2010");

		cal.set(2010, Calendar.OCTOBER, 26, 10, 15, 0); // last day - 5
		Assertions.assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));

		cronExpression = new CronExpression("0 15 10 L-1 * ? 2010");

		cal.set(2010, Calendar.OCTOBER, 30, 10, 15, 0); // last day - 1
		Assertions.assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));

		cronExpression = new CronExpression("0 15 10 L-1W * ? 2010");

		cal.set(2010, Calendar.OCTOBER, 29, 10, 15, 0); // nearest weekday to last day - 1 (29th is a friday in 2010)
		Assertions.assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));

	}

}
