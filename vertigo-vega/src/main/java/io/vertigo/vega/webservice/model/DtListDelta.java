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
package io.vertigo.vega.webservice.model;

import java.io.Serializable;

import io.vertigo.core.lang.Assertion;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.structure.model.DtObject;

/**
 * Delta operations on List.
 * @author npiedeloup (16 sept. 2014 18:13:55)
 * @param <D> Object type
 */
public final class DtListDelta<D extends DtObject> implements Serializable {
	private static final long serialVersionUID = -5002177631553042497L;

	private final DtList<D> dtListCreates;
	private final DtList<D> dtListUpdates;
	private final DtList<D> dtListDeletes;

	public DtListDelta(final DtList<D> dtListCreates, final DtList<D> dtListUpdates, final DtList<D> dtListDeletes) {
		Assertion.check()
				.isNotNull(dtListCreates)
				.isNotNull(dtListUpdates)
				.isNotNull(dtListDeletes);
		//---
		this.dtListCreates = dtListCreates;
		this.dtListUpdates = dtListUpdates;
		this.dtListDeletes = dtListDeletes;
	}

	/**
	 * @return Created objects.
	 */
	public DtList<D> getCreated() {
		return dtListCreates;
	}

	/**
	 * @return Updated objects.
	 */
	public DtList<D> getUpdated() {
		return dtListUpdates;
	}

	/**
	 * @return Deleted objects.
	 */
	public DtList<D> getDeleted() {
		return dtListDeletes;
	}
}
