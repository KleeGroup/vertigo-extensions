/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.x.comment.data;

import java.util.Arrays;
import java.util.List;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.util.ListBuilder;
import io.vertigo.x.account.services.Account;
import io.vertigo.x.account.services.AccountBuilder;
import io.vertigo.x.account.services.AccountGroup;
import io.vertigo.x.account.services.AccountServices;

public class KeyConcepts {

	public static void initData(final AccountServices accountServices) {
		final Account testAccount0 = new AccountBuilder("0").withDisplayName("John doe").withEmail("john.doe@yopmail.com").build();
		final Account testAccount1 = new AccountBuilder("1").withDisplayName("Palmer Luckey").withEmail("palmer.luckey@yopmail.com").build();
		final Account testAccount2 = new AccountBuilder("2").withDisplayName("Bill Clinton").withEmail("bill.clinton@yopmail.com").build();
		accountServices.getStore().saveAccounts(Arrays.asList(testAccount0, testAccount1, testAccount2));

		final URI<Account> accountURI1 = DtObjectUtil.createURI(Account.class, testAccount1.getId());
		final URI<Account> accountURI2 = DtObjectUtil.createURI(Account.class, testAccount2.getId());

		final AccountGroup testAccountGroup1 = new AccountGroup("100", "TIME's cover");
		final URI<AccountGroup> group1Uri = DtObjectUtil.createURI(AccountGroup.class, testAccountGroup1.getId());
		accountServices.getStore().saveGroup(testAccountGroup1);

		accountServices.getStore().attach(accountURI1, group1Uri);
		accountServices.getStore().attach(accountURI2, group1Uri);

		final AccountGroup groupAll = new AccountGroup("ALL", "Everyone");
		final URI<AccountGroup> groupAllUri = DtObjectUtil.createURI(AccountGroup.class, groupAll.getId());
		accountServices.getStore().saveGroup(groupAll);
		accountServices.getStore().attach(accountURI1, groupAllUri);
		accountServices.getStore().attach(accountURI2, groupAllUri);

		//---create 5 000 noisy data
		final List<Account> accounts = createAccounts();
		for (final Account account : accounts) {
			final URI<Account> accountUri = DtObjectUtil.createURI(Account.class, account.getId());
			accountServices.getStore().attach(accountUri, groupAllUri);
		}
		accountServices.getStore().saveAccounts(accounts);

	}

	public static int id = 10;

	private static List<Account> createAccounts() {
		return new ListBuilder<Account>()
				.add(createAccount("Jean Meunier", "jmeunier@yopmail.com"))
				.add(createAccount("Emeline Granger", "egranger@yopmail.com"))
				.add(createAccount("Silvia Robert", "sylv.robert@yopmail.com"))
				.add(createAccount("Manuel Long", "manu@yopmail.com"))
				.add(createAccount("David Martin", "david.martin@yopmail.com"))
				.add(createAccount("Véronique LeBourgeois", "vero89@yopmail.com"))
				.add(createAccount("Bernard Dufour", "bdufour@yopmail.com"))
				.add(createAccount("Nicolas Legendre", "nicolas.legendre@yopmail.com"))
				.add(createAccount("Marie Garnier", "marie.garnier@yopmail.com"))
				.add(createAccount("Hugo Bertrand", "hb@yopmail.com"))
				.build();
	}

	private static Account createAccount(final String displayName, final String email) {
		return new AccountBuilder(Integer.toString(id++))
				.withDisplayName(displayName)
				.withEmail(email)
				.build();
	}
}
