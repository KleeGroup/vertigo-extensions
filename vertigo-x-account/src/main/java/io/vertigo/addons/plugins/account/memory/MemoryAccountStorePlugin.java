package io.vertigo.addons.plugins.account.memory;

import io.vertigo.addons.account.Account;
import io.vertigo.addons.account.AccountGroup;
import io.vertigo.addons.impl.account.AccountStorePlugin;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author pchretien
 */
public final class MemoryAccountStorePlugin implements AccountStorePlugin {
	private final Map<URI<Account>, Account> accountByURI = new HashMap<>();
	private final Map<URI<AccountGroup>, AccountGroup> groupByURI = new HashMap<>();
	//---
	private final Map<URI<Account>, Set<URI<AccountGroup>>> groupByAccountURI = new HashMap<>();
	private final Map<URI<AccountGroup>, Set<URI<Account>>> accountBygroupURI = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public long getNbAccounts() {
		return accountByURI.size();
	}

	/** {@inheritDoc} */
	@Override
	public long getNbGroups() {
		return groupByURI.size();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean exists(final URI<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		return accountByURI.containsKey(accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Account getAccount(final URI<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		return accountByURI.get(accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void saveAccount(final Account account) {
		Assertion.checkNotNull(account);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(account);
		final URI<Account> uri = new URI<>(dtDefinition, account.getId());
		//----
		final Object old = accountByURI.put(uri, account);
		if (old == null) {
			groupByAccountURI.put(uri, new HashSet<URI<AccountGroup>>());
		}
	}

	//-----
	/** {@inheritDoc} */
	@Override
	public synchronized AccountGroup getGroup(final URI<AccountGroup> groupURI) {
		Assertion.checkNotNull(groupURI);
		//-----
		return groupByURI.get(groupURI);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Collection<AccountGroup> getAllGroups() {
		return groupByURI.values();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void saveGroup(final AccountGroup group) {
		Assertion.checkNotNull(group);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(group);
		final URI<AccountGroup> uri = new URI<>(dtDefinition, group.getId());
		//----
		Assertion.checkArgument(!accountByURI.containsKey(uri), "this group is already registered, you can't create it");
		//-----
		accountBygroupURI.put(uri, new HashSet<URI<Account>>());
		groupByURI.put(uri, group);
	}

	//-----
	/** {@inheritDoc} */
	@Override
	public synchronized void attach(final URI<Account> accountURI, final URI<AccountGroup> groupURI) {
		Assertion.checkNotNull(accountURI);
		Assertion.checkNotNull(groupURI);
		//-----
		final Set<URI<AccountGroup>> groupURIs = groupByAccountURI.get(accountURI);
		Assertion.checkNotNull(groupURIs, "account must be create before this operation");
		groupURIs.add(groupURI);
		//-----
		final Set<URI<Account>> accountURIs = accountBygroupURI.get(groupURI);
		Assertion.checkNotNull(accountURIs, "group must be create before this operation");
		accountURIs.add(accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void detach(final URI<Account> accountURI, final URI<AccountGroup> groupURI) {
		Assertion.checkNotNull(accountURI);
		Assertion.checkNotNull(groupURI);
		//-----
		final Set<URI<AccountGroup>> groupURIs = groupByAccountURI.get(accountURI);
		Assertion.checkNotNull(groupURIs, "account does not long exist");
		groupURIs.remove(groupURI);

		//-----
		final Set<URI<Account>> accountURIs = accountBygroupURI.get(groupURI);
		Assertion.checkNotNull(accountURIs, "group does not long exist");
		accountURIs.remove(accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<URI<AccountGroup>> getGroupURIs(final URI<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		final Set<URI<AccountGroup>> groupURIs = groupByAccountURI.get(accountURI);
		Assertion.checkNotNull(accountURI, "account {0} must be create before this operation", accountURI);
		return Collections.unmodifiableSet(groupURIs);
		//
		//		Assertion.checkNotNull(groupURIs, "account must be create before this operation");
		//		List<AccountGroup> groups = new ArrayList<>();
		//		for (URI<AccountGroup> groupURI : groupURIs) {
		//			groups.add(groupByURI.get(groupURI));
		//		}
		//		return Collections.unmodifiableList(groups);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<URI<Account>> getAccountURIs(final URI<AccountGroup> groupURI) {
		Assertion.checkNotNull(groupURI);
		//-----
		final Set<URI<Account>> accountURIs = accountBygroupURI.get(groupURI);
		Assertion.checkNotNull(accountURIs, "group {0} must be create before this operation", groupURI);
		return Collections.unmodifiableSet(accountURIs);
	}

}
