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
package io.vertigo.account.plugins.account.store.datastore;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountMapperHelper;
import io.vertigo.account.impl.account.AccountStorePlugin;
import io.vertigo.account.plugins.account.store.AbstractAccountStorePlugin;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.Node;
import io.vertigo.core.param.ParamValue;
import io.vertigo.datamodel.criteria.Criteria;
import io.vertigo.datamodel.criteria.Criterions;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.FormatterException;
import io.vertigo.datamodel.structure.definitions.association.AssociationDefinition;
import io.vertigo.datamodel.structure.definitions.association.AssociationNNDefinition;
import io.vertigo.datamodel.structure.definitions.association.AssociationSimpleDefinition;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForNNAssociation;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForSimpleAssociation;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtListState;
import io.vertigo.datamodel.structure.model.DtListURI;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.datastore.filestore.FileStoreManager;
import io.vertigo.datastore.filestore.definitions.FileInfoDefinition;
import io.vertigo.datastore.filestore.model.FileInfoURI;
import io.vertigo.datastore.filestore.model.VFile;

/**
 * Source of identity.
 * @author npiedeloup
 */
public final class StoreAccountStorePlugin extends AbstractAccountStorePlugin implements AccountStorePlugin {

	private final SmartTypeManager smartTypeManager;
	private final VTransactionManager transactionManager;
	private final EntityStoreManager entityStoreManager;
	private final FileStoreManager fileStoreManager;

	private DtField userIdField;
	private final String groupIdentityEntity;
	private final String userAuthField;
	private final Optional<String> photoFileInfo;
	private Optional<FileInfoDefinition> photoFileInfoDefinition = Optional.empty();
	private DtDefinition userGroupDtDefinition;
	private DtField groupIdField;
	private final String groupToGroupAccountMappingStr;
	private AssociationDefinition associationUserGroup;
	private String associationGroupRoleName;
	private String associationUserRoleName;

	private AccountMapperHelper<DtField, GroupProperty> mapperHelper; //GroupProperty from UserGroupAttribute

	private enum GroupProperty {
		id, displayName
	}

	/**
	 * Constructor.
	 * @param userIdentityEntity Entity name of userIdentityEntity
	 * @param groupIdentityEntity Entity name of groupIdentityEntity
	 * @param userAuthField FieldName use to find user by it's authToken
	 * @param userToAccountMappingStr User to account conversion mapping
	 * @param groupToGroupAccountMappingStr User to account conversion mapping
	 * @param entityStoreManager Store Manager
	 * @param fileStoreManager File Store Manager
	 */
	@Inject
	public StoreAccountStorePlugin(
			@ParamValue("userIdentityEntity") final String userIdentityEntity,
			@ParamValue("groupIdentityEntity") final String groupIdentityEntity,
			@ParamValue("userAuthField") final String userAuthField,
			@ParamValue("photoFileInfo") final Optional<String> photoFileInfo,
			@ParamValue("userToAccountMapping") final String userToAccountMappingStr,
			@ParamValue("groupToGroupAccountMapping") final String groupToGroupAccountMappingStr,
			final SmartTypeManager smartTypeManager,
			final EntityStoreManager entityStoreManager,
			final FileStoreManager fileStoreManager,
			final VTransactionManager transactionManager) {
		super(userIdentityEntity, userToAccountMappingStr);
		Assertion.check().isNotBlank(userIdentityEntity)
				.isNotBlank(userAuthField)
				.isNotNull(smartTypeManager)
				.isNotNull(entityStoreManager)
				.isNotNull(fileStoreManager)
				.isNotBlank(groupToGroupAccountMappingStr);

		this.groupIdentityEntity = groupIdentityEntity;
		this.userAuthField = userAuthField;
		this.photoFileInfo = photoFileInfo;
		this.smartTypeManager = smartTypeManager;
		this.entityStoreManager = entityStoreManager;
		this.fileStoreManager = fileStoreManager;
		this.transactionManager = transactionManager;
		this.groupToGroupAccountMappingStr = groupToGroupAccountMappingStr;
	}

