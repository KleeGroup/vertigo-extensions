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
package io.vertigo.ui.core;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.node.Node;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.definitions.FormatterException;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.util.DtObjectUtil;
import io.vertigo.ui.core.encoders.EncoderDate;
import io.vertigo.vega.engines.webservice.json.VegaUiObject;

/**
 * Objet d'IHM, fournit les valeurs formatés des champs de l'objet métier sous-jacent.
 * Implements Map<String, Object> car struts poste des String[] que l'on reconverti en String (on prend le premier).
 *
 * @author pchretien, npiedeloup
 * @param <D> Type de DtObject représenté par cet Input
 */
public final class MapUiObject<D extends DtObject> extends VegaUiObject<D> implements Map<String, Serializable> {
	private static final long serialVersionUID = -4639050257543017072L;
	private static final String SMART_TYPE_MULTIPLE_IDS = "STyMultipleIds";

	/**
	 * Constructor.
	 * @param serverSideDto DtObject
	 */
	public MapUiObject(final D serverSideDto) {
		this(serverSideDto, (D) DtObjectUtil.createDtObject(DtObjectUtil.findDtDefinition(serverSideDto)), Collections.emptySet());
	}

	/**
	 * Constructor.
	 * @param serverSideDto  DtObject
	 * @param inputDto Input DtObject
	 * @param modifiedFields List of modified fields
	 */
	public MapUiObject(final D serverSideDto, final D inputDto, final Set<String> modifiedFields) {
		super(inputDto, modifiedFields);
		setServerSideObject(serverSideDto);
	}

