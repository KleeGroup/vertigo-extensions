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
package io.vertigo.account.plugins.account.cache.redis;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountCachePlugin;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.connectors.redis.RedisConnector;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.filestore.model.VFile;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * @author pchretien
 */
public final class RedisAccountCachePlugin implements AccountCachePlugin {
	private static final String HPHOTO_BY_ACCOUNT_START_KEY = "photoByAccount:";
	private static final String HGROUP_START_KEY = "group:";
	private static final String HACCOUNT_START_KEY = "account:";
	private static final String HAUTHTOKEN_INDEX_KEY = "authToken";

	private static final String SGROUPS_BY_ACCOUNT_START_KEY = "groupsByAccount:";
	private static final String SACCOUNTS_BY_GROUP_START_KEY = "accountsByGroup:";

	private static final String SGROUPS_KEY = "groups";
	private static final String SACCOUNTS_KEY = "accounts";

	private final RedisConnector redisConnector;
	private final PhotoCodec photoCodec;

	/**
	 * @param redisConnectors Connector Redis
	 * @param codecManager Codec manager
	 */
	@Inject
	public RedisAccountCachePlugin(
			@ParamValue("connectorName") final Optional<String> connectorNameOpt,
			final List<RedisConnector> redisConnectors,
			final CodecManager codecManager) {
		Assertion.check()
				.isNotNull(connectorNameOpt)
				.isNotNull(redisConnectors)
				.isNotNull(codecManager);
		//-----

		final String connectorName = connectorNameOpt.orElse("main");
		redisConnector = redisConnectors.stream()
				.filter(connector -> connectorName.equals(connector.getName()))
				.findFirst().get();
		photoCodec = new PhotoCodec(codecManager);
	}

