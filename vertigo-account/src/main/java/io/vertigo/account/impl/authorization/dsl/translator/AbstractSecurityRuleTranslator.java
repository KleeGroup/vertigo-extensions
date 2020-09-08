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
package io.vertigo.account.impl.authorization.dsl.translator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.vertigo.account.authorization.definitions.SecuredEntity;
import io.vertigo.account.authorization.definitions.SecurityDimension;
import io.vertigo.account.authorization.definitions.rulemodel.RuleMultiExpression;
import io.vertigo.account.impl.authorization.dsl.rules.DslParserUtil;
import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.util.StringUtil;

abstract class AbstractSecurityRuleTranslator<S extends AbstractSecurityRuleTranslator<S>> {
	private static final Pattern BEGIN_LINE_TRIM_PATTERN = Pattern.compile("^\\s+");
	private static final Pattern END_LINE_TRIM_PATTERN = Pattern.compile("\\s+$");
	private static final Pattern MULTIPLE_WHITESPACE_PATTERN = Pattern.compile("\\s+");
	protected static final Pattern EMPTY_QUERY_PATTERN = Pattern.compile("^\\(\\)$");

	private SecuredEntity mySecuredEntity;
	private final List<RuleMultiExpression> myMultiExpressions = new ArrayList<>();
	private Map<String, List<Serializable>> myUserCriteria;

	/**
	 * Specifies the protected entity we are working on
	 * @param securedEntity the entity
	 * @return this builder
	 */
	public S on(final SecuredEntity securedEntity) {
		Assertion.check().isNotNull(securedEntity);
		//-----
		this.mySecuredEntity = securedEntity;
		return (S) this;
	}

	/**
	 * Set security pattern.
	 * @param securityMultiExpression security parsed expression
	 * @return this builder
	 */
	public final S withRule(final RuleMultiExpression securityMultiExpression) {
		Assertion.check().isNotNull(securityMultiExpression);
		//-----
		myMultiExpressions.add(securityMultiExpression);
		return (S) this;
	}

	/**
	 * Set security pattern.
	 * @param securityRule security Pattern (not null, could be empty)
	 * @return this builder
	 */
	public final S withRule(final String securityRule) {
		Assertion.check().isNotNull(securityRule);
		//-----
		try {
			final RuleMultiExpression myMultiExpression = DslParserUtil.parseMultiExpression(securityRule);
			myMultiExpressions.add(myMultiExpression);
		} catch (final PegNoMatchFoundException e) {
			final String message = StringUtil.format("Echec de lecture de la securityRule {0}\n{1}", securityRule, e.getFullMessage());
			throw WrappedException.wrap(e, message);
		} catch (final Exception e) {
			final String message = StringUtil.format("Echec de lecture de la securityRule {0}\n{1}", securityRule, e.getMessage());
			throw WrappedException.wrap(e, message);
		}
		return (S) this;
	}

	/**
	 * Set criteria.
	 * @param userCriteria Criteria
	 * @return this builder
	 */
	public final S withCriteria(final Map<String, List<Serializable>> userCriteria) {
		Assertion.check()
				.isNotNull(userCriteria)
				.isNull(myUserCriteria, "criteria was already set : {0}", myUserCriteria);
		//-----
		myUserCriteria = userCriteria;
		return (S) this;
	}

	protected final boolean isSimpleSecurityField(final String fieldName) {
		if (mySecuredEntity == null) {
			return true; //if no SecuredEntity (ie juste a translator in test), we use only simple field query
		}
		return mySecuredEntity.getSecurityFields().stream()
				.anyMatch(field -> fieldName.equals(field.getName()));
	}

	protected final SecurityDimension getSecurityDimension(final String fieldName) {
		Assertion.check().isNotNull(mySecuredEntity, "Can't use SecurityDimension when no SecuredEntity definition was set");
		//-----
		return mySecuredEntity.getSecurityDimensions().stream()
				.filter(securityDimension -> fieldName.equals(securityDimension.getName()))
				.findFirst()//findFirst car pas le moment de vérifier qu'il y en qu'un seul
				.get();
	}

	protected final List<RuleMultiExpression> getMultiExpressions() {
		Assertion.check().isNotNull(myMultiExpressions, "MultiExpressions was not set");
		//----
		return myMultiExpressions;
	}

	protected final List<Serializable> getUserCriteria(final String userProperty) {
		Assertion.check().isNotNull(myUserCriteria, "UserCriteria was not set");
		//----
		return myUserCriteria.getOrDefault(userProperty, Collections.emptyList());
	}

	protected static List<Serializable> subValues(final List<String> values, final boolean includeHead, final String value, final boolean valueIncluded) {
		final int indexof = values.indexOf(value);
		Assertion.check().isTrue(indexof >= 0, "Current value ({0}) of security axe {1} not found in authorized values", value);
		//----
		final int offsetEdge = valueIncluded == includeHead ? 1 : 0; //add +1 to indexOf if valueInclude XOR includeHead
		final List<? extends Serializable> subValues = includeHead ? values.subList(0, indexof + offsetEdge) : values.subList(indexof + offsetEdge, values.size() - 1);
		return (List<Serializable>) subValues;
	}

	protected static <K> int lastIndexNotNull(final K[] value) {
		for (int i = value.length - 1; i >= 0; i--) {
			if (value[i] != null) {
				return i;
			}
		}
		return -1;
	}

	protected static String cleanQuery(final String query) {
		String queryString = BEGIN_LINE_TRIM_PATTERN.matcher(query).replaceAll("");//replace whitespaces at beginning of a line
		queryString = END_LINE_TRIM_PATTERN.matcher(queryString).replaceAll("");//replace whitespaces at end of a line
		queryString = MULTIPLE_WHITESPACE_PATTERN.matcher(queryString).replaceAll(" ");// replace multiple whitespaces by space
		return queryString;
	}
}
