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
package io.vertigo.quarto.plugins.converter.openoffice;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.util.TempFile;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.filestore.util.FileUtil;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.quarto.impl.converter.ConverterPlugin;

/**
 * Conversion des fichiers à partir de OpenOffice.
 * @author npiedeloup
 */
abstract class AbstractOpenOfficeConverterPlugin implements ConverterPlugin, Activeable {
	/** Le port par défaut pour accéder à OpenOffice est 8100. */
	public static final int DEFAULT_UNO_PORT = 8100;

	private static final Logger LOGGER = LogManager.getLogger(AbstractOpenOfficeConverterPlugin.class);

	private final ExecutorService executors = Executors.newFixedThreadPool(1);

	private final String unoHost;
	private final int unoPort;
	private final int convertTimeoutSeconds;

	/**
	 * Constructor.
	 * @param unoHost Hote du serveur OpenOffice
	 * @param unoPort Port de connexion au serveur OpenOffice
	 * @param convertTimeoutSeconds Convert timeout in seconds
	 */
	protected AbstractOpenOfficeConverterPlugin(final String unoHost, final String unoPort, final int convertTimeoutSeconds) {
		super();
		Assertion.check()
				.isNotBlank(unoHost)
				.isTrue(convertTimeoutSeconds >= 1 && convertTimeoutSeconds <= 900, "Le timeout de conversion est exprimé en seconde et doit-être compris entre 1s et 15min (900s)");
		//-----
		this.unoHost = unoHost;
		this.unoPort = Integer.parseInt(unoPort);
		this.convertTimeoutSeconds = convertTimeoutSeconds;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//nothing
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		executors.shutdown();
	}

	/** {@inheritDoc} */
	@Override
	public final VFile convertToFormat(final VFile file, final String targetFormat) {
		Assertion.check().isNotBlank(targetFormat);
		//-----
		return convertToFormat(file, ConverterFormat.find(targetFormat));
	}

	private VFile convertToFormat(final VFile file, final ConverterFormat targetFormat) {
		Assertion.check()
				.isNotNull(file)
				.isNotNull(targetFormat)
				// si le format de sortie est celui d'entrée la convertion est inutile
				.isFalse(targetFormat.getTypeMime().equals(file.getMimeType()), "Le format de sortie est identique à celui d'entrée ; la conversion est inutile");
		//-----
		final Path inputFile = FileUtil.obtainReadOnlyPath(file);
		final Callable<Path> convertTask = () -> doConvertToFormat(inputFile.toFile(), targetFormat).toPath();
		final Path targetFile;
		try {
			final Future<Path> targetFileFuture = executors.submit(convertTask);
			targetFile = targetFileFuture.get(convertTimeoutSeconds, TimeUnit.SECONDS);
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Erreur de conversion du document au format {0} ({1})", targetFormat.name(), e.getClass().getSimpleName());
		}
		return FSFile.of(targetFile);
	}

	// On synchronize sur le plugin car OpenOffice supporte mal les accès concurrents.
	/**
	 * @param inputFile Fichier source
	 * @param targetFormat Format de destination
	 * @return Fichier resultat
	 * @throws Exception Exception
	 */
	synchronized File doConvertToFormat(final File inputFile, final ConverterFormat targetFormat) throws Exception {
		try (final OpenOfficeConnection openOfficeConnection = connectOpenOffice()) {
			Assertion.check().isTrue(inputFile.exists(), "Le document à convertir n''existe pas : {0}", inputFile.getAbsolutePath());
			final XComponent xDoc = loadDocument(inputFile, openOfficeConnection);
			try {
				refreshDocument(xDoc);
				LOGGER.debug("Document source chargé");

				final File targetFile = new TempFile("edition", '.' + targetFormat.name());
				storeDocument(targetFile, xDoc, targetFormat, openOfficeConnection);
				LOGGER.debug("Conversion réussie");
				return targetFile;
			} finally {
				xDoc.dispose();
			}
		}
	}

	/**
	 * Ecriture du document.
	 * @param outputFile Fichier de sortie
	 * @param xDoc Document OpenOffice source
	 * @param targetFormat Format de sortie
	 * @param openOfficeConnection Connection à OpenOffice
	 * @throws Exception Erreur de traitement
	 */
	protected abstract void storeDocument(final File outputFile, final XComponent xDoc, final ConverterFormat targetFormat, final OpenOfficeConnection openOfficeConnection) throws Exception;

	/**
	 * Lecture d'un docuement.
	 * @param inputFile Fichier source
	 * @param openOfficeConnection  Connection à OpenOffice
	 * @return Document OpenOffice
	 * @throws Exception Erreur de traitement
	 */
	protected abstract XComponent loadDocument(final File inputFile, final OpenOfficeConnection openOfficeConnection) throws Exception;

