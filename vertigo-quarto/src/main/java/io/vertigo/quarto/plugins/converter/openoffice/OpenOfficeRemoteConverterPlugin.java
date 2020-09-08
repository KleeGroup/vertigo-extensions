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
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XStorable;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;
import io.vertigo.quarto.plugins.converter.openoffice.stream.OOoFileInputStream;
import io.vertigo.quarto.plugins.converter.openoffice.stream.OOoFileOutputStream;

/**
 * Conversion des fichiers à partir de OpenOffice.
 * @author npiedeloup
 */
public final class OpenOfficeRemoteConverterPlugin extends AbstractOpenOfficeConverterPlugin {
	private static final Logger LOGGER = LogManager.getLogger(OpenOfficeRemoteConverterPlugin.class);

	/**
	 * Constructor.
	 * @param unoHost Hote du serveur OpenOffice
	 * @param unoPort Port de connexion au serveur OpenOffice
	 * @param convertTimeoutSecondsOpt Timeout de conversion des documents
	 */
	@Inject
	public OpenOfficeRemoteConverterPlugin(
			@ParamValue("unohost") final String unoHost,
			@ParamValue("unoport") final String unoPort,
			@ParamValue("convertTimeoutSeconds") final Optional<Integer> convertTimeoutSecondsOpt) {
		super(unoHost, unoPort, convertTimeoutSecondsOpt.orElse(60));
	}

	/** {@inheritDoc} */
	@Override
	protected void storeDocument(final File outputFile, final XComponent xDoc, final ConverterFormat targetFormat, final OpenOfficeConnection openOfficeConnection) throws Exception {
		final XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, xDoc);
		final XOutputStream outputStream = new OOoFileOutputStream(outputFile);
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Storing to {}", outputFile.getAbsolutePath());
			}
			final PropertyValue[] fileProps = getFileProperties(targetFormat, outputStream);
			xStorable.storeToURL("private:stream", fileProps);
		} finally {
			outputStream.closeOutput();
		}
	}

	/** {@inheritDoc} */
	@Override
	protected XComponent loadDocument(final File inputFile, final OpenOfficeConnection openOfficeConnection) throws Exception {
		final String inputUrl = inputFile.getAbsolutePath();

		final String inputExtensionStr = inputUrl.substring(inputUrl.lastIndexOf('.') + 1).toUpperCase(Locale.ENGLISH);
		final ConverterFormat docType = ConverterFormat.valueOf(inputExtensionStr);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Openning document... {}", inputUrl);
		}
		final XInputStream inputStream = new OOoFileInputStream(inputFile);
		try {
			final PropertyValue[] loadProps = getFileProperties(docType, inputStream);
			final XComponent xDoc = openOfficeConnection.getDesktop().loadComponentFromURL("private:stream", "_blank", 0, loadProps);
			//-----
			Assertion.check().isNotNull(xDoc, "Le document n''a pas été chargé : {0}", inputUrl);
			return xDoc;
		} finally {
			inputStream.closeInput();
		}

	}

}
