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
package io.vertigo.social.impl.comment;

import java.util.List;
import java.util.UUID;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.comment.Comment;

/**
 * @author pchretien
 */
public interface CommentPlugin extends Plugin {

	/**
	 * Publishes a comment about a key concept.
	 * @param comment the comment 
	 * @param keyConceptURI the key concept defined by its URI
	 */
	<S extends KeyConcept> void publish(Comment comment, UID<S> keyConceptURI);

	/**
	 * Gets the comment by its uuid. 
	 * @param uuid the uuid of the comment
	 * @return the comment
	 */
	Comment get(UUID uuid);

	/**
	 * Lists the comments as a key concerned is concerned
	 * @param keyConceptURI the key concept defined by its URI
	 * @return the list of comments
	 */
	<S extends KeyConcept> List<Comment> getComments(UID<S> keyConceptURI);

	/**
	 * Updates a comment.
	 * @param comment the comment
	 */
	void update(Comment comment);

}
