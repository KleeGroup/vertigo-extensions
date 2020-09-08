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
package io.vertigo.quarto.impl.publisher.merger.processor;

import java.util.List;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.core.lang.Assertion;
import io.vertigo.quarto.impl.publisher.merger.grammar.TagEncodedField;
import io.vertigo.quarto.publisher.model.PublisherData;
import io.vertigo.quarto.publisher.model.PublisherNode;

/**
 * Processor String2String qui 'evalue la chaine d'entrée comme un script java.
 * Les balises reconnues sont <% %> et <%= %>
 * @author npiedeloup
 */
public final class MergerScriptEvaluatorProcessor implements MergerProcessor {
	public static final String DATA = "data";
	private final ScriptManager scriptManager;
	private final Encoder<String, String> valueEncoder;

	/**
	 * Constructeur.
	 */
	public MergerScriptEvaluatorProcessor(final ScriptManager scriptManager, final Encoder<String, String> valueEncoder) {
		Assertion.check()
				.isNotNull(scriptManager)
				.isNotNull(valueEncoder);
		//-----
		this.scriptManager = scriptManager;
		this.valueEncoder = valueEncoder;
	}

	/** {@inheritDoc} */
	@Override
	public String execute(final String script, final PublisherData publisherData) {

		final Class<PublisherNode> dataClass = PublisherNode.class;
		final ExpressionParameter data = ExpressionParameter.of(DATA, dataClass, publisherData.getRootNode());
		final ExpressionParameter encoder = ExpressionParameter.of(TagEncodedField.ENCODER, valueEncoder.getClass(), valueEncoder);
		final List<ExpressionParameter> scriptEvaluatorParameters = List.of(
				data,
				encoder);

		return scriptManager.evaluateScript(script, SeparatorType.XML, scriptEvaluatorParameters);
	}
}
