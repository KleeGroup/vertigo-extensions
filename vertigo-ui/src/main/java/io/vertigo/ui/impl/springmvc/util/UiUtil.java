/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.ui.impl.springmvc.util;

import java.io.Serializable;
import java.util.List;

import io.vertigo.app.Home;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;
import io.vertigo.lang.Assertion;
import io.vertigo.ui.core.AbstractUiListUnmodifiable;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.util.StringUtil;
import io.vertigo.vega.webservice.model.UiList;
import io.vertigo.vega.webservice.model.UiObject;

/**
 * Class utilitaire pour le rendu des pages en jsp/ftl.
 * @author npiedeloup
 */
public final class UiUtil implements Serializable {

	private static final long serialVersionUID = -5677843485950859547L;
	private static final Formatter DEFAULT_FORMATTER = new FormatterDefault(null);//by convention : no args

	/**
	 * Constructor.
	 */
	//can't be private, because an instance must be put into struts context, for access from tags.
	public UiUtil() {
		//empty
	}

	/**
	 * @param uiObject Object du context
	 * @return Nom de l'object dans le context
	 */
	public static String contextKey(final UiObject<?> uiObject) {
		final ViewContext viewContext = UiRequestUtil.getCurrentViewContext();
		return viewContext.findKey(uiObject);
	}

	/**
	 * @param uiList List du context
	 * @param uiObject Objet de la liste
	 * @return index de l'objet dans sa liste
	 */
	public static int indexOf(final List<?> uiList, final UiObject<?> uiObject) {
		return uiList.indexOf(uiObject);
	}

	/**
	 * @param fieldPath Chemin du champ
	 * @return Label du champs
	 */
	public static String label(final String fieldPath) {
		return getDtField(fieldPath).getLabel().getDisplay();
	}

	/**
	 * @param fieldPath Chemin du champ
	 * @return Domain du champs
	 */
	public static String domainUnit(final String object, final String fieldName, final String overrideValue) {
		if (overrideValue != null) {
			return overrideValue;
		} else if (fieldName != null) {
			return getDtField(object + "." + fieldName).getDomain().getProperties().getValue(DtProperty.UNIT);
		}
		return "";
	}

	/**
	 * @param fieldPath Chemin du champ
	 * @return Domain du champs
	 */
	public static Integer domainMaxLength(final String object, final String fieldName) {
		if (fieldName != null) {
			return getDtField(object + "." + fieldName).getDomain().getProperties().getValue(DtProperty.MAX_LENGTH);
		}
		return null;
	}

	/**
	 * @param fieldPath Chemin du champ
	 * @return Domain du champs
	 */
	public static String domainCss(final String object, final String fieldName, final String overrideValue, final String defaultValue) {
		if (overrideValue != null) {
			return overrideValue;
		} else if (fieldName != null) {
			return "col_" + getDtField(object + "." + fieldName).getDomain().getName();
		}
		return defaultValue;
	}

	/**
	 * @param fieldPath Chemin du champ
	 * @return Domain du champs
	 */
	public static String domainAlign(final String object, final String fieldName, final String overrideValue) {
		if (overrideValue != null) {
			return overrideValue;
		} else if (fieldName != null) {
			final DataType dataType = getDtField(object + "." + fieldName).getDomain().getDataType();
			switch (dataType) {
				case Long:
				case Integer:
				case Double:
				case BigDecimal:
					return "right";
				case Boolean:
				case Instant:
				case LocalDate:
				case String:
				case DataStream:
				default:
					return "left";
			}
		}
		return "left";
	}

	/**
	 * @param fieldPath Chemin du champ boolean
	 * @param value Valeur à formater
	 * @return rendu du champs boolean
	 */
	public static String formatBoolean(final String fieldPath, final Boolean value) {
		if (!fieldPath.contains(".")) { //cas des ContextRef sans domain
			return DEFAULT_FORMATTER.valueToString(value, DataType.Boolean);
		}
		return getDtField(fieldPath).getDomain().valueToString(value);
	}

	public static Double getMinValue(final String fieldPath) {
		return getDtField(fieldPath).getDomain().getProperties().getValue(DtProperty.MIN_VALUE);
	}

	public static Double getMaxValue(final String fieldPath) {
		return getDtField(fieldPath).getDomain().getProperties().getValue(DtProperty.MAX_VALUE);
	}

	public static Double getStep(final Double minValue, final Double maxValue) {
		Assertion.checkNotNull(minValue);
		Assertion.checkNotNull(maxValue);
		Assertion.checkState(maxValue > minValue, "Unable to calculate step : maxValue '{0}' must be superior to minValue '{1}'", maxValue, minValue);
		//---
		final Double rawStep = (maxValue - minValue) / 200; // we allow at max 200 possible values

		final double index = Math.floor(Math.log10(rawStep));
		final double step = Math.pow(10, index);
		if (rawStep <= step) {
			return step;
		} else if (rawStep <= 2 * step) {
			return 2 * step;
		} else if (rawStep <= 5 * step) {
			return 5 * step;
		} else {
			return 10 * step;
		}

	}

