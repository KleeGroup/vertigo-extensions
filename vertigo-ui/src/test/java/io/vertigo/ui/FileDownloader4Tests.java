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
package io.vertigo.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import io.vertigo.core.util.TempFile;

public class FileDownloader4Tests {

	private static final Logger LOG = LogManager.getLogger(FileDownloader4Tests.class);
	private final WebDriver driver;
	private boolean followRedirects = true;
	private boolean mimicWebDriverCookieState = true;
	private int httpStatusOfLastDownloadAttempt = 0;

	/**
	 * Constructor.
	 * @param driverObject Selenium's WebDriver
	 */
	public FileDownloader4Tests(final WebDriver driverObject) {
		driver = driverObject;
	}

	/**
	 * Specify if the FileDownloader class should follow redirects when trying to download a file
	 *
	 * @param value
	 */
	public void followRedirectsWhenDownloading(final boolean value) {
		followRedirects = value;
	}

	/**
	 * Download the file specified in the href attribute of a WebElement
	 *
	 * @param element
	 * @return downloadedFileAbsolutePath
	 * @throws Exception
	 */
	public String downloadFile(final WebElement element) throws Exception {
		return downloader(element, "href");
	}

	/**
	 * Download the image specified in the src attribute of a WebElement
	 *
	 * @param element
	 * @return downloadedFileAbsolutePath
	 * @throws Exception
	 */
	public String downloadImage(final WebElement element) throws Exception {
		return downloader(element, "src");
	}

	/**
	 * Gets the HTTP status code of the last download file attempt.	 *
	 * @return Last HTTP status code
	 */
	public int getHTTPStatusOfLastDownloadAttempt() {
		return httpStatusOfLastDownloadAttempt;
	}

	/**
	 * Mimic the cookie state of WebDriver (Defaults to true)
	 * This will enable you to access files that are only available when logged in.
	 * If set to false the connection will be made as an anonymouse user
	 *
	 * @param value
	 */
	public void mimicWebDriverCookieState(final boolean value) {
		mimicWebDriverCookieState = value;
	}

	/**
	 * Load in all the cookies WebDriver currently knows about so that we can mimic the browser cookie state
	 *
	 * @param seleniumCookieSet
	 * @return BasicCookieStore
	 */
	private static BasicCookieStore mimicCookieState(final Set<Cookie> seleniumCookieSet) {
		final BasicCookieStore mimicWebDriverCookieStore = new BasicCookieStore();
		for (final Cookie seleniumCookie : seleniumCookieSet) {
			final BasicClientCookie duplicateCookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
			duplicateCookie.setDomain(seleniumCookie.getDomain());
			duplicateCookie.setSecure(seleniumCookie.isSecure());
			duplicateCookie.setExpiryDate(seleniumCookie.getExpiry());
			duplicateCookie.setPath(seleniumCookie.getPath());
			mimicWebDriverCookieStore.addCookie(duplicateCookie);
		}

		return mimicWebDriverCookieStore;
	}

	/**
	 * Perform the file/image download.
	 *
	 * @param element
	 * @param attribute
	 * @return downloadedFileAbsolutePath
	 * @throws IOException
	 * @throws NullPointerException
	 */
	private String downloader(final WebElement element, final String attribute) throws IOException, NullPointerException, URISyntaxException {
		final String fileToDownloadLocation = element.getAttribute(attribute);
		if (fileToDownloadLocation.trim().equals("")) {
			throw new NullPointerException("The element you have specified does not link to anything!");
		}

		final URL fileToDownload = new URL(fileToDownloadLocation);
		final File downloadedFile = new TempFile("test", ".tmp");
		if (downloadedFile.canWrite() == false) {
			downloadedFile.setWritable(true);
		}

		try (final CloseableHttpClient client = HttpClientBuilder.create().build()) {
			final BasicHttpContext localContext = new BasicHttpContext();

			LOG.info("Mimic WebDriver cookie state: " + mimicWebDriverCookieState);
			if (mimicWebDriverCookieState) {
				localContext.setAttribute(ClientContext.COOKIE_STORE, mimicCookieState(driver.manage().getCookies()));
			}

			final HttpGet httpget = new HttpGet(fileToDownload.toURI());
			final HttpParams httpRequestParameters = httpget.getParams();
			httpRequestParameters.setParameter(ClientPNames.HANDLE_REDIRECTS, followRedirects);
			httpget.setParams(httpRequestParameters);

			LOG.info("Sending GET request for: " + httpget.getURI());
			try (final CloseableHttpResponse response = client.execute(httpget, localContext)) {
				httpStatusOfLastDownloadAttempt = response.getStatusLine().getStatusCode();
				LOG.info("HTTP GET request status: " + httpStatusOfLastDownloadAttempt);
				LOG.info("Downloading file: " + downloadedFile.getName());
				FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
				//response.getEntity().getContent().close();
			}
		}

		final String downloadedFileAbsolutePath = downloadedFile.getAbsolutePath();
		LOG.info("File downloaded to '" + downloadedFileAbsolutePath + "'");

		return downloadedFileAbsolutePath;
	}

}
