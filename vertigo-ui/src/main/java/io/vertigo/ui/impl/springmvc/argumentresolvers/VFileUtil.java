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
package io.vertigo.ui.impl.springmvc.argumentresolvers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.StreamFile;

/**
 * @author npiedeloup
 */
final class VFileUtil {

	private static final Logger LOG = LogManager.getLogger(VFileUtil.class);
	private static final String NOT_ALLOWED_IN_FILENAME = "\\/:*?\"<>|;";

	private VFileUtil() {
		//nothing
	}

	/**
	 * @param result WebService result
	 * @param response Response
	 */
	static void sendVFile(final VFile result, final HttpServletResponse response) {
		sendVFile(result, true, response);
	}

	private static void sendVFile(final VFile result, final boolean attachment, final HttpServletResponse response) {
		try {
			send(result, attachment, response);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error while sending file. <!-- {0} -->", e.getMessage());
		}
		// response already send
	}

	static VFile readQueryFile(final HttpServletRequest request, final String requestParamName) {
		try {
			Assertion.check()
					.isTrue(request.getContentType().contains("multipart/form-data"), "File {0} not found. Request contentType isn't \"multipart/form-data\"", requestParamName)
					.isFalse(request.getParts().isEmpty(),
							"File {0} not found. Request is multipart but there is no Parts. : Check you have defined MultipartConfig (example for Tomcat set allowCasualMultipartParsing=\"true\" on context tag in your context definition, for Jetty use JettyMultipartConfig)",
							requestParamName);
			final Part file = request.getPart(requestParamName);
			if (file == null) {
				final String sentParts = request.getParts()
						.stream()
						.map(Part::getName)
						.collect(Collectors.joining(", "));
				throw new IllegalArgumentException("File " + requestParamName + " not found. Parts sent : " + sentParts);
			}
			return createVFile(file);
		} catch (IOException | ServletException e) {
			throw WrappedException.wrap(e);
		}
	}

	private static void send(final VFile vFile, final boolean isAttachment, final HttpServletResponse response)
			throws IOException {
		final Long length = vFile.getLength();
		Assertion.check().isTrue(length < Integer.MAX_VALUE, "Too big file to be send. It's "
				+ length / 1024 + " Ko long, but maximum was " + Integer.MAX_VALUE / 1024
				+ " Ko.");
		response.setContentLength(length.intValue());
		response.addHeader("Content-Disposition", encodeFileNameToContentDisposition(vFile.getFileName(), isAttachment));
		response.addDateHeader("Last-Modified", vFile.getLastModified().toEpochMilli());
		response.addHeader("Cache-Control", "private");
		response.setContentType(vFile.getMimeType());

		try (final InputStream input = vFile.createInputStream()) {
			try (final OutputStream output = response.getOutputStream()) {
				copy(input, output);
			}
		}
	}

	/**
	 * Encode fileName according to RFC 5987.
	 * @param fileName String
	 * @param isAttachment boolean is Content an attachment
	 * @return String
	 */
	private static String encodeFileNameToContentDisposition(final String fileName, final boolean isAttachment) {
		if (fileName == null) {
			return "";
		}
		// on remplace par des espaces les caractères interdits dans les noms de fichiers : \ / : * ? " < > | ;
		final int notAllowedLength = NOT_ALLOWED_IN_FILENAME.length();
		String cleanFileName = fileName; //only accepted char
		for (int i = 0; i < notAllowedLength; i++) {
			cleanFileName = cleanFileName.replace(NOT_ALLOWED_IN_FILENAME.charAt(i), '_');
		}

		final int length = cleanFileName.length();
		final StringBuilder sb = new StringBuilder(length + length / 4);
		if (isAttachment) {
			sb.append("attachment;");
		}
		final String cleanestFileName = cleanFileName.replaceAll(" ", "%20"); //cleanest for default fileName
		sb.append("filename=\"").append(cleanestFileName).append('\"');
		byte[] utf8FileName;
		try {
			utf8FileName = cleanFileName.getBytes("utf8"); //Utf8 fileName
			sb.append(";filename*=UTF-8''");
			for (final byte c : utf8FileName) {
				if (c == '.' || c == '-' || c == '_' || isSimpleLetterOrDigit(c)) {
					sb.append((char) c);
				} else {
					sb.append('%');
					sb.append(String.format("%02X", c)); // we want byte as a char on one byte
				}
			}
		} catch (final UnsupportedEncodingException e) {
			LOG.warn("UnsupportedEncodingException UTF-8", e);
			//utf-8 unsupported we only use the filename= header
		}
		return sb.toString();
	}

	/**
	 * Copie le contenu d'un flux d'entrée vers un flux de sortie.
	 * @param in flux d'entrée
	 * @param out flux de sortie
	 * @throws IOException Erreur d'entrée/sortie
	 */
	private static void copy(final InputStream in, final OutputStream out) throws IOException {
		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read = in.read(bytes);
		while (read != -1) {
			out.write(bytes, 0, read);
			read = in.read(bytes);
		}
	}

	private static boolean isSimpleLetterOrDigit(final byte c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}

	private static VFile createVFile(final Part file) {
		final String fileName = getSubmittedFileName(file);
		String mimeType = file.getContentType();
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		return StreamFile.of(fileName, mimeType, Instant.now(), file.getSize(), new FileInputStreamBuilder(file));
	}

	private static String getSubmittedFileName(final Part filePart) {
		final String header = filePart.getHeader("content-disposition");
		if (header == null) {
			return null;
		}
		for (final String headerPart : header.split(";")) {
			if (headerPart.trim().startsWith("filename")) {
				return headerPart.substring(headerPart.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

	private static final class FileInputStreamBuilder implements InputStreamBuilder {
		private final Part file;

		FileInputStreamBuilder(final Part file) {
			this.file = file;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return file.getInputStream();
		}
	}

}
