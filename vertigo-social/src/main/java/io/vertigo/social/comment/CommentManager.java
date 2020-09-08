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
package io.vertigo.social.comment;

import java.util.List;

import io.vertigo.account.account.Account;
import io.vertigo.core.node.component.Component;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;

/**
 * @author pchretien
 */
public interface CommentManager extends Component {

	/**
	 * Gets a list of sorted comments published on this keyConcept.
	 * @param keyConceptUID the UID of the keyConcept
	 * @return the list of sorted comments
	 */
	List<Comment> getComments(UID<? extends KeyConcept> keyConceptUID);

	/**
	 * Publishes a comment on a key concept.
	 * @param accountUID the account defined by its UID
	 * @param comment  the comment
	 * @param keyConceptUID the UID of the keyConcept
	 */
	void publish(final UID<Account> accountUID, Comment comment, UID<? extends KeyConcept> keyConceptUID);

	/**
	 * Updates a comment.
	 * @param accountUID the account defined by its UID
	 * @param comment the updated comment
	 */
	void update(final UID<Account> accountUID, Comment comment);
}
