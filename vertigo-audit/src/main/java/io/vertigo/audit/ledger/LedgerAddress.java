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

public final class LedgerAddress {

	private final String accountName;
	private final String publicAddress;

	public LedgerAddress(final String accountName, final String publicAddress) {
		this.publicAddress = publicAddress;
		this.accountName = accountName;
	}

	/**
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * @return the publicAddress
	 */
	public String getPublicAddress() {
		return publicAddress;
	}

	@Override
	public String toString() {
		return "LedgerAddress: " + accountName + " [" + publicAddress + "]";
	}

}
