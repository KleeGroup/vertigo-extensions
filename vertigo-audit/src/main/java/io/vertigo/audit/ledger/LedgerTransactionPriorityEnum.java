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
package io.vertigo.audit.ledger;

public enum LedgerTransactionPriorityEnum {

	VERYSLOW(5), //
	SLOW(50), //ETH: ~0.15 GWEI (~$0.000728), ~12H00-35H00
	MEDIUM(500), //ETH: ~1.5-4.5 GWEI (~$0.00728), ~10 minutes
	FAST(650), // ETH: ~3-5 GWEI (~$0.02), ~3 minutes
	VERYFAST(1000); // ETH: ~20 GWEI (~$0.10), ~10 secondes

	private final int perMille;

	/**
	 *
	 * @param percentage
	 */
	private LedgerTransactionPriorityEnum(final int perMille) {
		this.perMille = perMille;
	}

	/**
	 * @return the percentage
	 */
	public int getPermille() {
		return perMille;
	}

}