	/**
	 * @param fieldPath Chemin du champ
	 * @return Si le champs est obligatoire
	 */
	public static boolean required(final String fieldPath) {
		Assertion.checkArgument(fieldPath.indexOf('.') != 0, "FieldPath shouldn't starts with . ({0})", fieldPath);
		//-----
		if (fieldPath.indexOf('.') > 0) { //Le champs est porté par un Object
			return getDtField(fieldPath).isRequired();
		}
		return false; //on ne sait pas dire, mais on ne force pas à obligatoire
	}

	/**
	 * @param uiList liste du context
	 * @return Nom du champ display de cette liste
	 */
	public static String getDisplayField(final String uiListKey) {
		final DtDefinition dtDefinition = getUiList(uiListKey).getDtDefinition();
		return dtDefinition.getDisplayField().get().getName();
	}

	/**
	 * @param uiList liste du context
	 * @return Nom du champ de l'id de cette liste
	 */
	public static String getIdField(final String uiListKey) {
		final UiList uiList = getUiList(uiListKey);
		if (uiList instanceof AbstractUiListUnmodifiable) {
			return ((AbstractUiListUnmodifiable) uiList).getIdFieldName();
		}
		final DtDefinition dtDefinition = getUiList(uiListKey).getDtDefinition();
		return dtDefinition.getIdField().get().getName();
	}

	/**
	 * Get the current locale prefix (either the user's or the app (see LocaleManager javadoc)
	 * ex : fr, en-us, en-gb, es, it
	 * @return the locale (in the quasar's style) to download the right js file
	 */
	public static String getCurrentLocalePrefixForQuasar() {
		final LocaleManager localeManager = Home.getApp().getComponentSpace().resolve(LocaleManager.class);
		final String currentLocaleTag = localeManager.getCurrentLocale().toLanguageTag();
		// not so great but not other solutions (quasar's doesn't respect the standard...)
		if (currentLocaleTag.startsWith("fr")) {
			return "fr";
		} else if (currentLocaleTag.startsWith("es")) {
			return "es";
		} else if (currentLocaleTag.startsWith("it")) {
			return "it";
		} else if ("en".equals(currentLocaleTag)) {
			return "en-us"; //we need to make a choice...
		} else {
			return currentLocaleTag.toLowerCase();
		}
	}

	/**
	 * Get the current locale tag (either the user's or the app (see LocaleManager javadoc)
	 * ex : fr, enUs, enGb, es, it
	 * @return the locale (in the quasar's style) to select the right language in quasar
	 */
	public static String getCurrentLocaleForQuasar() {
		return StringUtil.constToLowerCamelCase(getCurrentLocalePrefixForQuasar().toUpperCase().replace('-', '_'));
	}

	private static UiList getUiList(final String uiListKey) {
		final ViewContext viewContext = UiRequestUtil.getCurrentViewContext();
		return (UiList) viewContext.get(uiListKey);
	}

	private static DtField getDtField(final String fieldPath) {
		Assertion.checkArgument(fieldPath.indexOf('.') > 0, "Le champs n'est pas porté par un Object ({0})", fieldPath);
		//Assertion.checkArgument(fieldPath.indexOf('.') == fieldPath.lastIndexOf('.'), "Seul un point est autorisé ({0})", fieldPath);
		final String contextKey = fieldPath.substring(0, fieldPath.lastIndexOf('.'));
		final String fieldName = fieldPath.substring(fieldPath.lastIndexOf('.') + 1);
		final ViewContext viewContext = UiRequestUtil.getCurrentViewContext();
		final Object contextObject = viewContext.get(contextKey);
		Assertion.checkNotNull(contextObject, "{0} n''est pas dans le context", contextKey);
		Assertion.checkArgument(contextObject instanceof UiObject || contextObject instanceof UiList, "{0}({1}) doit être un UiObject ou une UiList ", contextKey,
				contextObject.getClass().getSimpleName());

		final DtDefinition dtDefinition;
		if (contextObject instanceof UiObject) {
			dtDefinition = ((UiObject<?>) contextObject).getDtDefinition();
		} else {
			dtDefinition = ((UiList<?>) contextObject).getDtDefinition();
		}
		Assertion.checkNotNull(dtDefinition); //, "{0}({1}) doit être un UiObject ou un UiList ", contextKey, contextObject.getClass().getSimpleName());
		Assertion.checkNotNull(dtDefinition, "{0}({1}) doit être un UiObject ou un UiList ", contextKey, contextObject.getClass().getSimpleName());
		return dtDefinition.getField(fieldName);

	}
}
