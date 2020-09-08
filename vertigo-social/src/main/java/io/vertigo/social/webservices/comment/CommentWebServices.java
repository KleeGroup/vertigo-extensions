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
package io.vertigo.social.webservices.comment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.authentication.AuthenticationManager;
import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.locale.MessageText;
import io.vertigo.core.node.Node;
import io.vertigo.core.util.MapBuilder;
import io.vertigo.datamodel.structure.definitions.DtDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.KeyConcept;
import io.vertigo.datamodel.structure.model.UID;
import io.vertigo.social.comment.Comment;
import io.vertigo.social.comment.CommentManager;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.ExcludedFields;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PUT;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;

/**
 * Webservice for Notification extension.
 *
 * @author npiedeloup
 */
@PathPrefix("/x/comment")
public final class CommentWebServices implements WebServices {

	private static final String API_VERSION = "0.1.0";
	private static final String IMPL_VERSION = "0.9.2";

	@Inject
	private CommentManager commentServices;

	@Inject
	private AuthenticationManager authenticationManager;

	/**
	 * Gets comments for keyConcept.
	 * @param keyConcept KeyConcept type
	 * @param id KeyConcept id
	 * @return comments for keyConcept
	 */
	@GET("/api/comments")
	public List<Comment> getComments(@QueryParam("concept") final String keyConcept, @QueryParam("id") final String id) {
		final UID<KeyConcept> keyConceptURI = readKeyConceptURI(keyConcept, id);
		return commentServices.getComments(keyConceptURI);
	}

	/**
	 * Publishes a new comment.
	 * @param comment Comment msg
	 * @param keyConcept KeyConcept type
	 * @param id KeyConcept id
	 */
	@POST("/api/comments")
	public Comment publishComment(@ExcludedFields("uuid") final Comment comment, @QueryParam("concept") final String keyConcept, @QueryParam("id") final String id) {
		final UID<KeyConcept> keyConceptURI = readKeyConceptURI(keyConcept, id);
		commentServices.publish(getLoggedAccountURI(), comment, keyConceptURI);
		return comment;
	}

	/**
	 * Updates a comment.
	 * @param uuid Comment uuid
	 * @param comment Comment msg
	 */
	@PUT("/api/comments/{uuid}")
	public Comment updateComment(@PathParam("uuid") final String uuid, final Comment comment) {
		Assertion.check()
				.isNotNull(uuid)
				.isNotNull(comment)
				.isTrue(uuid.equals(comment.getUuid().toString()), "Comment uuid ({0}) must match WebService route ({1})", comment.getUuid(), uuid);
		//-----
		commentServices.update(getLoggedAccountURI(), comment);
		return comment;
	}

	//-----
	/**
	 * Returns status (code 200 or 500)
	 * @return "OK" or error message
	 */
	@GET("/status")
	@AnonymousAccessAllowed
	public String getStatus() {
		return "OK";
	}

	/**
	 * Returns  stats.
	 * @return "OK" or error message
	 */
	@GET("/stats")
	@AnonymousAccessAllowed
	public Map<String, Object> getStats() {
		final Map<String, Object> stats = new HashMap<>();
		final Map<String, Object> sizeStats = new HashMap<>();
		sizeStats.put("comments", "not yet");
		stats.put("size", sizeStats);
		return stats;
	}

	/**
	 * Returns  config.
	 * @return Config object
	 */
	@GET("/config")
	@AnonymousAccessAllowed
	public Map<String, Object> getConfig() {
		return new MapBuilder<String, Object>()
				.put("api-version", API_VERSION)
				.put("impl-version", IMPL_VERSION)
				.build();
	}

	/**
	 * Returns  help.
	 * @return Help object
	 */
	@GET("/help")
	@AnonymousAccessAllowed
	public String getHelp() {
		return "##Comment extension"
				+ "\n This extension manage the comment center.";
	}

	private static UID<KeyConcept> readKeyConceptURI(final String keyConcept, @QueryParam("id") final String id) {
		final DtDefinition dtDefinition = Node.getNode().getDefinitionSpace().resolve("Dt" + keyConcept, DtDefinition.class);
		final Object keyConceptId = stringToId(id, dtDefinition);
		return UID.of(dtDefinition, keyConceptId);
	}

	private static Object stringToId(final String id, final DtDefinition dtDefinition) {
		final Optional<DtField> idFieldOpt = dtDefinition.getIdField();
		Assertion.check().isTrue(idFieldOpt.isPresent(), "KeyConcept {0} must have an id field, in order to support Comment extension", dtDefinition.getLocalName());

		final Class dataType = idFieldOpt.get().getSmartTypeDefinition().getJavaClass();
		if (String.class.isAssignableFrom(dataType)) {
			return id;
		} else if (Integer.class.isAssignableFrom(dataType)) {
			return Integer.valueOf(id);
		} else if (Long.class.isAssignableFrom(dataType)) {
			return Long.valueOf(id);
		}
		throw new IllegalArgumentException("the id of the keyConcept " + dtDefinition.getLocalName() + " must be String, Long or Integer");
	}

	private UID<Account> getLoggedAccountURI() {
		return authenticationManager.getLoggedAccount()
				.orElseThrow(() -> new VSecurityException(MessageText.of("No account logged in")))
				.getUID();
	}

}
