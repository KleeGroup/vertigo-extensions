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
package io.vertigo.ui.impl.springmvc.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.StringUtil;
import io.vertigo.datastore.kvstore.KVStoreManager;
import io.vertigo.ui.core.ComponentStates;
import io.vertigo.ui.core.FormMode;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.core.ViewContextKey;
import io.vertigo.ui.core.ViewContextMap;
import io.vertigo.ui.exception.ExpiredViewContextException;
import io.vertigo.ui.impl.springmvc.util.UiRequestUtil;
import io.vertigo.ui.impl.springmvc.util.UiUtil;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * Super class des Actions SpringMvc.
 *
 * @author npiedeloup, mlaroche
 */
public abstract class AbstractVSpringMvcController {

	public static final String DEFAULT_VIEW_NAME_ATTRIBUTE = "defaultViewName";

	/** Clé de la collection des contexts dans le KVStoreManager. */
	public static final String CONTEXT_COLLECTION_NAME = "VViewContext";

	/** Clé de context du UiUtil. */
	public static final ViewContextKey<UiUtil> UTIL_CONTEXT_KEY = ViewContextKey.of("util");
	/** Clé de context du mode. */
	public static final ViewContextKey<FormMode> MODE_CONTEXT_KEY = ViewContextKey.of("mode");
	//TODO voir pour déléguer cette gestion des modes
	/** Clé de context du mode Edit. */
	public static final ViewContextKey<Boolean> MODE_EDIT_CONTEXT_KEY = ViewContextKey.of("modeEdit");
	/** Clé de context du mode ReadOnly. */
	public static final ViewContextKey<Boolean> MODE_READ_ONLY_CONTEXT_KEY = ViewContextKey.of("modeReadOnly");
	/** Clé de context du mode Create. */
	public static final ViewContextKey<Boolean> MODE_CREATE_CONTEXT_KEY = ViewContextKey.of("modeCreate");
	/** Préfix des clés des paramètres passés par l'url. */
	public static final String URL_PARAM_PREFIX = "params.";

	private static final String SLASH = "/";

	/**
	 * Indique que l'initialisation du context par un parametre de l'url est autorisé.
	 */
	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AcceptCtxQueryParam {
		//rien
	}

	@Inject
	private KVStoreManager kvStoreManager;
	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private JsonEngine jsonEngine;

	public void prepareContext(final HttpServletRequest request) throws ExpiredViewContextException {
		final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		ViewContext viewContext = null;
		final String ctxId = request.getParameter(ViewContext.CTX.get());
		if ("POST".equals(request.getMethod()) || ctxId != null && acceptCtxQueryParam()) {
			if (ctxId == null) {
				contextMiss(null);
			} else {
				ViewContextMap viewContextMap;
				try (VTransactionWritable transactionWritable = transactionManager.createCurrentTransaction()) {
					viewContextMap = kvStoreManager.find(CONTEXT_COLLECTION_NAME, ctxId, ViewContextMap.class).orElse(null);
					UiRequestUtil.setRequestScopedAttribute("createdContext", false);
				}
				if (viewContextMap == null) {
					contextMiss(ctxId);
				}
				viewContext = new ViewContext(viewContextMap, jsonEngine);
				viewContext.makeModifiable();
				viewContext.setInputCtxId(ctxId);
				attributes.setAttribute("viewContext", viewContext, RequestAttributes.SCOPE_REQUEST);
			}

		} else {
			viewContext = new ViewContext(new ViewContextMap(), jsonEngine);
			attributes.setAttribute("viewContext", viewContext, RequestAttributes.SCOPE_REQUEST);
			//initContextUrlParameters(request, viewContext);
			//TODO vérifier que l'action demandée n'attendait pas de context : il va etre recrée vide ce qui n'est pas bon dans certains cas.
			preInitContext(viewContext);
			Assertion.check().isTrue(viewContext.containsKey(UTIL_CONTEXT_KEY), "Pour surcharger preInitContext vous devez rappeler les parents super.preInitContext(). Action: {0}",
					getClass().getSimpleName());
			//initContext();
		}
		viewContext.setCtxId();
		if (useDefaultViewName()) {
			request.setAttribute(DEFAULT_VIEW_NAME_ATTRIBUTE, getDefaultViewName(this));
		}
	}

	/**
	 * Definition if whe should use the vertigo conventions to determine the default viewname
	 * @return if we should use it
	 */
	protected boolean useDefaultViewName() {
		return true;
	}

	private static String getDefaultViewName(final AbstractVSpringMvcController controller) {
		String path = controller.getClass().getName();
		path = path.substring(0, path.lastIndexOf('.'));
		//package is
		// group.id.project.feature.controllers and we look in feature/...
		// or group.id.project.controllers and we look in project/
		Assertion.check().isTrue(path.contains(".controllers"), "Default naming only works if your package contains .controllers, it's not the case for the controller {0}", controller.getClass());
		path = path.substring(path.lastIndexOf('.', path.indexOf(".controllers") - 1) + 1);
		path = path.replaceAll("\\.controllers", "");
		path = path.replaceAll("\\.", SLASH);
		String simpleName = StringUtil.first2LowerCase(controller.getClass().getSimpleName());
		simpleName = simpleName.replaceAll("Controller", "");
		return path + SLASH + simpleName;
	}

