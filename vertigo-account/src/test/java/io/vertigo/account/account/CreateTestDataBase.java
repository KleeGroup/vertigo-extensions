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
package io.vertigo.account.account;

import java.util.List;
import java.util.Optional;

import io.vertigo.account.SqlUtil;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.node.Node;
import io.vertigo.core.util.ListBuilder;
import io.vertigo.datamodel.task.TaskManager;

final class CreateTestDataBase {

	private CreateTestDataBase() {
		//private
	}

	public static void initMainStore() {
		final VTransactionManager transactionManager = Node.getNode().getComponentSpace().resolve(VTransactionManager.class);
		final TaskManager taskManager = Node.getNode().getComponentSpace().resolve(TaskManager.class);

		//A chaque test on recrée la table famille
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateMainStoreRequests(),
				"TkInitMain",
				Optional.empty());
	}

	private static List<String> getCreateMainStoreRequests() {
		return new ListBuilder<String>()
				.addAll(getCreateUserGroupRequests())
				.addAll(getCreateUserRequests())
				.build();
	}

	private static List<String> getCreateUserGroupRequests() {
		return List.of(
				" create table USER_GROUP(GRP_ID varchar(50), NAME varchar(100), COMMENT varchar(100))",
				" create sequence SEQ_USER_GROUP start with 10001 increment by 1",
				"insert into USER_GROUP(GRP_ID, NAME, COMMENT) values (100, 'TIMEs cover', 'Group for user who are on the TIMEs cover')",
				"insert into USER_GROUP(GRP_ID, NAME, COMMENT) values (101, 'Users', 'Group for standard users')",
				"insert into USER_GROUP(GRP_ID, NAME, COMMENT) values (102, 'Readers', 'Group for read only users')",
				"insert into USER_GROUP(GRP_ID, NAME, COMMENT) values ('ALL', 'Everyone', 'Group for everyone')");
	}

	private static List<String> getCreateUserRequests() {
		return List.of(
				" create table USER(USR_ID varchar(50), FULL_NAME varchar(100), EMAIL varchar(100), GRP_ID varchar(50))",
				" create sequence SEQ_USER start with 10001 increment by 1",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (0, 'John Doe', 'john.doe@yopmail.com', 'ALL')",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (1, 'Palmer Luckey', 'palmer.luckey@yopmail.com', 100)",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (2, 'Bill Clinton', 'bill.clinton@yopmail.com', 100)",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (3, 'Phil Mormon', 'phil.mormon@yopmail.com', 'ALL')",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (4, 'Npi', 'npi@vertigo.io', 'ALL')",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (10, 'Bernard Dufour', 'bdufour@yopmail.com', 'ALL')",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (11, 'Nicolas Legendre', 'nicolas.legendre@yopmail.com', 'ALL')",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (12, 'Marie Garnier', 'marie.garnier@yopmail.com', 'ALL')",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (13, 'Hugo Bertrand', 'hb@yopmail.com', 'ALL')",
				"insert into user(USR_ID, FULL_NAME, EMAIL, GRP_ID) values (14, 'Super Admin', 'admin@yopmail.com', 'ALL')");
	}

}
