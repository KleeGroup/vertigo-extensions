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
package io.vertigo.account.plugins.account.store;

import io.vertigo.account.account.Account;
import io.vertigo.account.impl.account.AccountMapperHelper;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.Entity;

/**
 * @author pchretien
 */
public abstract class AbstractAccountStorePlugin implements Activeable {

	private DtDefinition userDtDefinition;
	private final String userDtDefinitionName;
	private final String userToAccountMappingStr;

	private AccountMapperHelper<DtField, AccountProperty> mapperHelper; //AccountAttribute from UserAttribute

	private enum AccountProperty {
		id, displayName, email, authToken, photo
	}

	/**
	 * @param userDtDefinitionName User dtDefinition name
	 * @param userToAccountMappingStr User to account conversion mapping
	 */
	protected AbstractAccountStorePlugin(
			@ParamValue("userDtDefinitionName") final String userDtDefinitionName,
			@ParamValue("userToAccountMapping") final String userToAccountMappingStr) {
		Assertion.check()
				.isNotBlank(userDtDefinitionName)
				.isNotBlank(userToAccountMappingStr);
		//-----
		this.userDtDefinitionName = userDtDefinitionName;
		this.userToAccountMappingStr = userToAccountMappingStr;
	}

	/** {@inheritDoc} */
	@Override
	public final void start() {
		userDtDefinition = Node.getNode().getDefinitionSpace().resolve(userDtDefinitionName, DtDefinition.class);
		mapperHelper = new AccountMapperHelper(userDtDefinition, AccountProperty.class, userToAccountMappingStr)
				.withMandatoryDestField(AccountProperty.id)
				.withMandatoryDestField(AccountProperty.displayName)
				.withMandatoryDestField(AccountProperty.authToken)
				.parseAttributeMapping();

		postStart();
	}

	protected void postStart() {
		//may be extended
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//rien
	}

	protected final DtDefinition getUserDtDefinition() {
		return userDtDefinition;
	}

	protected Account userToAccount(final Entity userEntity) {
		final String accountId = parseAttribute(AccountProperty.id, userEntity);
		final String authToken = parseAttribute(AccountProperty.authToken, userEntity);
		final String displayName = parseAttribute(AccountProperty.displayName, userEntity);
		final String email = parseOptionalAttribute(AccountProperty.email, userEntity);
		final String photo = parseOptionalAttribute(AccountProperty.photo, userEntity);
		return Account.builder(accountId)
				.withAuthToken(authToken)
				.withDisplayName(displayName)
				.withEmail(email)
				.withPhoto(photo)
				.build();
	}

	private String parseAttribute(final AccountProperty accountProperty, final Entity userEntity) {
		final DtField attributeField = mapperHelper.getSourceAttribute(accountProperty);
		final Object value = attributeField.getDataAccessor().getValue(userEntity);
		return value != null ? String.valueOf(value) : null;
	}

	private String parseOptionalAttribute(final AccountProperty accountProperty, final Entity userEntity) {
		final DtField attributeField = mapperHelper.getSourceAttribute(accountProperty);
		if (attributeField != null) {
			final Object value = attributeField.getDataAccessor().getValue(userEntity);
			return value != null ? String.valueOf(value) : null;
		}
		return null;
	}
}
