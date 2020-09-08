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
package io.vertigo.audit.impl.ledger;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.audit.ledger.LedgerAddress;
import io.vertigo.audit.ledger.LedgerManager;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Tuple;

public final class LedgerManagerImpl implements LedgerManager {
	private static final Logger LOGGER = LogManager.getLogger(LedgerManagerImpl.class);

	private final CodecManager codecManager;
	private final LedgerPlugin ledgerPlugin;

	private final ConcurrentLinkedQueue<Tuple<String, Runnable>> messageQueue = new ConcurrentLinkedQueue<>();

	@Inject
	public LedgerManagerImpl(final CodecManager codecManager, final LedgerPlugin ledgerPlugin) {
		Assertion.check()
				.isNotNull(codecManager)
				.isNotNull(ledgerPlugin);
		//---
		this.codecManager = codecManager;
		this.ledgerPlugin = ledgerPlugin;
	}

	@Override
	public String sendData(final String data) {
		Assertion.check().isNotBlank(data);
		//---
		final String hash = codecManager.getHexEncoder().encode(data.getBytes(StandardCharsets.UTF_8));
		LOGGER.info("Sending transaction to the legder... Buffer:{}", hash);
		ledgerPlugin.sendData(hash);
		LOGGER.info("Transaction successfully written on the legder.");
		return hash;
	}

	@Override
	public void sendDataAsync(final String data, final Runnable callback) {
		messageQueue.add(Tuple.of(data, callback));
	}

	/**
	 * Daemon to unstack processes to end them
	 */
	@DaemonScheduled(name = "DmnLedgerFlushMessages", periodInSeconds = 10)
	public void pollQueue() {
		while (!messageQueue.isEmpty()) {
			final Tuple<String, Runnable> messageAndCallBack = messageQueue.poll();
			final String message = messageAndCallBack.getVal1();
			if (message != null) {
				sendData(message);
				messageAndCallBack.getVal2().run();
			}
		}
	}

	@Override
	public BigInteger getWalletBalance(final LedgerAddress ledgerAddress) {
		Assertion.check().isNotNull(ledgerAddress);
		//---
		return ledgerPlugin.getWalletBalance(ledgerAddress);
	}

	@Override
	public BigInteger getMyWalletBalance() {
		return ledgerPlugin.getMyWalletBalance();
	}

}
