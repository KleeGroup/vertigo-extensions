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
package io.vertigo.account.impl.authorization;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.vertigo.account.authorization.AuthorizationManager;
import io.vertigo.account.authorization.UserAuthorizations;
import io.vertigo.account.authorization.definitions.Authorization;
import io.vertigo.account.authorization.definitions.AuthorizationName;
import io.vertigo.account.authorization.definitions.OperationName;
import io.vertigo.account.authorization.definitions.SecuredEntity;
import io.vertigo.account.authorization.definitions.rulemodel.RuleMultiExpression;
import io.vertigo.account.impl.authorization.dsl.translator.CriteriaSecurityRuleTranslator;
import io.vertigo.account.impl.authorization.dsl.translator.SearchSecurityRuleTranslator;
import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.datamodel.criteria.Criteria;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * Main authorizations manager implementation.
 * @author npiedeloup
 */
public final class AuthorizationManagerImpl implements AuthorizationManager {
	private static final String USER_SESSION_ACL_KEY = "vertigo.account.authorizations";

	private final VSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param securityManager Security manager
	 */
	@Inject
	public AuthorizationManagerImpl(final VSecurityManager securityManager) {
		Assertion.check().isNotNull(securityManager);
		//-----
		this.securityManager = securityManager;
	}

	/** {@inheritDoc} */
	@Override
	public UserAuthorizations obtainUserAuthorizations() {
		return getUserAuthorizationsOpt().orElseThrow(() -> new IllegalArgumentException("Can't getUserAuthorizations, check your have create an UserSession before."));

	}

	private Optional<UserAuthorizations> getUserAuthorizationsOpt() {
		final Optional<UserSession> userSessionOpt = securityManager.getCurrentUserSession();
		if (userSessionOpt.isEmpty()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return Optional.empty();
		}
		UserAuthorizations userAuthorizations = userSessionOpt.get().getAttribute(USER_SESSION_ACL_KEY);
		if (userAuthorizations == null) {
			userAuthorizations = new UserAuthorizations();
			userSessionOpt.get().putAttribute(USER_SESSION_ACL_KEY, userAuthorizations);
		}
		return Optional.of(userAuthorizations);

	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAuthorization(final AuthorizationName... permissionNames) {
		Assertion.check().isNotNull(permissionNames);
		//---
		return getUserAuthorizationsOpt()
				.map(userPermissions -> userPermissions.hasAuthorization(permissionNames))
				// Si il n'y a pas de userPermissions alors pas d'autorisation.
				.orElse(false);

	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> boolean isAuthorized(final K keyConcept, final OperationName<K> operationName) {
		Assertion.check().isNotNull(keyConcept)
				.isNotNull(operationName);
		//---
		return getAuthorizedOperations(keyConcept).contains(operationName.name());
	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> Criteria<K> getCriteriaSecurity(final Class<K> keyConceptClass, final OperationName<K> operation) {
		Assertion.check().isNotNull(keyConceptClass)
				.isNotNull(operation);
		//---
		final Optional<UserAuthorizations> userPermissionsOpt = getUserAuthorizationsOpt();
		if (userPermissionsOpt.isEmpty()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return Criterions.alwaysFalse();
		}

		final UserAuthorizations userPermissions = userPermissionsOpt.get();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConceptClass);
		final SecuredEntity securedEntity = findSecuredEntity(dtDefinition);

		final List<Criteria<K>> criterions = userPermissions.getEntityAuthorizations(dtDefinition).stream()
				.filter(permission -> permission.getOperation().get().equals(operation.name())
						|| permission.getOverrides().contains(operation.name()))
				.flatMap(permission -> permission.getRules().stream())
				.map(rule -> new CriteriaSecurityRuleTranslator<K>()
						.on(securedEntity)
						.withRule(rule)
						.withCriteria(userPermissions.getSecurityKeys())
						.toCriteria())
				.collect(Collectors.toList());

		if (criterions.isEmpty()) {
			// Si il n'y a pas de droits alors pas d'autorisation.
			return Criterions.alwaysFalse();
		}

		Criteria<K> securityCriteria = null;
		for (final Criteria<K> ruleCriteria : criterions) {
			if (securityCriteria == null) {
				securityCriteria = ruleCriteria;
			} else {
				securityCriteria = securityCriteria.or(ruleCriteria);
			}
		}
		return securityCriteria;
	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> String getSearchSecurity(final Class<K> keyConceptClass, final OperationName<K> operationName) {
		Assertion.check()
				.isNotNull(keyConceptClass)
				.isNotNull(operationName);
		//---
		final Optional<UserAuthorizations> userPermissionsOpt = getUserAuthorizationsOpt();
		if (userPermissionsOpt.isEmpty()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return ""; //Attention : pas de *:*
		}

		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConceptClass);
		final SecuredEntity securedEntity = findSecuredEntity(dtDefinition);

		final UserAuthorizations userPermissions = userPermissionsOpt.get();
		final SearchSecurityRuleTranslator securityRuleTranslator = new SearchSecurityRuleTranslator()
				.on(securedEntity)
				.withCriteria(userPermissions.getSecurityKeys());

		final List<Authorization> permissions = userPermissions.getEntityAuthorizations(dtDefinition).stream()
				.filter(permission -> permission.getOperation().get().equals(operationName.name()))
				.collect(Collectors.toList());
		for (final Authorization permission : permissions) {
			for (final RuleMultiExpression ruleExpression : permission.getRules()) {
				securityRuleTranslator.withRule(ruleExpression);
			}
		}
		return securityRuleTranslator.toSearchQuery();
	}

	@Override
	public Set<String> getPriorAuthorizations() {
		return getUserAuthorizationsOpt()
				.map(UserAuthorizations::getPriorAuthorizationNames)
				// Si il n'y a pas de userPermissions alors pas d'autorisation.
				.orElseGet(Collections::emptySet);
	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> List<String> getAuthorizedOperations(final K keyConcept) {
		Assertion.check().isNotNull(keyConcept);
		//---
		final Optional<UserAuthorizations> userPermissionsOpt = getUserAuthorizationsOpt();
		if (userPermissionsOpt.isEmpty()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return Collections.emptyList();
		}
		final UserAuthorizations userPermissions = userPermissionsOpt.get();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConcept);
		final SecuredEntity securedEntity = findSecuredEntity(dtDefinition);

		return userPermissions.getEntityAuthorizations(dtDefinition).stream()
				.filter(permission -> permission.getRules().stream()
						.anyMatch(rule -> new CriteriaSecurityRuleTranslator<K>()
								.on(securedEntity)
								.withRule(rule)
								.withCriteria(userPermissions.getSecurityKeys())
								.toCriteria()
								.toPredicate().test(keyConcept)))
				.flatMap(authorization -> Stream.concat(Stream.of(authorization.getOperation().get()), authorization.getOverrides().stream()))
				.collect(Collectors.toList());
	}

	/**
	 * Finds the SecuredEntity from a type of 'dtDefinition'
	 * @param dtDefinition the 'dtDefinition'
	 * @return SecuredEntity
	 */
	public static SecuredEntity findSecuredEntity(final DtDefinition dtDefinition) {
		Assertion.check().isNotNull(dtDefinition);
		//---
		final String name = SecuredEntity.PREFIX + dtDefinition.getName();
		return Node.getNode().getDefinitionSpace().resolve(name, SecuredEntity.class);
	}

}
