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
package io.vertigo.ui.core;

import java.io.Serializable;
import java.util.UUID;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.Node;
import io.vertigo.datastore.kvstore.KVStoreManager;

/**
 * @author npiedeloup
 */
public final class ProtectedValueUtil {
	public static final String PROTECTED_VALUE_COLLECTION_NAME = "protected-value";

	/**
	 * Genere et conserve une URL protégée.
	 */
	public static String generateProtectedValue(final Serializable unprotectedValue) {
		if (unprotectedValue == null) {
			return null;
		}
		//unprotectedValue is not null here
		final String protectedUrl = protectValue(unprotectedValue);
		try (VTransactionWritable transactionWritable = getTransactionManager().createCurrentTransaction()) {
			getKVStoreManager().put(PROTECTED_VALUE_COLLECTION_NAME, protectedUrl, unprotectedValue);
			transactionWritable.commit();
		}
		return protectedUrl;
	}

	/**
	 * @param unprotectedValue value to protect (may or may not used)
	 */
	private static String protectValue(final Serializable unprotectedValue) {
		return UUID.randomUUID().toString();
	}

	/**
	 * Resoud une value protégée.
	 */
	public static <V extends Serializable> V readProtectedValue(final String protectedValue, final Class<V> clazz) {
		if (protectedValue == null) {
			return null;
		}
		try (VTransactionWritable transactionWritable = getTransactionManager().createCurrentTransaction()) {
			final V unprotectedValue;
			unprotectedValue = getKVStoreManager().find(PROTECTED_VALUE_COLLECTION_NAME, protectedValue, clazz).orElse(null);
			return unprotectedValue;
		}
	}

	private static VTransactionManager getTransactionManager() {
		return Node.getNode().getComponentSpace().resolve(VTransactionManager.class);
	}

	private static KVStoreManager getKVStoreManager() {
		return Node.getNode().getComponentSpace().resolve(KVStoreManager.class);
	}

}
