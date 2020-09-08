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
package io.vertigo.vega.webservice.data.ws;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.FileAttachment;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.HeaderParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;

//bas� sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class FileDownloadWebServices implements WebServices {

	@Inject
	private ResourceManager resourcetManager;

	@GET("/downloadFile")
	public VFile testDownloadFile(final @QueryParam("id") Integer id) {
		final URL imageUrl = resourcetManager.resolve("npi2loup.png");
		final Path imageFile = asPath(imageUrl);
		final VFile imageVFile = FSFile.of("image" + id + ".png", "image/png", imageFile);
		return imageVFile;
	}

	@AnonymousAccessAllowed
	@GET("/downloadEmbeddedFile")
	@FileAttachment(false)
	public VFile testDownloadEmbeddedFile(final @QueryParam("id") Integer id) {
		return testDownloadFile(id);
	}

	@GET("/downloadNotModifiedFile")
	public VFile testDownloadNotModifiedFile(final @QueryParam("id") Integer id, final @HeaderParam("If-Modified-Since") Optional<Date> ifModifiedSince, final HttpServletResponse response) {
		final VFile imageFile = testDownloadFile(id);
		if (ifModifiedSince.isPresent() && imageFile.getLastModified().compareTo(Instant.ofEpochMilli(ifModifiedSince.get().getTime())) <= 0) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return null;
			//this service must declared VFile as return type because it should return VFile when file was modified
		}
		return imageFile;
	}

	@AnonymousAccessAllowed
	@GET("/downloadFileContentType")
	public VFile testDownloadFileContentType(final @QueryParam("id") Integer id) {
		final URL imageUrl = resourcetManager.resolve("npi2loup.png");
		final Path imageFile = asPath(imageUrl);
		return FSFile.of("image" + id + generateSpecialChars(id) + ".png", "image/png", imageFile);
	}

	private static String generateSpecialChars(final Integer id) {
		switch (id) {
			case 1:
				return "ÔÙæóñ";
			case 2:
				return "µ°«/";
			case 3:
				return "ÔÙæ óñµ°«/";
			case 4:
				return "€;_~";
			case 5:
				return "你好abcABCæøåÆØÅäöüïëêîâéíáóúýñ½§!#¤%&()=`@£$€{[]}+´¨^~'-_,;";
			default:
				return "";
		}
	}

	private static Path asPath(final URL url) {
		try {
			return Path.of(url.toURI());
		} catch (final URISyntaxException e) {
			return Path.of(url.getPath());
		}
	}
}