	@Override
	protected void postStart() {
		userIdField = getUserDtDefinition().getIdField().get(); //Entity with Id mandatory

		if (photoFileInfo.isPresent()) {
			photoFileInfoDefinition = Optional.of(Node.getNode().getDefinitionSpace().resolve(photoFileInfo.get(), FileInfoDefinition.class));
		}

		userGroupDtDefinition = Node.getNode().getDefinitionSpace().resolve(groupIdentityEntity, DtDefinition.class);
		groupIdField = userGroupDtDefinition.getIdField().get(); //Entity with Id mandatory
		mapperHelper = new AccountMapperHelper(userGroupDtDefinition, GroupProperty.class, groupToGroupAccountMappingStr)
				.withMandatoryDestField(GroupProperty.id)
				.withMandatoryDestField(GroupProperty.displayName)
				.parseAttributeMapping();

		for (final AssociationDefinition association : Node.getNode().getDefinitionSpace().getAll(AssociationDefinition.class)) {
			if (userGroupDtDefinition.equals(association.getAssociationNodeA().getDtDefinition())
					&& getUserDtDefinition().equals(association.getAssociationNodeB().getDtDefinition())) {
				associationUserGroup = association;
				associationUserRoleName = association.getAssociationNodeB().getRole();
				associationGroupRoleName = association.getAssociationNodeA().getRole();
				break;
			} else if (userGroupDtDefinition.equals(association.getAssociationNodeB().getDtDefinition())
					&& getUserDtDefinition().equals(association.getAssociationNodeA().getDtDefinition())) {
				associationUserGroup = association;
				associationUserRoleName = association.getAssociationNodeA().getRole();
				associationGroupRoleName = association.getAssociationNodeB().getRole();
				break;
			}
		}
		Assertion.check()
				.isNotNull(associationUserGroup, "Association between User ({0}) and Group ({1}) not found",
						getUserDtDefinition().getClassSimpleName(), userGroupDtDefinition.getClassSimpleName())
				.isTrue(associationUserGroup instanceof AssociationSimpleDefinition || associationUserGroup instanceof AssociationNNDefinition,
						"Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
	}

	/** {@inheritDoc} */
	@Override
	public Account getAccount(final UID<Account> accountURI) {
		final Entity userEntity = readUserEntity(accountURI);
		return userToAccount(userEntity);
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<AccountGroup>> getGroupUIDs(final UID<Account> accountUID) {
		if (associationUserGroup instanceof AssociationSimpleDefinition) {
			//case 1 group per user
			final Entity userEntity = readUserEntity(accountUID);
			final Object fkValue = ((AssociationSimpleDefinition) associationUserGroup).getFKField().getDataAccessor().getValue(userEntity);
			final UID<AccountGroup> groupURI = UID.of(userGroupDtDefinition, fkValue);
			return Collections.singleton(groupURI);
		}
		//case N group per user
		//other case checked in postStart by assertions
		Assertion.check().isTrue(associationUserGroup instanceof AssociationNNDefinition,
				"Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
		final DtListURI groupDtListURI = new DtListURIForNNAssociation((AssociationNNDefinition) associationUserGroup, accountUID, associationGroupRoleName);

		//-----
		final DtList<? extends Entity> result = Node.getNode().getComponentSpace().resolve(EntityStoreManager.class).findAll(groupDtListURI);
		return result.stream().map(groupEntity ->

		groupToAccount(groupEntity).getUID())
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public AccountGroup getGroup(final UID<AccountGroup> accountGroupUID) {
		final Entity groupEntity = readGroupEntity(accountGroupUID);
		return groupToAccount(groupEntity);
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<Account>> getAccountUIDs(final UID<AccountGroup> groupUID) {
		final DtListURI userDtListURI;
		if (associationUserGroup instanceof AssociationSimpleDefinition) {
			userDtListURI = new DtListURIForSimpleAssociation((AssociationSimpleDefinition) associationUserGroup, groupUID, associationUserRoleName);
		} else { //autres cas éliminés par assertion dans le postStart
			Assertion.check().isTrue(associationUserGroup instanceof AssociationNNDefinition,

					"Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
			userDtListURI = new DtListURIForNNAssociation((AssociationNNDefinition) associationUserGroup, groupUID, associationUserRoleName);
		}
		//-----
		final DtList<? extends Entity> result = Node.getNode().getComponentSpace().resolve(EntityStoreManager.class).findAll(userDtListURI);
		return result.stream()
				.map(userEntity -> userToAccount(userEntity).getUID())
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountURI) {
		final Account account = getAccount(accountURI);
		final String photoId = account.getPhoto();
		if (photoId != null && photoFileInfoDefinition.isPresent()) {
			return executeInTransaction(() -> {
				final FileInfoURI photoUri = new FileInfoURI(photoFileInfoDefinition.get(), photoId);
				return Optional.of(fileStoreManager.read(photoUri).getVFile());
			});
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {

		Serializable userAuthTokenValue;
		try {
			userAuthTokenValue = Serializable.class.cast(smartTypeManager.stringToValue(getUserDtDefinition().getField(userAuthField).getSmartTypeDefinition(), userAuthToken));
		} catch (final FormatterException e) {
			throw WrappedException.wrap(e);
		}
		final Criteria<Entity> criteriaByAuthToken = Criterions.isEqualTo(() -> userAuthField, userAuthTokenValue);
		return executeInTransaction(() -> {
			final DtList<Entity> results = entityStoreManager.find(getUserDtDefinition(), criteriaByAuthToken, DtListState.of(2));
			Assertion.check().isTrue(results.size() <= 1, "Too many matching for authToken {0}", userAuthToken);
			if (!results.isEmpty()) {
				return Optional.of(userToAccount(results.get(0)));
			}
			return Optional.empty();
		});
	}

	private AccountGroup groupToAccount(final Entity groupEntity) {
		final String groupId = parseAttribute(GroupProperty.id, groupEntity);
		final String displayName = parseAttribute(GroupProperty.displayName, groupEntity);
		return new AccountGroup(groupId, displayName);
	}

	private String parseAttribute(final GroupProperty accountProperty, final Entity userEntity) {
		final DtField attributeField = mapperHelper.getSourceAttribute(accountProperty);
		return String.valueOf(attributeField.getDataAccessor().getValue(userEntity));
	}

	private Entity readUserEntity(final UID<Account> accountURI) {
		return executeInTransaction(() -> {
			try {
				final Serializable typedId = (Serializable) smartTypeManager.stringToValue(userIdField.getSmartTypeDefinition(), (String) accountURI.getId()); //account id IS always a String
				final UID<Entity> userURI = UID.of(getUserDtDefinition(), typedId);
				return entityStoreManager.readOne(userURI);
			} catch (final FormatterException e) {
				throw WrappedException.wrap(e, "Can't get UserEntity from Store");
			}
		});
	}

	private Entity readGroupEntity(final UID<AccountGroup> accountGroupURI) {
		return executeInTransaction(() -> {
			try {
				final Serializable typedId = (Serializable) smartTypeManager.stringToValue(groupIdField.getSmartTypeDefinition(), (String) accountGroupURI.getId()); //accountGroup id IS always a String
				final UID<Entity> groupURI = UID.of(userGroupDtDefinition, typedId);
				return entityStoreManager.readOne(groupURI);
			} catch (final FormatterException e) {
				throw WrappedException.wrap(e, "Can't get UserGroupEntity from Store");
			}
		});
	}

	private <O> O executeInTransaction(final Supplier<O> supplier) {
		if (transactionManager.hasCurrentTransaction()) {
			return supplier.get();
		}
		//Dans le cas ou il n'existe pas de transaction on en crée une.
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return supplier.get();
		}
	}
}
