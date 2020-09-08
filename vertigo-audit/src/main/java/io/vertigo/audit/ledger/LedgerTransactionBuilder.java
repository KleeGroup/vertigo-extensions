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

import java.math.BigInteger;

import io.vertigo.core.lang.Builder;

/**
 *
 * @author xdurand
 *
 */
public final class LedgerTransactionBuilder implements Builder<LedgerTransaction> {
	private String myHash;
	private BigInteger myNonce;
	private String myBlockHash;
	private BigInteger myBlockNumber;
	private BigInteger myTransactionIndex;
	private String myFrom;
	private String myTo;
	private BigInteger myValue;
	private String myMessage;

	LedgerTransactionBuilder() {
		super();
	}

	/**
	 * @param hash the hash to set
	 */
	public LedgerTransactionBuilder withHash(final String hash) {
		myHash = hash;
		return this;
	}

	/**
	 * @param nonce the nonce to set
	 */
	public LedgerTransactionBuilder withNonce(final BigInteger nonce) {
		myNonce = nonce;
		return this;
	}

	/**
	 * @param blockHash the blockHash to set
	 */
	public LedgerTransactionBuilder withBlockHash(final String blockHash) {
		myBlockHash = blockHash;
		return this;
	}

	/**
	 * @param blockNumber the blockNumber to set
	 */
	public LedgerTransactionBuilder withBlockNumber(final BigInteger blockNumber) {
		myBlockNumber = blockNumber;
		return this;
	}

	/**
	 * @param transactionIndex the transactionIndex to set
	 */
	public LedgerTransactionBuilder withTransactionIndex(final BigInteger transactionIndex) {
		myTransactionIndex = transactionIndex;
		return this;
	}

	/**
	 * @param from the from to set
	 */
	public LedgerTransactionBuilder withFrom(final String from) {
		myFrom = from;
		return this;
	}

	/**
	 * @param to the to to set
	 */
	public LedgerTransactionBuilder withTo(final String to) {
		myTo = to;
		return this;
	}

	/**
	 * @param value the value to set
	 */
	public LedgerTransactionBuilder withValue(final BigInteger value) {
		myValue = value;
		return this;
	}

	/**
	 * @param message the message to set
	 */
	public LedgerTransactionBuilder withMessage(final String message) {
		myMessage = message;
		return this;
	}

	@Override
	public LedgerTransaction build() {
		return new LedgerTransaction(
				myHash,
				myNonce,
				myBlockHash,
				myBlockNumber,
				myTransactionIndex,
				myFrom,
				myTo,
				myValue,
				myMessage);
	}

}
