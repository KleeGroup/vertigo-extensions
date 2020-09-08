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
package io.vertigo.datastore.impl.entitystore;

import java.util.Comparator;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.datamodel.structure.definitions.DataAccessor;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.DtField.FieldType;
import io.vertigo.datamodel.structure.model.DtListURIForMasterData;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.model.Entity;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.datastore.entitystore.EntityStoreManager;

/**
 * Comparateur des DtObject.
 * S'appuie sur SortState.
 * Si la colonne est un type primitif alors on effectue le tri sur ce type.
 * Si la colonne est une UID on délégue à l'UID.
 * @param <D> Type de l'objet
 *
 * @author pchretien
 */
public final class DtObjectComparator<D extends DtObject> implements Comparator<D> {

	//On ne veut pas d'un comparateur sérializable !!!
	/**
	 * Comparateur du tri
	 */
	private final Comparator<Object> comparator;

	/**
	 * Field du tri
	 */
	private final DtField sortField;

	/**
	 * Constructor.
	 * @param entityStoreManager Manager de persistence
	 * @param sortField the sort field
	 * @param sortDesc sort order
	 */
	public DtObjectComparator(final EntityStoreManager entityStoreManager, final DtField sortField, final boolean sortDesc) {
		Assertion.check().isNotNull(sortField);
		//-----
		this.sortField = sortField;
		//On recherche le comparateur associé au champ de la collection
		//Si il n'y a pas de comparateur alors on applique la comparaison standard.
		//On regarde si on est sur une ForeignKey et sur une MasterDataList
		if (sortField.getType() == FieldType.FOREIGN_KEY && entityStoreManager.getMasterDataConfig().containsMasterData(sortField.getFkDtDefinition())) {
			//Il existe une Liste de référence associée
			//Dans le cas des liste de référence on délégue la comparaison
			final DtListURIForMasterData mdlUri = entityStoreManager.getMasterDataConfig().getDtListURIForMasterData(sortField.getFkDtDefinition());
			this.comparator = createMasterDataComparator(sortDesc, entityStoreManager, mdlUri);
		} else {
			//Cas par défaut
			this.comparator = (v1, v2) -> DtObjectUtil.compareFieldValues(v1, v2, sortDesc);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final D dto1, final D dto2) {
		Assertion.check()
				.isNotNull(dto1)
				.isNotNull(dto2);
		//Les DTC ne contiennent pas d'éléments null.
		//-----
		final DataAccessor dataAccessor = sortField.getDataAccessor();
		return comparator.compare(dataAccessor.getValue(dto1), dataAccessor.getValue(dto2));
	}

	/**
	 * Fournit le comparateur à utiliser pour trier une colonne référenéant une MasterDataList.
	 * @return Comparator à utiliser pour trier la colonne.
	 */
	private static Comparator<Object> createMasterDataComparator(final boolean sortDesc, final EntityStoreManager entityStoreManager, final DtListURIForMasterData dtcURIForMasterData) {
		Assertion.check()
				.isNotNull(entityStoreManager)
				.isNotNull(dtcURIForMasterData);
		//-----
		final DtField mdFieldSort = dtcURIForMasterData.getDtDefinition().getSortField().get();
		return new MasterDataComparator(dtcURIForMasterData, sortDesc, entityStoreManager, mdFieldSort);
	}

	private static final class MasterDataComparator implements Comparator<Object> {
		private final DtListURIForMasterData dtcURIForMasterData;
		private final boolean sortDesc;
		private final EntityStoreManager dataStore;
		private final DtField mdFieldSort;

		MasterDataComparator(final DtListURIForMasterData dtcURIForMasterData, final boolean sortDesc, final EntityStoreManager dataStore, final DtField mdFieldSort) {
			this.dtcURIForMasterData = dtcURIForMasterData;
			this.sortDesc = sortDesc;
			this.dataStore = dataStore;
			this.mdFieldSort = mdFieldSort;
		}

		private Object getSortValue(final Object o) {
			final UID<Entity> uid = UID.of(dtcURIForMasterData.getDtDefinition(), o);
			DtObject dto;
			try {
				dto = dataStore.readOne(uid);
			} catch (final Exception e) {
				//Il ne peut pas y avoir d'exception typée dans un comparateur.
				throw WrappedException.wrap(e);
			}
			return mdFieldSort.getDataAccessor().getValue(dto);
		}

		@Override
		public int compare(final Object o1, final Object o2) {
			if (o1 != null && o2 != null) {
				final Object lib1 = getSortValue(o1);
				final Object lib2 = getSortValue(o2);
				return DtObjectUtil.compareFieldValues(lib1, lib2, sortDesc);
			}
			return DtObjectUtil.compareFieldValues(o1, o2, sortDesc); //si l'un des deux est null on retombe sur la comparaison standard
		}
	}

}