	private OpenOfficeConnection connectOpenOffice() throws IOException {
		final OpenOfficeConnection openOfficeConnection = new SocketOpenOfficeConnection(unoHost, unoPort);
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("connecting to OpenOffice.org on {}:{} ", unoHost, unoPort);
			}
			openOfficeConnection.connect(); //Attention déjà observé : connection ne s'établissant pas et pas de timeout
		} catch (final ConnectException connectException) {
			//On précise les causes possibles de l'erreur.
			final String msg = new StringBuilder()
					.append("Dans le fichier OOoBasePath\\Basis\\share\\registry\\data\\org\\openoffice\\Setup.xcu.\n")
					.append("Juste après cette ligne-ci : <node oor:name=\"Office\">\n")
					.append("Il faut ajouter les lignes suivantes :\n")
					.append("<prop oor:name=\"ooSetupConnectionURL\" oor:type=\"xs:string\">\n")
					.append("<value>socket,host=localhost,port=").append(unoPort).append(";urp;</value>\n")
					.append("</prop>\n")
					.append("Ensuite, il faut lancer OpenOffice... et l'agent OpenOffice si il tourne.")
					.toString();

			throw new IOException("Impossible de se connecter à OpenOffice, vérifier qu'il est bien en écoute sur " + unoHost + ":" + unoPort + ".\n\n" + msg, connectException);
		}
		return openOfficeConnection;
	}

	private static void refreshDocument(final XComponent document) {
		final XRefreshable refreshable = UnoRuntime.queryInterface(XRefreshable.class, document);
		if (refreshable != null) {
			refreshable.refresh();
		}
	}

	private static PropertyValue[] getFileProperties(final ConverterFormat docType, final XOutputStream outputStream, final XInputStream inputStream) {
		Assertion.check()
				.isNotNull(docType, "Le type du format de sortie est obligatoire")
				.isTrue(outputStream == null || inputStream == null, "Les properties pointent soit un fichier local, soit un flux d'entrée, soit un flux de sortie");
		final List<PropertyValue> fileProps = new ArrayList<>(3);

		PropertyValue fileProp = new PropertyValue();
		fileProp.Name = "Hidden";
		fileProp.Value = Boolean.TRUE;
		fileProps.add(fileProp);

		if (outputStream != null) {
			fileProp = new PropertyValue();
			fileProp.Name = "OutputStream";
			fileProp.Value = outputStream;
			fileProps.add(fileProp);
		} else if (inputStream != null) {
			fileProp = new PropertyValue();
			fileProp.Name = "InputStream";
			fileProp.Value = inputStream;
			fileProps.add(fileProp);
		}
		if (docType != ConverterFormat.ODT) {
			fileProp = new PropertyValue();
			fileProp.Name = "FilterName";
			fileProp.Value = getFilterNameFromExtension(docType);
			fileProps.add(fileProp);
		}
		return fileProps.toArray(new PropertyValue[fileProps.size()]);
	}

	/**
	 * Fournit les proterties de fichier pour un format de conversion.
	 * @param docType format
	 * @return Proterties
	 */
	protected static final PropertyValue[] getFileProperties(final ConverterFormat docType) {
		return getFileProperties(docType, null, null);
	}

	/**
	 * Fournit les proterties de fichier pour un format de conversion et un flux d'ecriture.
	 * @param docType docType format
	 * @param outputStream Flux d'ecriture
	 * @return Proterties
	 */
	protected static final PropertyValue[] getFileProperties(final ConverterFormat docType, final XOutputStream outputStream) {
		return getFileProperties(docType, outputStream, null);
	}

	/**
	 * Fournit les proterties de fichier pour un format de conversion et un flux de lecture.
	 * @param docType docType format
	 * @param inputStream Flux de lecture
	 * @return Proterties
	 */
	protected static final PropertyValue[] getFileProperties(final ConverterFormat docType, final XInputStream inputStream) {
		return getFileProperties(docType, null, inputStream);
	}

	/**
	 * @param docType Format de conversion
	 * @return filterName géré par OpenOffice pour lui préciser le format de conversion
	 */
	protected static final String getFilterNameFromExtension(final ConverterFormat docType) {
		//Liste des filterName géré par OpenOffice.
		//la liste est dans :
		//OO 3.3 "OpenOffice.org 3\Basis\share\registry\modules\org\openoffice\TypeDetection\Filter\fcfg_writer_filters.xcu"
		//OO 3.4 "OpenOffice.org 3\Basis\share\registry\writer.xcd"
		switch (docType) {
			case PDF:
				return "writer_pdf_Export";
			case RTF:
				return "Rich Text Format";
			case DOC:
				return "MS Word 97";
			case ODT:
				return "Open Document Format";
			case TXT:
				return "Text";
			//DOCX et CSV non géré
			default:
				throw new IllegalArgumentException("Type de document non géré : " + docType);
		}
	}
}
