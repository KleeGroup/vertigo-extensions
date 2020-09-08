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
package io.vertigo.datamodel.structure.model;

import java.io.Serializable;
import java.util.stream.Stream;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.definition.DefinitionReference;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.association.AssociationDefinition;
import io.vertigo.datamodel.structure.definitions.association.AssociationNNDefinition;
import io.vertigo.datamodel.structure.definitions.association.AssociationSimpleDefinition;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForAssociation;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForNNAssociation;
import io.vertigo.datamodel.structure.definitions.association.DtListURIForSimpleAssociation;

/**
 * This class is a way to access to a list of entities managed by a relationship.
 * It's a kind of box (aka optional) that offers a small list of methods.
 *
 * @author mlaroche
 *
 * @param <E> the type of the targetedEntity
 */
public class ListVAccessor<E extends Entity> implements Serializable {
	private static final long serialVersionUID = 1L;

	private enum State {
		LOADED, NOT_LOADED
	}

	private State status = State.NOT_LOADED;
	private final Entity entity;
	private final DefinitionReference<AssociationDefinition> associationDefinitionReference;
	private final String roleName;
	private DtList<E> value;
	private final DefinitionReference<DtDefinition> targetDefinitionReference;

	/**
	 * Constructor.
	 * @param entity the entity
	 * @param roleName the role of the association (case of multiple associations with the same entity)
	 */
	public ListVAccessor(final Entity entity, final String associationDefinitionName, final String roleName) {
		Assertion.check()
				.isNotNull(entity)
				.isNotBlank(associationDefinitionName)
				.isNotBlank(roleName);
		//---
		this.entity = entity;
		this.roleName = roleName;
		//---
		final AssociationDefinition associationDefinition = Node.getNode().getDefinitionSpace().resolve(associationDefinitionName, AssociationDefinition.class);
		this.associationDefinitionReference = new DefinitionReference<>(associationDefinition);
		final DtDefinition targetDefinition = Stream.of(associationDefinition.getAssociationNodeA(), associationDefinition.getAssociationNodeB())
				.filter(associationNode -> roleName.equals(associationNode.getRole()))
				.findFirst()
				.orElseThrow(() -> new VSystemException("Unable to find association node with role '{1}' on association '{0}'", associationDefinitionName, roleName))
				.getDtDefinition();
		//---
		targetDefinitionReference = new DefinitionReference<>(targetDefinition);
	}

	/**
	 * @return the entity uri
	 */
	public final <A extends DtListURIForAssociation> A getDtListURI() {
		final AssociationDefinition associationDefinition = associationDefinitionReference.get();
		if (associationDefinition instanceof AssociationSimpleDefinition) {
			return (A) new DtListURIForSimpleAssociation((AssociationSimpleDefinition) associationDefinition, entity.getUID(), roleName);
		} else if (associationDefinition instanceof AssociationNNDefinition) {
			return (A) new DtListURIForNNAssociation((AssociationNNDefinition) associationDefinition, entity.getUID(), roleName);
		}
		throw new VSystemException("Unhandled type of association. Only Simple and NN Associations are supported");
	}

	/**
	 * Loads the value if needed.
	 */
	public final DtList<E> get() {
		Assertion.check().isTrue(status == State.LOADED, "Accessor is not loaded, you must load it before calling get method");
		//--
		return value;
	}

	public void set(final DtList<E> dtList) {
		Assertion.check().isNotNull(dtList);
		//---
		value = dtList;
		//--
		status = State.LOADED;
	}

	/**
	 * Loads the value if needed.
	 */
	public final void reset() {
		status = State.NOT_LOADED;
		value = null;
	}

	/**
	 * @return if entity is already loaded
	 */
	public final boolean isLoaded() {
		return status == State.LOADED;
	}

	/**
	 * @return Role of this relation
	 */
	public final String getRole() {
		return roleName;
	}

	protected UID<E> getSourceUID() {
		return entity.getUID();
	}

	protected DefinitionReference<DtDefinition> getTargetDefinitionReference() {
		return targetDefinitionReference;
	}

}
