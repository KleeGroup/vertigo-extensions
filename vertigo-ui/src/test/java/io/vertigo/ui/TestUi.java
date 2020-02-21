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
package io.vertigo.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.web.SpringServletContainerInitializer;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;

public class TestUi {

	private static List<ContainerInitializer> springInitializers() {
		final SpringServletContainerInitializer sci = new SpringServletContainerInitializer();
		final ContainerInitializer initializer = new ContainerInitializer(sci, null);
		initializer.addApplicableTypeName(TestVSpringWebApplicationInitializer.class.getCanonicalName());
		final List<ContainerInitializer> initializers = new ArrayList<>();
		initializers.add(initializer);
		return initializers;
	}

	private static ClassLoader getUrlClassLoader() {
		return new URLClassLoader(new URL[0], TestUi.class.getClassLoader());
	}

	private static final int port = 18080;
	private final String baseUrl = "http://localhost:" + port;
	private static Server server;
	private static WebDriver driver;

	@BeforeAll
	public static void setUp() throws Exception {
		startServer();
		driver = new JBrowserDriver(Settings.builder()
				.timezone(Timezone.EUROPE_PARIS)
				.headless(true) //use false for debug purpose
				.build());
	}

	private static void startServer() throws IOException, Exception {
		server = new Server(port);
		final WebAppContext context = new WebAppContext(TestUi.class.getClassLoader().getResource("testWebApp/").getFile(), "/test");
		System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");
		context.setAttribute("jacoco.exclClassLoaders", "*");

		context.setAttribute("javax.servlet.context.tempdir", getScratchDir());
		context.setAttribute("org.eclipse.jetty.containerInitializers", springInitializers());
		context.addBean(new ServletContainerInitializersStarter(context), true);
		context.setClassLoader(getUrlClassLoader());
		context.setClassLoader(new WebAppClassLoader(TestUi.class.getClassLoader(), context));

		server.setHandler(context);
		server.start();
	}

	private static File getScratchDir() throws IOException {
		final File tempDir = new File(System.getProperty("java.io.tmpdir"));
		final File scratchDir = new File(tempDir.toString(), "embedded-jetty-html");

		if (!scratchDir.exists()) {
			if (!scratchDir.mkdirs()) {
				throw new IOException("Unable to create scratch directory: " + scratchDir);
			}
		}
		return scratchDir;
	}

	@AfterAll
	public static void tearDown() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	public static void main(final String[] args) throws Exception {
		startServer();
		server.join();
	}

	@Test
	public void testLoadLoginPage() {
		driver.get(baseUrl + "/test/");
		Assertions.assertEquals(baseUrl + "/test/", driver.getCurrentUrl());
	}

	@Test
	public void testMovies() throws InterruptedException {
		driver.get(baseUrl + "/test/movies/");
		Thread.sleep(5000);
	}

	@Test
	public void testMovieDetail() throws InterruptedException {
		driver.get(baseUrl + "/test/movie/1000");
		Thread.sleep(5000);
	}

	@Test
	public void testDemo() throws InterruptedException {
		driver.get(baseUrl + "/test/componentsDemo/");
		Thread.sleep(5000);
	}

	@Test
	public void testPostSimpleForm() throws InterruptedException {
		driver.get(baseUrl + "/test/componentsDemo/");

		assertEquals("Movie Information", waitElement(By.className("text-h6")).getText());
		findElement(By.name("vContext[movie][title]")).clear();
		findElement(By.name("vContext[movie][title]")).sendKeys("Test 1");
		findElement(By.name("vContext[movie][year]")).clear();
		findElement(By.name("vContext[movie][year]")).sendKeys("2020");
		findElement(By.id("saveAction")).click();

		assertEquals("Test 1", findElement(By.name("vContext[movie][title]")).getAttribute("value"));
		assertEquals("2020", findElement(By.name("vContext[movie][year]")).getAttribute("value"));
	}

	/* May be use in nexts tests
	private String getWebElementsAsString(final List<WebElement> webElements) {
		return webElements.stream()
				.map(WebElement::getText)
				.collect(Collectors.joining(", "));
	}
	*/
	private WebElement waitElement(final By byElement) throws InterruptedException {
		return waitElement(byElement, 1000);
	}

	private WebElement waitElement(final By byElement, final long timeout) throws InterruptedException {
		final long start = System.currentTimeMillis();
		do {
			try {
				if (isElementPresent(byElement)) {
					return driver.findElement(byElement);
				}
			} catch (final Exception e) {
				//do nothing
			}
			Thread.sleep(100);
		} while (System.currentTimeMillis() - start < timeout);
		System.out.println(driver.getPageSource());
		throw new AssertionError("Element non trouvé en " + timeout + "ms : " + byElement.toString());
	}

	private static boolean isElementPresent(final By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (final org.openqa.selenium.NoSuchElementException e) {
			return false;
		}
	}

	private static WebElement findElement(final By by) {
		try {
			return driver.findElement(by);
		} catch (final org.openqa.selenium.NoSuchElementException e) {
			System.out.println(driver.getPageSource());
			throw new NoSuchElementException(by.toString(), e);
		}
	}
}
