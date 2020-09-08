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
package io.vertigo.commons.peg;

/**
 * Rule to named a sub rules set.
 *
 * @author npiedeloup
 */
public final class PegLogger {
	private static final boolean DISABLED = true;

	private static int tabs = 1;

	public static void look(final String category, final String ruleName, final int start, final PegRule<?> rule) {
		if (DISABLED) {
			return;
		}
		tabs++;
		final String tabStr = String.format("%" + tabs * 4 + "s", "");
		System.out.println(tabStr + "Look  " + category + " " + ruleName + " @" + start + " : " + rule.getClass().getSimpleName() + "@" + System.identityHashCode(rule));
	}

	public static void log(final String message) {
		if (DISABLED) {
			return;
		}
		final String tabStr = String.format("%" + tabs * 4 + "s", "");
		System.out.println(tabStr + message);
	}

	public static void found(final String category, final String ruleName, final int start, final int end, final String text, final PegRule<?> rule) {
		if (DISABLED) {
			return;
		}
		final String tabStr = String.format("%" + tabs * 4 + "s", "");
		tabs--;
		System.out.println(tabStr + "Found  " + category + " " + ruleName + " @" + start + "-" + end + " '" + text.substring(start, end) + "' : " + rule.getClass().getSimpleName() + "@" + System.identityHashCode(rule));
	}

	public static void miss(final String category, final String ruleName, final int start, final PegRule<?> rule) {
		if (DISABLED) {
			return;
		}
		final String tabStr = String.format("%" + tabs * 4 + "s", "");
		tabs--;
		System.out.println(tabStr + "Miss  " + category + " " + ruleName + " @" + start + " : " + rule.getClass().getSimpleName() + "@" + System.identityHashCode(rule));
	}

}
