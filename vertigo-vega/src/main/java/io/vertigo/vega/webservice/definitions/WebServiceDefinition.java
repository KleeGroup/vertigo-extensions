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
package io.vertigo.vega.webservice.definitions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.vega.webservice.definitions.WebServiceParam.WebServiceParamType;

/**
 * Web service definition.
 * @author npiedeloup
 */
@DefinitionPrefix(WebServiceDefinition.PREFIX)
public final class WebServiceDefinition extends AbstractDefinition {
	public static final String PREFIX = "Ws";

	/**
	 * HTTP Verb supported.
	 */
	public enum Verb {
		Get,
		Post,
		Put,
		Patch,
		Delete,
	}

	private final String path;
	private final String sortPath;
	private final Verb verb;
	private final String acceptType;

	private final Method method; //method use to handle this WebService
	private final boolean needSession;
	private final boolean sessionInvalidate;
	private final boolean needAuthentification;

	private final boolean accessTokenPublish;
	private final boolean accessTokenMandatory;
	private final boolean accessTokenConsume;
	private final boolean serverSideSave;

	private final Set<String> includedFields;
	private final Set<String> excludedFields;

	private final List<WebServiceParam> webServiceParams;
	private final String doc;
	private final boolean corsProtected;
	private final boolean fileAttachment;

	/**
	 * Constructor.
	 */
	WebServiceDefinition(
			final String name,
			final Verb verb,
			final String path,
			final String sortPath,
			final String acceptType,
			final Method method,
			final boolean needSession,
			final boolean sessionInvalidate,
			final boolean needAuthentification,
			final boolean accessTokenPublish,
			final boolean accessTokenMandatory,
			final boolean accessTokenConsume,
			final boolean serverSideSave,
			final Set<String> includedFields,
			final Set<String> excludedFields,
			final List<WebServiceParam> webServiceParams,
			final String doc,
			final boolean corsProtected,
			final boolean fileAttachment) {
		super(name);
		//---
		Assertion.check()
				.isNotNull(verb)
				.isNotBlank(path)
				.isNotBlank(sortPath)
				.isNotBlank(acceptType)
				.isNotNull(method)
				.isNotNull(includedFields)
				.isNotNull(excludedFields)
				.isNotNull(webServiceParams)
				.isNotNull(doc); //doc can be empty
		final String userFriendlyMethodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
		Assertion.check()
				.when(accessTokenConsume, () -> Assertion.check()
						.isTrue(accessTokenMandatory, "AccessToken mandatory for accessTokenConsume ({0})", userFriendlyMethodName))
				.when(serverSideSave, () -> Assertion.check()
						.isTrue(needSession, "Session mandatory for serverSideState ({0})", userFriendlyMethodName)
						.isTrue(!Void.TYPE.equals(method.getReturnType()), "Return object mandatory for serverSideState ({0})", userFriendlyMethodName));
		checkPathParams(path, webServiceParams, userFriendlyMethodName);
		//-----
		this.verb = verb;
		this.path = path;
		this.sortPath = sortPath;
		this.acceptType = acceptType;

		this.method = method;
		this.needSession = needSession;
		this.sessionInvalidate = sessionInvalidate;
		this.needAuthentification = needAuthentification;

		this.accessTokenPublish = accessTokenPublish;
		this.accessTokenMandatory = accessTokenMandatory;
		this.accessTokenConsume = accessTokenConsume;
		this.serverSideSave = serverSideSave;

		this.includedFields = Collections.unmodifiableSet(new LinkedHashSet<>(includedFields));
		this.excludedFields = Collections.unmodifiableSet(new LinkedHashSet<>(excludedFields));
		this.webServiceParams = Collections.unmodifiableList(new ArrayList<>(webServiceParams));

		this.doc = doc;
		this.corsProtected = corsProtected;
		this.fileAttachment = fileAttachment;
	}

	/**
	 * Static method factory for WebServiceDefinitionBuilder
	 * @param method Method to bind to this webService
	 * @return WebServiceDefinitionBuilder
	 */
	public static WebServiceDefinitionBuilder builder(final Method method) {
		return new WebServiceDefinitionBuilder(method);
	}

	private static void checkPathParams(final String myPath, final List<WebServiceParam> myWebServiceParams, final String methodName) {
		final Set<String> inputPathParam = new HashSet<>();
		final Set<String> urlPathParam = new HashSet<>();
		for (final WebServiceParam myWebServiceParam : myWebServiceParams) {
			if (myWebServiceParam.getParamType() == WebServiceParamType.Path) {
				inputPathParam.add(myWebServiceParam.getName());
			}
		}
		final Pattern pattern = Pattern.compile("\\{(.+?)\\}");
		final Matcher matcher = pattern.matcher(myPath);
		while (matcher.find()) {
			urlPathParam.add(matcher.group(1)); //group 0 is always the entire match
		}
		final Set<String> notUsed = new HashSet<>(urlPathParam);
		notUsed.removeAll(inputPathParam);
		Assertion.check().isTrue(notUsed.isEmpty(), "Some pathParam of {1} declared in path are not used {0}", notUsed, methodName);

		final Set<String> notDeclared = new HashSet<>(inputPathParam);
		notDeclared.removeAll(urlPathParam);
		Assertion.check().isTrue(notDeclared.isEmpty(), "Some pathParam of {1} are not declared in path {0}", notDeclared, methodName);
	}

	/**
	 * @return path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return sortPath
	 */
	public String getSortPath() {
		return sortPath;
	}

	/**
	 * @return verb
	 */
	public Verb getVerb() {
		return verb;
	}

	/**
	 * @return acceptType
	 */
	public String getAcceptType() {
		return acceptType;
	}

	/**
	 * @return method
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * @return needSession
	 */
	public boolean isNeedSession() {
		return needSession;
	}

	/**
	 * @return sessionInvalidate
	 */
	public boolean isSessionInvalidate() {
		return sessionInvalidate;
	}

	/**
	 * @return needAuthentification
	 */
	public boolean isNeedAuthentification() {
		return needAuthentification;
	}

	/**
	 * @return accessTokenPublish
	 */
	public boolean isAccessTokenPublish() {
		return accessTokenPublish;
	}

	/**
	 * @return accessTokenMandatory
	 */
	public boolean isAccessTokenMandatory() {
		return accessTokenMandatory;
	}

	/**
	 * @return accessTokenConsume
	 */
	public boolean isAccessTokenConsume() {
		return accessTokenConsume;
	}

	/**
	 * @return serverSideSave
	 */
	public boolean isServerSideSave() {
		return serverSideSave;
	}

	/**
	 * @return includedFields
	 */
	public Set<String> getIncludedFields() {
		return includedFields;
	}

	/**
	 * @return excludedFields
	 */
	public Set<String> getExcludedFields() {
		return excludedFields;
	}

	/**
	 * @return webServiceParams
	 */
	public List<WebServiceParam> getWebServiceParams() {
		return webServiceParams;
	}

	/**
	 * @return doc
	 */
	public String getDoc() {
		return doc;
	}

	/**
	 * @return corsProtected
	 */
	public boolean isCorsProtected() {
		return corsProtected;
	}

	/**
	 * @return fileAttachment
	 */
	public boolean isFileAttachment() {
		return fileAttachment;
	}
}