	/** {@inheritDoc} */
	@Override
	public Serializable get(final Object key) {
		final String keyFieldName = String.class.cast(key);
		Assertion.check()
				.isNotBlank(keyFieldName)
				.isTrue(Character.isLowerCase(keyFieldName.charAt(0)) && !keyFieldName.contains("_"), "Le nom du champs doit-être en camelCase ({0}).", keyFieldName);
		//-----
		final DtField dtField = getDtField(keyFieldName);
		if (isMultiple(dtField)) {
			final String strValue = getInputValue(keyFieldName);
			return parseMultipleValue(strValue);
		} else if (isBoolean(dtField)) {
			final Boolean value = getTypedValue(keyFieldName, Boolean.class);
			return value != null ? String.valueOf(value) : null;
		} else {
			return getInputValue(keyFieldName);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String put(final String fieldName, final Serializable value) {
		Assertion.check()
				.isNotBlank(fieldName)
				.isNotNull(value, "La valeur formatée ne doit pas être null mais vide ({0})", fieldName)
				.isTrue(value instanceof String || value instanceof String[], "Les données saisies doivent être de type String ou String[] ({0} : {1})", fieldName, value.getClass());
		//-----
		final DtField dtField = getDtField(fieldName);
		String strValue;
		if (isMultiple(dtField)) {
			strValue = formatMultipleValue(value);
		} else if (isAboutDate(dtField)) {
			strValue = requestParameterToString(value);
			try {
				final SmartTypeManager smartTypeManager = Node.getNode().getComponentSpace().resolve(SmartTypeManager.class);
				final Object typedValue = EncoderDate.stringToValue(strValue, dtField.getSmartTypeDefinition().getBasicType());
				strValue = smartTypeManager.valueToString(dtField.getSmartTypeDefinition(), typedValue);// we fall back in the normal case if everything is right -> go to formatter
			} catch (final FormatterException e) {
				// do nothing we keep the input value
			}
		} else {
			strValue = requestParameterToString(value);
		}
		setInputValue(fieldName, strValue);
		return null;
	}

	private static String requestParameterToString(final Serializable value) {
		return value instanceof String[] ? ((String[]) value)[0] : (String) value;
	}

	private static String formatMultipleValue(final Object values) {
		if (values instanceof String) {
			// just one
			return (String) values;
		}
		// we are a String array
		return Arrays
				.stream((String[]) values)
				.collect(Collectors.joining(";"));
	}

	private static String[] parseMultipleValue(final String strValue) {
		return strValue.split(";");
	}

	private static boolean isMultiple(final DtField dtField) {
		return SMART_TYPE_MULTIPLE_IDS.equals(dtField.getSmartTypeDefinition().getName());
	}

	private static boolean isBoolean(final DtField dtField) {
		return dtField.getSmartTypeDefinition().getScope().isPrimitive() && dtField.getSmartTypeDefinition().getBasicType() == BasicType.Boolean;
	}

	private static boolean isAboutDate(final DtField dtField) {
		return dtField.getSmartTypeDefinition().getScope().isPrimitive() && dtField.getSmartTypeDefinition().getBasicType().isAboutDate();
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(final Object arg0) {
		return fieldIndex.contains(arg0);
	}

	/** Non implémenté. */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/** Non implémenté. */
	@Override
	public boolean containsValue(final Object arg0) {
		throw new UnsupportedOperationException();
	}

	/** Implémentation : TODO : see if it's ok */
	@Override
	public Set<java.util.Map.Entry<String, Serializable>> entrySet() {
		return fieldIndex
				.stream()
				.map(key -> new AbstractMap.SimpleEntry<>(key, get(key)))
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return fieldIndex.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return fieldIndex;
	}

	/** Not supported. */
	@Override
	public void putAll(final Map<? extends String, ? extends Serializable> arg0) {
		throw new UnsupportedOperationException();
	}

	/** Not supported. */
	@Override
	public String remove(final Object arg0) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return fieldIndex.size();
	}

	/** Not supported. */
	@Override
	public Collection<Serializable> values() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return the typed value.
	 * @param fieldName Field
	 * @return Typed value
	 */
	public Serializable getTypedValue(final String fieldName) {
		return getTypedValue(fieldName, Serializable.class);
	}

	/**
	 * Return a Serializable Map for client.
	 * @param fieldsForClient List of fields
	 * @param valueTransformers Map of transformers
	 * @return HashMap (needed for Serializable)
	 */
	public HashMap<String, Serializable> mapForClient(final Set<String> fieldsForClient, final Map<String, Function<Serializable, String>> valueTransformers) {
		final Set<String> filterSet = new HashSet<>();
		filterSet.addAll(fieldsForClient);
		if (fieldsForClient.contains("*")) {
			filterSet.remove("*");
			filterSet.addAll(fieldIndex);
		}
		final HashMap<String, Serializable> mapForClient = new HashMap<>(filterSet.size());
		filterSet
				.forEach(key -> mapForClient.put(key, getValueForClient(key, valueTransformers.get(key))));
		return mapForClient;

	}

	private Serializable getValueForClient(final String fieldKey, final Function<Serializable, String> valueTransformer) {
		final boolean hasFormatModifier = fieldKey.endsWith("_fmt");
		final boolean hasDisplayModifier = fieldKey.endsWith("_display");
		final String fieldName;
		if (hasFormatModifier) {
			fieldName = fieldKey.substring(0, fieldKey.length() - "_fmt".length());
		} else {
			fieldName = hasDisplayModifier ? fieldKey.substring(0, fieldKey.length() - "_display".length()) : fieldKey;
		}
		//--- if error
		if (hasFormatError(fieldName)) {
			return getInputValue(fieldName);
		}
		//--- good data
		if (hasFormatModifier) {
			return getFormattedValue(fieldName);
		}
		if (valueTransformer != null) {
			return valueTransformer.apply(getTypedValue(fieldName, Serializable.class));
		}
		return getEncodedValue(fieldName);

	}

	private Serializable getEncodedValue(final String key) {
		final String keyFieldName = String.class.cast(key);
		Assertion.check()
				.isNotBlank(keyFieldName)
				.isTrue(Character.isLowerCase(keyFieldName.charAt(0)) && !keyFieldName.contains("_"), "Le nom du champs doit-être en camelCase ({0}).", keyFieldName);
		//---
		final DtField dtField = getDtField(keyFieldName);
		final SmartTypeDefinition smartType = dtField.getSmartTypeDefinition();
		if (smartType.getScope().isPrimitive()) {
			if (isAboutDate(dtField)) {
				final Serializable value = getTypedValue(keyFieldName, Serializable.class);
				return EncoderDate.valueToString(value, dtField.getSmartTypeDefinition().getBasicType());// encodeValue
			} else if (isMultiple(dtField)) {
				final String value = getTypedValue(keyFieldName, String.class);
				return value != null ? parseMultipleValue(value) : new String[0];
			}
		}
		return getTypedValue(keyFieldName, Serializable.class);
	}

	private String getFormattedValue(final String keyFieldName) {
		final SmartTypeManager smartTypeManager = Node.getNode().getComponentSpace().resolve(SmartTypeManager.class);
		final DtField dtField = getDtField(keyFieldName);
		final Serializable typedValue = getEncodedValue(keyFieldName);
		return typedValue != null ? smartTypeManager.valueToString(dtField.getSmartTypeDefinition(), typedValue) : null;
	}

}