	private boolean acceptCtxQueryParam() {
		return this.getClass().isAnnotationPresent(AcceptCtxQueryParam.class);
	}

	/**
	 * Appeler lorsque que le context est manquant.
	 * Par défaut lance une ExpiredContextException.
	 * Mais une action spécifique pourrait reconstruire le context si c'est pertinent.
	 * @param ctxId Id du context manquant (seule info disponible)
	 * @throws ExpiredViewContextException Context expiré (comportement standard)
	 */
	protected void contextMiss(final String ctxId) throws ExpiredViewContextException {
		throw new ExpiredViewContextException("Context ctxId:'" + ctxId + "' manquant");
	}

	/**
	 * Preinitialisation du context, pour ajouter les composants standard.
	 * Si surcharger doit rappeler le super.preInitContext();
	 */
	protected void preInitContext(final ViewContext viewContext) {
		viewContext.publishRef(UTIL_CONTEXT_KEY, new UiUtil());
		viewContext.asMap().put("componentStates", new ComponentStates());
		toModeReadOnly();
	}

	/**
	 * Conserve et fige le context.
	 * Utilisé par le KActionContextStoreInterceptor.
	 */
	public final void storeContext() {
		final ViewContext viewContext = getViewContext();
		try (VTransactionWritable transactionWritable = transactionManager.createCurrentTransaction()) {
			kvStoreManager.put(CONTEXT_COLLECTION_NAME, viewContext.getId(), viewContext.asMap());// we only store the underlying map
			transactionWritable.commit();
		}
	}

	/**
	 * Conserve et fige le context.
	 * Utilisé par le KActionContextStoreInterceptor.
	 */
	public final void makeUnmodifiable() {
		final ViewContext viewContext = getViewContext();
		viewContext.makeUnmodifiable();
	}

	/** {@inheritDoc} */

	public final void validate() {
		//rien
	}

	/** {@inheritDoc} */
	private static ViewContext getViewContext() {
		final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		final ViewContext viewContext = (ViewContext) attributes.getAttribute("viewContext", RequestAttributes.SCOPE_REQUEST);
		Assertion.check().isNotNull(viewContext);
		//---
		return viewContext;
	}

	/**
	 * Passe en mode edition.
	 */
	protected static final void toModeEdit() {
		//TODO voir pour déléguer cette gestion des modes
		final ViewContext viewContext = getViewContext();
		viewContext.publishRef(MODE_CONTEXT_KEY, FormMode.edit);
		viewContext.publishRef(MODE_READ_ONLY_CONTEXT_KEY, false);
		viewContext.publishRef(MODE_EDIT_CONTEXT_KEY, true);
		viewContext.publishRef(MODE_CREATE_CONTEXT_KEY, false);
	}

	/**
	 * Passe en mode creation.
	 */
	protected static final void toModeCreate() {
		//TODO voir pour déléguer cette gestion des modes
		final ViewContext viewContext = getViewContext();
		viewContext.publishRef(MODE_CONTEXT_KEY, FormMode.create);
		viewContext.publishRef(MODE_READ_ONLY_CONTEXT_KEY, false);
		viewContext.publishRef(MODE_EDIT_CONTEXT_KEY, false);
		viewContext.publishRef(MODE_CREATE_CONTEXT_KEY, true);
	}

	/**
	 * Passe en mode readonly.
	 */
	protected static final void toModeReadOnly() {
		//TODO voir pour déléguer cette gestion des modes
		final ViewContext viewContext = getViewContext();
		viewContext.publishRef(MODE_CONTEXT_KEY, FormMode.readOnly);
		viewContext.publishRef(MODE_READ_ONLY_CONTEXT_KEY, true);
		viewContext.publishRef(MODE_EDIT_CONTEXT_KEY, false);
		viewContext.publishRef(MODE_CREATE_CONTEXT_KEY, false);
	}

	/**
	 * @return Si on est en mode edition
	 */
	protected static final boolean isModeEdit() {
		final ViewContext viewContext = getViewContext();
		return FormMode.edit.equals(viewContext.get(MODE_CONTEXT_KEY));
	}

	/**
	 * @return Si on est en mode readOnly
	 */
	protected static final boolean isModeRead() {
		final ViewContext viewContext = getViewContext();
		return FormMode.readOnly.equals(viewContext.get(MODE_CONTEXT_KEY));
	}

	/**
	 * @return Si on est en mode create
	 */
	protected static final boolean isModeCreate() {
		final ViewContext viewContext = getViewContext();
		return FormMode.create.equals(viewContext.get(MODE_CONTEXT_KEY));
	}

	/**
	 * @return Pile des messages utilisateur.
	 */
	public static final UiMessageStack getUiMessageStack() {
		return UiRequestUtil.obtainCurrentUiMessageStack();
	}

	public boolean isViewContextDirty() {
		return getViewContext().isDirty();
	}

	protected boolean isNewContext() {
		return UiRequestUtil.getRequestScopedAttribute("createdContext", Boolean.class).orElse(true);
	}

}
