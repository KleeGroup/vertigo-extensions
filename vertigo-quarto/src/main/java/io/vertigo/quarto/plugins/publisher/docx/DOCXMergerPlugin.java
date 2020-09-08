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
package io.vertigo.quarto.plugins.publisher.docx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.quarto.impl.publisher.MergerPlugin;
import io.vertigo.quarto.impl.publisher.merger.grammar.ScriptGrammarUtil;
import io.vertigo.quarto.impl.publisher.merger.processor.GrammarEvaluatorProcessor;
import io.vertigo.quarto.impl.publisher.merger.processor.GrammarXMLBalancerProcessor;
import io.vertigo.quarto.impl.publisher.merger.processor.MergerProcessor;
import io.vertigo.quarto.impl.publisher.merger.processor.MergerScriptEvaluatorProcessor;
import io.vertigo.quarto.impl.publisher.merger.script.ScriptGrammar;
import io.vertigo.quarto.publisher.PublisherFormat;
import io.vertigo.quarto.publisher.model.PublisherData;

/**
 * Gestionnaire des fusions de documents Docx.
 *
 * @author adufranne
 */
public final class DOCXMergerPlugin implements MergerPlugin {
	private final List<MergerProcessor> mergerProcessors;

	/**
	 * Constructeur avec ODTScriptGrammar par défaut.
	 *
	 * @param scriptManager le script manager.
	 */
	@Inject
	public DOCXMergerPlugin(final ScriptManager scriptManager) {
		Assertion.check().isNotNull(scriptManager);
		//-----
		mergerProcessors = createMergerProcessors(scriptManager, ScriptGrammarUtil.createScriptGrammar());
	}

	private static List<MergerProcessor> createMergerProcessors(final ScriptManager scriptManager, final ScriptGrammar scriptGrammar) {
		Assertion.check()
				.isNotNull(scriptManager)
				.isNotNull(scriptGrammar);
		//-----
		return List.of(
				// Extraction des variables.
				new DOCXReverseInputProcessor(),
				// équilibrage de l'arbre xml.
				new GrammarXMLBalancerProcessor(),
				// kscript <##> => jsp <%%>.
				new GrammarEvaluatorProcessor(scriptManager, scriptGrammar),
				// Traitement Janino (TEXT balisé en java + Données => TEXT).
				new MergerScriptEvaluatorProcessor(scriptManager, new DOCXValueEncoder()),
				// Post traitements (TEXT => XML(DOCX)).
				new DOCXCleanerProcessor());
	}

	/**
	 * Effectue la fusion.
	 *
	 * @return Fichier résultat de la fusion
	 * @throws IOException Exception système
	 */
	@Override
	public File execute(final URL modelFileURL, final PublisherData data) throws IOException {
		Assertion.check()
				.isNotNull(modelFileURL)
				.isNotNull(data);
		//-----
		final File file = DOCXUtil.obtainModelFile(modelFileURL);
		file.setReadOnly(); //on protège le fichier
		try {
			return doExecute(file, data);
		} finally {
			file.setWritable(true); //on le remet éditable
		}
	}

	/**
	 * Effectue le traitement.
	 *
	 * @param modelFile Fichier model, ce fichier n'est pas modifié.
	 * @param publisherData Parametres des données à fusionner
	 * @return Fichier d'entrée modifié par le sous-processor
	 */
	private File doExecute(final File modelFile, final PublisherData publisherData) throws IOException {
		try (final ZipFile docxFile = new ZipFile(modelFile)) {
			final Map<String, String> xmlContents = DOCXUtil.extractDOCXContents(docxFile);
			// Phase 1 : Exécution, chaque String est traitée dans l'ordre des processeurs.
			for (final MergerProcessor currentMergerProcessor : mergerProcessors) {
				// On passe le traitement sur les fichiers de Docx
				for (final Entry<String, String> xmlContent : xmlContents.entrySet()) {
					final String newXmlContent = currentMergerProcessor.execute(xmlContent.getValue(), publisherData);
					xmlContents.put(xmlContent.getKey(), newXmlContent);
				}
			}

			// Phase 2 : Reconstruction, le Fichier Docx est recomposé à partir des fichiers traités
			return DOCXUtil.createDOCX(docxFile, xmlContents);
		}
	}

	/** {@inheritDoc} */
	@Override
	public PublisherFormat getPublisherFormat() {
		return PublisherFormat.DOCX;
	}
}
