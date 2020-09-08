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
package io.vertigo.datastore.filestore.definitions;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.datastore.filestore.model.FileInfo;

/**
 * Définition d'un FileInfo.
 *
 * La définition n'est pas serializable.
 * Elle doit être invariante (non mutable) dans le temps.
 * Par défaut elle est chargée au (re)démarrage du serveur.
 *
 * @author  npiedeloup, pchretien
 */
@DefinitionPrefix(FileInfoDefinition.PREFIX)
public final class FileInfoDefinition extends AbstractDefinition {
	public static final String PREFIX = "Fi";
	/**
	 * StoreName des fichiers de ce type.
	 */
	private final String storeName;

	/**
	 * Constructor.
	 * @param name Nom de la définition
	 * @param storeName Nom du store de ces fichiers
	 */
	public FileInfoDefinition(final String name, final String storeName) {
		super(name);
		//---
		Assertion.check().isNotBlank(storeName);
		//---
		this.storeName = storeName;
	}

	/**
	 * @return Store d'accès aux FI.
	 */
	public String getStoreName() {
		return storeName;
	}

	//=========================================================================
	//===========================STATIC========================================
	//=========================================================================
	/**
	 * @param fileInfoClass fileInfo Class
	 * @return FileInfoDefinition from class
	 */
	public static FileInfoDefinition findFileInfoDefinition(final Class<? extends FileInfo> fileInfoClass) {
		Assertion.check().isNotNull(fileInfoClass);
		//---
		final String name = FileInfoDefinition.PREFIX + fileInfoClass.getSimpleName();
		return Node.getNode().getDefinitionSpace().resolve(name, FileInfoDefinition.class);
	}
}
