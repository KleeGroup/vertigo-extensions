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
package io.vertigo.quarto.impl.converter;

/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Quarto FileTypeDetector to detect specific quarto type, OS independant.
 * Inspired by github.com/jeantil/jdk7-mimeutils.
 */
public class MimeTypesFileTypeDetector extends FileTypeDetector {
	private static final String[][] CONVERTER_FORMAT = {
			{ ".XLS", "application/vnd.ms-excel" },
			{ ".DOC", "application/msword" },
			{ ".DOCX", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
			{ ".XLSX", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
			{ ".RTF", "text/rtf" },
			{ ".TXT", "text/plain" },
			{ ".CSV", "application/csv.ms-excel" },
			{ ".PDF", "application/pdf" },
			{ ".ODS", "application/vnd.oasis.opendocument.spreadsheet" },
			{ ".ODT", "application/vnd.oasis.opendocument.text" },
	};

	// map of extension to MIME type
	private final Map<String, String> mimeTypeMap = new HashMap<>();

	public MimeTypesFileTypeDetector() {
		for (final String[] format : CONVERTER_FORMAT) {
			mimeTypeMap.put(format[0].toUpperCase(Locale.ROOT), format[1]);
		}
	}

	@Override
	public String probeContentType(final Path path) throws IOException {
		final Path fn = path.getFileName();
		if (fn == null) {
			return null; // no file name
		}

		final String ext = getUpperCaseExtension(fn.toString());
		if (ext.isEmpty()) {
			return null; // no extension
		}

		if (mimeTypeMap.isEmpty()) {
			return null;
		}

		// Case-unsensitive search
		return mimeTypeMap.get(ext);
	}

	// Get the extension of a file name.
	private static String getUpperCaseExtension(final String name) {
		String ext = "";
		if (!name.isEmpty()) {
			final int dot = name.lastIndexOf('.');
			if (dot >= 0) {
				ext = name.substring(dot).toUpperCase(Locale.ROOT);
			}
		}
		return ext;
	}
}
