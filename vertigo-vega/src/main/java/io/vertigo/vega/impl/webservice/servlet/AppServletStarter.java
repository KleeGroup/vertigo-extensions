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
package io.vertigo.vega.impl.webservice.servlet;

import java.util.Properties;

import io.vertigo.core.node.config.LogConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.yaml.YamlNodeConfigBuilder;

/**
 * @author npiedeloup
 */
final class AppServletStarter extends AbstractAppServletStarter {

	/** {@inheritDoc} */
	@Override
	NodeConfig buildNodeConfig(final Properties bootConf) {
		final String logFileName;
		//si présent on récupère le paramétrage du fichier externe de paramétrage log4j
		if (bootConf.containsKey(LOG4J_CONFIGURATION_PARAM_NAME)) {
			logFileName = bootConf.getProperty(LOG4J_CONFIGURATION_PARAM_NAME);
			bootConf.remove(LOG4J_CONFIGURATION_PARAM_NAME);
		} else {
			logFileName = null;
		}

		final String configFileNames = bootConf.getProperty("boot.applicationConfiguration");
		final String[] configFileNamesSplit = configFileNames.split(";");
		bootConf.remove("boot.applicationConfiguration");
		//-----
		final YamlNodeConfigBuilder nodeConfigBuilder = new YamlNodeConfigBuilder(bootConf);
		if (logFileName != null) {
			nodeConfigBuilder.withLogConfig(new LogConfig(logFileName));
		}
		nodeConfigBuilder.withFiles(getClass(), configFileNamesSplit);

		// Initialisation de l'état de l'application
		return nodeConfigBuilder.build();
	}
}
