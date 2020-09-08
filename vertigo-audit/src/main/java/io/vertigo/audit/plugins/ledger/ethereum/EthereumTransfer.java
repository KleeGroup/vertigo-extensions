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
package io.vertigo.audit.plugins.ledger.ethereum;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import io.vertigo.audit.ledger.LedgerTransactionPriorityEnum;

final class EthereumTransfer extends Transfer {

	private static final BigInteger GAS_UNIT_PER_BIT = BigInteger.valueOf(68L);
	private static final BigDecimal GAS_LIMIT_MARGIN = BigDecimal.valueOf(1.10);

	EthereumTransfer(final Web3j web3j, final TransactionManager transactionManager) {
		super(web3j, transactionManager);
	}

	/**
	 *
	 * Given the duration required to execute a transaction, asynchronous execution is strongly
	 * recommended via {@link Transfer#sendFunds(String, BigDecimal, Convert.Unit)}.
	 *
	 * @param toAddress
	 * @param value
	 * @param unit
	 * @param gasPrice
	 * @param gasLimit
	 * @param message
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TransactionException
	 */
	private TransactionReceipt send(
			final String toAddress, final BigDecimal value, final Convert.Unit unit, final String message, final LedgerTransactionPriorityEnum priority) throws IOException, TransactionException {

		final BigDecimal currentGasPrice = new BigDecimal(requestCurrentGasPrice());
		final BigDecimal pmille = BigDecimal.valueOf(priority.getPermille());
		final BigInteger gasPrice = currentGasPrice.multiply(pmille).divide(BigDecimal.valueOf(1000)).toBigInteger();

		final int messageLength = message.length();
		final BigInteger bitSize = BigInteger.valueOf(messageLength % 2 == 0 ? messageLength / 2 : messageLength / 2 + 1);
		final BigInteger storageCost = GAS_UNIT_PER_BIT.multiply(bitSize);
		final BigInteger storageCostWithMargin = (new BigDecimal(storageCost)).multiply(GAS_LIMIT_MARGIN).toBigInteger();
		final BigInteger totalCost = GAS_LIMIT.add(storageCostWithMargin);

		return send(toAddress, value, unit, gasPrice, totalCost, message);
	}

	/**
	 *
	 * @param toAddress
	 * @param value
	 * @param unit
	 * @param gasPrice
	 * @param gasLimit
	 * @param message
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TransactionException
	 */
	private TransactionReceipt send(
			final String toAddress, final BigDecimal value, final Convert.Unit unit, final BigInteger gasPrice,
			final BigInteger gasLimit, final String message) throws IOException, TransactionException {

		final BigDecimal weiValue = Convert.toWei(value, unit);
		if (!Numeric.isIntegerValue(weiValue)) {
			throw new UnsupportedOperationException(
					"Non decimal Wei value provided: " + value + " " + unit
							+ " = " + weiValue + " Wei");
		}

		final String resolvedAddress = ensResolver.resolve(toAddress);
		return send(resolvedAddress, message, weiValue.toBigIntegerExact(), gasPrice, gasLimit);
	}

	/**
	 *
	 * @param web3j
	 * @param credentials
	 * @param toAddress
	 * @param value
	 * @param unit
	 * @param gasPrice
	 * @param gasLimit
	 * @param message
	 * @return
	 */
	static RemoteCall<TransactionReceipt> sendFunds(
			final Web3j web3j,
			final Credentials credentials,
			final String toAddress,
			final BigDecimal value,
			final Convert.Unit unit,
			final BigInteger gasPrice,
			final BigInteger gasLimit,
			final String message) {

		final TransactionManager transactionManager = new RawTransactionManager(web3j, credentials);
		return new RemoteCall<>(() -> new EthereumTransfer(web3j, transactionManager).send(toAddress, value, unit, gasPrice, gasLimit, message));
	}

	/**
	 *
	 * @param web3j
	 * @param credentials
	 * @param toAddress
	 * @param value
	 * @param unit
	 * @param gasPrice
	 * @param gasLimit
	 * @param message
	 * @return
	 */
	static RemoteCall<TransactionReceipt> sendFunds(
			final Web3j web3j,
			final Credentials credentials,
			final String toAddress,
			final BigDecimal value,
			final Convert.Unit unit,
			final String message,
			final LedgerTransactionPriorityEnum priority) {
		final TransactionManager transactionManager = new RawTransactionManager(web3j, credentials);
		return new RemoteCall<>(() -> new EthereumTransfer(web3j, transactionManager).send(toAddress, value, unit, message, priority));
	}

}
