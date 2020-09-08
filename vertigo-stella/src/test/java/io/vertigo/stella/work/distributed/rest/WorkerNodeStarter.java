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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.util.ClassUtil;

/**
 * @author npiedeloup
 */
public class WorkerNodeStarter {
	private static final Logger LOG = LogManager.getLogger(WorkerNodeStarter.class);

	/**
	 * Lance l'environnement et attend indéfiniment.
	 * @param args "Usage: java vertigo.kernel.Starter managers.xml <conf.properties>"
	 * @throws InterruptedException Interrupt
	 */
	public static void main(final String[] args) throws InterruptedException {
		Assertion.check().isTrue(args.length >= 1 && args.length <= 2, "Usage WorkerNodeStarter managers.xml <maxLifeTime>");
		//-----
		final long timeToWait = args.length == 2 ? Long.parseLong(args[1]) * 1000L : 5 * 60 * 1000L;

		final String nodeConfigClassName = args[0];
		final StellaNodeConfigClientNode nodeConfigClientNode = ClassUtil.newInstance(ClassUtil.classForName(nodeConfigClassName, StellaNodeConfigClientNode.class));
		final NodeConfig nodeConfig = nodeConfigClientNode.getNodeConfig();

		LOG.info("Node starting");
		run(nodeConfig, timeToWait);
		LOG.info("Node stop");
	}

	private static void run(final NodeConfig nodeConfig, final long timeToWait) {
		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			System.out.println("Node started (timout in " + timeToWait / 1000 + "s)");
			if (timeToWait > 0) {
				Thread.sleep(timeToWait);
			} else {
				//infinite
				while (!Thread.currentThread().isInterrupted()) {
					Thread.sleep(60 * 1000);
				}
			}
			System.out.println("Node stopping by timeout");
		} catch (final InterruptedException e) {
			//rien arret normal
			System.out.println("Node stopping by interrupted");
		} catch (final Exception e) {
			System.err.println("Application error, exit " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}
