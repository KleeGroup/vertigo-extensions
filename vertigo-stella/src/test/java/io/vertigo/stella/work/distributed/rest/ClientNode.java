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
package io.vertigo.stella.work.distributed.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import io.vertigo.core.lang.Assertion;

/**
 * @author npiedeloup
 */
final class ClientNode {
	private Process nodeProcess;
	private final List<Thread> subThreads = new ArrayList<>();
	private final int maxLifeTime;

	private final String nodeConfigClassName;

	/**
	 * Constructeur.
	 * @param nodeId Node id : 1 or 2
	 * @param maxLifeTime Durée de vie max en seconde
	 */
	ClientNode(final int nodeId, final int maxLifeTime) {
		Assertion.check()
				.isTrue(nodeId == 1 || nodeId == 2, "You must specified nodeId : 1 or 2")
				.isTrue(maxLifeTime >= 0 && maxLifeTime < 30000, "MaxLifeTime is in seconde and must be less than 30000 ({0}). Use 0 if you need infinit life.", maxLifeTime);
		//-----
		this.maxLifeTime = maxLifeTime;
		nodeConfigClassName = StellaNodeConfigClientNode.class.getName() + nodeId;
	}

	public void start() throws IOException {
		final String command = new StringBuilder()
				.append("java -cp ")
				.append(properSystemPath(System.getProperty("java.class.path")))
				.append(" io.vertigo.stella.work.distributed.rest.WorkerNodeStarter " + nodeConfigClassName + " " + maxLifeTime)
				.toString();
		nodeProcess = Runtime.getRuntime().exec(command);
		subThreads.add(createOutputFlusher(nodeProcess.getInputStream(), "[ClientNode] ", System.out));
		subThreads.add(createOutputFlusher(nodeProcess.getErrorStream(), "[ClientNode-err] ", System.err));
		for (final Thread subThread : subThreads) {
			subThread.setDaemon(true);
			subThread.start();
		}
	}

	private static String properSystemPath(final String path) {
		Assertion.check().isNotNull(path);
		//---
		return path.replaceAll("([^;]+);([^;]+)", "\"$1\";\"$2\"");
	}

	public void stop() throws InterruptedException {
		nodeProcess.destroy();
		nodeProcess.waitFor();
		LogManager.getLogger(ClientNode.class).info("ClientNode stopped");
		for (final Thread subThread : subThreads) {
			subThread.interrupt();
		}
	}

	private static Thread createOutputFlusher(final InputStream inputStream, final String prefix, final PrintStream out) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try (final InputStreamReader isr = new InputStreamReader(inputStream)) {
					try (final BufferedReader br = new BufferedReader(isr)) {
						String line;
						out.println(prefix + " Start outputFlusher");
						while (!Thread.interrupted()) {
							while ((line = br.readLine()) != null) {
								out.println(prefix + line);
							}
							Thread.sleep(50);
						}
					}
				} catch (final InterruptedException | IOException e) {
					return;
				}
			}
		}, "ClientNodeOutputFlusher");
	}
}
