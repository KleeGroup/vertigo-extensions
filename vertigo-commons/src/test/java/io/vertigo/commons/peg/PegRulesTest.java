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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PegRulesTest {
	/**
	 * hi -> 0
	 * ho -> 1
	 * ha -> 2
	 */
	private final PegRule<PegChoice> choice = PegRules.choice(PegRules.term("hi"), PegRules.term("ho"), PegRules.term("ha"));
	private final PegRule sequence = PegRules.sequence(PegRules.term("hi"), PegRules.term("ho"), PegRules.term("ha"));
	private final PegRule oneOrMore = PegRules.oneOrMore(PegRules.term("hi"), true);
	private final PegRule zeroOrMore = PegRules.zeroOrMore(PegRules.term("hi"), true);
	private final PegRule skipBlanks = PegRules.skipBlanks("-*+");

	@Test
	public void choice() throws PegNoMatchFoundException {
		Assertions.assertEquals(0, choice.parse("hi").getValue().getChoiceIndex());
		Assertions.assertEquals(1, choice.parse("ho").getValue().getChoiceIndex());
		Assertions.assertEquals(2, choice.parse("ha").getValue().getChoiceIndex());
	}

	@Test
	public void choice2() {
		Assertions.assertThrows(PegNoMatchFoundException.class, () -> choice.parse("hu"));
	}

	@Test
	public void sequence() throws PegNoMatchFoundException {
		Assertions.assertEquals(List.of("hi", "ho", "ha"), sequence.parse("hihoha").getValue());
	}

	@Test
	public void sequence2() {
		Assertions.assertThrows(PegNoMatchFoundException.class, () -> {
			Assertions.assertEquals(Arrays.asList("hi", "ho", "ha"), sequence.parse("hiho").getValue());
		});
	}

	@Test
	public void optional() throws PegNoMatchFoundException {
		//option is not found => index =0
		Assertions.assertEquals(0, PegRules.optional(choice).parse("hu").getIndex());
		//option is found => index =2
		Assertions.assertEquals(2, PegRules.optional(choice).parse("ha").getIndex());
	}

	@Test
	public void oneOrMoreUntilTheEnd() throws PegNoMatchFoundException {
		Assertions.assertEquals(List.of("hi", "hi", "hi"), oneOrMore.parse("hihihi").getValue());
	}

	@Test
	public void oneOrMoreUntilTheEnd2() {
		Assertions.assertThrows(PegNoMatchFoundException.class, () -> oneOrMore.parse("hihihiho"));
	}

	@Test
	public void zerOrMoreUntilTheEnd() throws PegNoMatchFoundException {
		Assertions.assertEquals(0, zeroOrMore.parse("").getIndex());
		Assertions.assertEquals(List.of("hi", "hi", "hi"), zeroOrMore.parse("hihihi").getValue());
	}

	@Test
	public void zeroOrMoreUntilTheEnd2() {
		Assertions.assertThrows(PegNoMatchFoundException.class, () -> zeroOrMore.parse("hihihiho"));
	}

	@Test
	public void skipBlanks() throws PegNoMatchFoundException {
		Assertions.assertEquals(10, skipBlanks.parse("+++****+++").getIndex());
	}

	@Test
	public void asHtml() {
		Assertions.assertNotEquals("", new PegRulesHtmlRenderer().render(choice));
		Assertions.assertNotEquals("", new PegRulesHtmlRenderer().render(sequence));
		Assertions.assertNotEquals("", new PegRulesHtmlRenderer().render(oneOrMore));
		Assertions.assertNotEquals("", new PegRulesHtmlRenderer().render(zeroOrMore));
		Assertions.assertNotEquals("", new PegRulesHtmlRenderer().render(skipBlanks));
	}
}
