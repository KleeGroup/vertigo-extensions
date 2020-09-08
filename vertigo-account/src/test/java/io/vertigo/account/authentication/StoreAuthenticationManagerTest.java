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
package io.vertigo.account.authentication;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.vertigo.account.authentication.MyNodeConfig.AuthentPlugin;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.config.NodeConfig;

/**
 * Implementation standard de la gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public final class StoreAuthenticationManagerTest extends AbstractAuthenticationManagerTest {
	private VTransactionWritable tx;

	@Override
	protected NodeConfig buildNodeConfig() {
		return MyNodeConfig.config(AuthentPlugin.store, false);
	}

	@BeforeEach
	public void initDb() {
		CreateTestDataBase.initMainStore();
	}

	@BeforeEach
	public void createTx() throws Exception {
		tx = obtainTx();
	}

	@AfterEach
	public void finishTx() throws Exception {
		if (tx != null) {
			tx.close();
		}
	}

	protected VTransactionWritable obtainTx() {
		return Node.getNode().getComponentSpace().resolve(VTransactionManager.class).createCurrentTransaction();
	}

}