	/** {@inheritDoc} */
	@Override
	public void putAccount(final Account account) {
		Assertion.check().isNotNull(account);
		//-----
		try (final Jedis jedis = redisConnector.getClient()) {
			try (final Transaction tx = jedis.multi()) {
				tx.hmset(HACCOUNT_START_KEY + account.getId(), account2Map(account));
				tx.hset(HAUTHTOKEN_INDEX_KEY, account.getAuthToken(), account.getId());
				tx.sadd(SACCOUNTS_KEY, account.getId());
				tx.exec();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccount(final UID<Account> accountUID) {
		Assertion.check().isNotNull(accountUID);
		//-----
		try (final Jedis jedis = redisConnector.getClient()) {
			final String key = HACCOUNT_START_KEY + accountUID.getId();
			if (jedis.exists(key)) {
				return Optional.of(map2Account(jedis.hgetAll(key)));
			}
			return Optional.empty();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void putGroup(final AccountGroup group) {
		Assertion.check().isNotNull(group);
		//-----
		//----
		try (final Jedis jedis = redisConnector.getClient()) {
			try (final Transaction tx = jedis.multi()) {
				tx.hmset(HGROUP_START_KEY + group.getId(), group2Map(group));
				tx.sadd(SGROUPS_KEY, group.getId());
				tx.exec();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<AccountGroup> getGroup(final UID<AccountGroup> groupUID) {
		Assertion.check().isNotNull(groupUID);
		//-----
		try (final Jedis jedis = redisConnector.getClient()) {
			final String key = HGROUP_START_KEY + groupUID.getId();
			if (jedis.exists(key)) {
				return Optional.of(map2Group(jedis.hgetAll(key)));
			}
			return Optional.empty();
		}
	}

	/*
	@Override
	public Collection<AccountGroup> getAllGroups() {
		final List<Response<Map<String, String>>> responses = new ArrayList<>();
		try (final Jedis jedis = redisConnector.getResource()) {
			final Set<String> ids = jedis.smembers(SACCOUNTS_KEY);
			try (final Transaction tx = jedis.multi()) {
				for (final String id : ids) {
					responses.add(tx.hgetAll(HACCOUNT_START_KEY + id));
				}
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrap(ex);
			}
	
		}
		//----- we are using tx to avoid roundtrips
		final List<AccountGroup> groups = new ArrayList<>();
		for (final Response<Map<String, String>> response : responses) {
			final Map<String, String> data = response.get();
			if (!data.isEmpty()) {
				groups.add(map2Group(data));
			}
		}
		return groups;
	}*/

	/** {@inheritDoc} */
	@Override
	public void attach(final Set<UID<Account>> accountsUID, final UID<AccountGroup> groupUID) {
		Assertion.check()
				.isNotNull(accountsUID)
				.isNotNull(groupUID);
		//-----
		try (final Jedis jedis = redisConnector.getClient()) {
			try (final Transaction tx = jedis.multi()) {
				for (final UID<Account> accountURI : accountsUID) {
					tx.sadd(SACCOUNTS_BY_GROUP_START_KEY + groupUID.getId(), accountURI.getId().toString());
					tx.sadd(SGROUPS_BY_ACCOUNT_START_KEY + accountURI.getId(), groupUID.getId().toString());
				}
				tx.exec();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void attach(final UID<Account> accountUID, final Set<UID<AccountGroup>> groupUIDs) {
		Assertion.check()
				.isNotNull(accountUID)
				.isNotNull(groupUIDs);
		//-----
		try (final Jedis jedis = redisConnector.getClient()) {
			try (final Transaction tx = jedis.multi()) {
				for (final UID<AccountGroup> groupURI : groupUIDs) {
					tx.sadd(SACCOUNTS_BY_GROUP_START_KEY + groupURI.getId(), accountUID.getId().toString());
					tx.sadd(SGROUPS_BY_ACCOUNT_START_KEY + accountUID.getId(), groupURI.getId().toString());
				}
				tx.exec();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<Account>> getAccountUIDs(final UID<AccountGroup> groupUID) {
		Assertion.check().isNotNull(groupUID);
		//-----
		final Set<UID<Account>> set = new HashSet<>();
		try (final Jedis jedis = redisConnector.getClient()) {
			final Set<String> ids = jedis.smembers(SACCOUNTS_BY_GROUP_START_KEY + groupUID.getId());
			for (final String id : ids) {
				set.add(UID.of(Account.class, id));
			}
			return set;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<AccountGroup>> getGroupUIDs(final UID<Account> accountUID) {
		Assertion.check().isNotNull(accountUID);
		//-----
		final Set<UID<AccountGroup>> set = new HashSet<>();
		try (final Jedis jedis = redisConnector.getClient()) {
			final Set<String> ids = jedis.smembers(SGROUPS_BY_ACCOUNT_START_KEY + accountUID.getId());
			for (final String id : ids) {
				set.add(UID.of(AccountGroup.class, id));
			}
			return set;
		}
	}

	private static Map<String, String> account2Map(final Account account) {
		return new MapBuilder<String, String>()
				.put("id", account.getId())
				.put("authToken", account.getAuthToken())
				.put("displayName", account.getDisplayName())
				.putNullable("email", account.getEmail())
				.build();
	}

	private static Account map2Account(final Map<String, String> data) {
		return Account.builder(data.get("id"))
				.withAuthToken(data.get("authToken"))
				.withDisplayName(data.get("displayName"))
				.withEmail(data.get("email"))
				.build();
	}

	private static Map<String, String> group2Map(final AccountGroup group) {
		return new MapBuilder<String, String>()
				.put("id", group.getId())
				.put("displayName", group.getDisplayName())
				.build();
	}

	private static AccountGroup map2Group(final Map<String, String> data) {
		return new AccountGroup(data.get("id"), data.get("displayName"));
	}

	/** {@inheritDoc} */
	@Override
	public void setPhoto(final UID<Account> accountUID, final VFile photo) {
		Assertion.check()
				.isNotNull(accountUID)
				.isNotNull(photo);
		//-----
		final Map<String, String> vFileMapPhoto = photoCodec.vFile2Map(photo);
		try (final Jedis jedis = redisConnector.getClient()) {
			try (final Transaction tx = jedis.multi()) {
				tx.hmset(HPHOTO_BY_ACCOUNT_START_KEY + accountUID.getId(), vFileMapPhoto);
				tx.exec();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountUID) {
		final Map<String, String> result;
		try (final Jedis jedis = redisConnector.getClient()) {
			result = jedis.hgetAll(HPHOTO_BY_ACCOUNT_START_KEY + accountUID.getId());
		}
		if (result.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(photoCodec.map2vFile(result));
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		try (final Jedis jedis = redisConnector.getClient()) {
			try (final Transaction tx = jedis.multi()) {
				//todo : les haccount, photos et accountsByGroup", "photoByAccount ne sont pas supprimées
				tx.del(SACCOUNTS_KEY, SGROUPS_KEY, "accountsByGroup", "photoByAccount", HAUTHTOKEN_INDEX_KEY);
				tx.exec();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		try (final Jedis jedis = redisConnector.getClient()) {
			final String accountUri = jedis.hget(HAUTHTOKEN_INDEX_KEY, userAuthToken);
			if (accountUri != null) {
				return Optional.of(map2Account(jedis.hgetAll(HACCOUNT_START_KEY + accountUri)));
			}
			return Optional.empty();
		}
	}
}
