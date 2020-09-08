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
package io.vertigo.datastore.impl.filestore;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.datastore.filestore.model.FileInfo;
import io.vertigo.datastore.filestore.model.FileInfoURI;

/**
 * File store plugin.
 *
 * @author  npiedeloup
 */
public interface FileStorePlugin extends Plugin {

	/**
	 * @return Store name
	 */
	String getName();

	/**
	 * Load a file by its URI.
	 *
	 * @param uri FileURI requested
	 * @return FileInfo for this uri (null if not found).
	 */
	FileInfo read(FileInfoURI uri);

	//==========================================================================
	//=============================== Write operations =========================
	//==========================================================================
	/**
	 * Save a file.
	 * Input FileInfo must have an empty URI : insert mode
	 *
	 * @param fileInfo File to save (creation)
	 * @return the created FileInfo
	 */
	FileInfo create(FileInfo fileInfo);

	/**
	 * Save a file.
	 * Input FileInfo must have an URI : update mode
	 *
	 * @param fileInfo File to save  (modification)
	 */
	void update(FileInfo fileInfo);

	/**
	 * Delete a file.
	 * @param uri File's URI to remove
	 */
	void delete(FileInfoURI uri);

	/**
	 * Get's the FileInfo Class that are handled by the particular plugin
	 * @return
	 */
	Class<? extends FileInfo> getFileInfoClass();

}
