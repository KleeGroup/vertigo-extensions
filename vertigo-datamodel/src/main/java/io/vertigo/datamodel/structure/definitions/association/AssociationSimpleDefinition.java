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
package io.vertigo.datamodel.structure.definitions.association;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.util.AssociationUtil;

/**
 * This class defines a simple association : 1-1 or 1-n.
 * A simple association
 *
 * @author  jcassignol, pchretien
 */
@DefinitionPrefix("A")
public final class AssociationSimpleDefinition extends AssociationDefinition {
	private final AssociationNode foreignAssociationNode;
	private final AssociationNode primaryAssociationNode;
	private final String fkFieldName;

	/**
	 * Constructor.
	
	 * @param name the name of the association
	 * @param fkFieldName the fieldname that represents the foreign key
	 * @param associationNodeA the A node for this assocation
	 * @param associationNodeB the B node for this assocation
	 */
	public AssociationSimpleDefinition(
			final String name,
			final String fkFieldName,
			final AssociationNode associationNodeA,
			final AssociationNode associationNodeB) {
		super(name, associationNodeA, associationNodeB);
		Assertion.check()
				.isNotNull(fkFieldName)
				//We check that this assocation is not multiple
				.isFalse(associationNodeA.isMultiple() && associationNodeB.isMultiple(), "assocation : {0}. n-n assocation is prohibited in a simple assocation", name)
				.isNotNull(fkFieldName)
				.isTrue(StringUtil.isLowerCamelCase(fkFieldName), "the name of the field {0} must be in lowerCamelCase", fkFieldName);
		//-----
		// Which node is the key node (the primary key)
		final boolean isAPrimaryNode = AssociationUtil.isAPrimaryNode(
				associationNodeA.isMultiple(), associationNodeA.isNotNull(),
				associationNodeB.isMultiple(), associationNodeB.isNotNull());
		//-----
		if (isAPrimaryNode) {
			primaryAssociationNode = getAssociationNodeA();
			foreignAssociationNode = getAssociationNodeB();
		} else {
			primaryAssociationNode = getAssociationNodeB();
			foreignAssociationNode = getAssociationNodeA();
		}
		this.fkFieldName = fkFieldName;
		//-----
		// no one can make an association with you if you're not identified by a key (for now, before refac, isPersistent is the way to make the test isPersistent() <=> isEntity() )
		Assertion.check().isTrue(primaryAssociationNode.getDtDefinition().getStereotype().isPersistent(), "assocation : {0}. The primary associationNode must be an entity ", name);
	}

	/**
	 * @return the key node
	 */
	public AssociationNode getPrimaryAssociationNode() {
		return primaryAssociationNode;
	}

	/**
	 * @return the linked node
	 */
	public AssociationNode getForeignAssociationNode() {
		return foreignAssociationNode;
	}

	/**
	 * @return the field that supports the link
	 */
	public DtField getFKField() {
		return foreignAssociationNode.getDtDefinition().getField(fkFieldName);
	}
}
