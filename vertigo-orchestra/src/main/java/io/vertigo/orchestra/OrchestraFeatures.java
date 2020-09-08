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
package io.vertigo.orchestra;

import java.util.stream.Stream;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.orchestra.dao.definition.DefinitionPAO;
import io.vertigo.orchestra.dao.definition.OActivityDAO;
import io.vertigo.orchestra.dao.definition.OProcessDAO;
import io.vertigo.orchestra.dao.execution.ExecutionPAO;
import io.vertigo.orchestra.dao.execution.OActivityExecutionDAO;
import io.vertigo.orchestra.dao.execution.OActivityLogDAO;
import io.vertigo.orchestra.dao.execution.OActivityWorkspaceDAO;
import io.vertigo.orchestra.dao.execution.ONodeDAO;
import io.vertigo.orchestra.dao.execution.OProcessExecutionDAO;
import io.vertigo.orchestra.dao.planification.OProcessPlanificationDAO;
import io.vertigo.orchestra.dao.planification.PlanificationPAO;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.OrchestraSmartTypes;
import io.vertigo.orchestra.domain.DtDefinitions;
import io.vertigo.orchestra.impl.definitions.OrchestraDefinitionManagerImpl;
import io.vertigo.orchestra.impl.node.ONodeManager;
import io.vertigo.orchestra.impl.node.ONodeManagerImpl;
import io.vertigo.orchestra.impl.services.OrchestraServicesImpl;
import io.vertigo.orchestra.monitoring.dao.summary.SummaryPAO;
import io.vertigo.orchestra.monitoring.dao.uidefinitions.UidefinitionsPAO;
import io.vertigo.orchestra.monitoring.dao.uiexecutions.UiexecutionsPAO;
import io.vertigo.orchestra.plugins.definitions.db.DbProcessDefinitionStorePlugin;
import io.vertigo.orchestra.plugins.definitions.memory.MemoryProcessDefinitionStorePlugin;
import io.vertigo.orchestra.plugins.services.execution.db.DbProcessExecutorPlugin;
import io.vertigo.orchestra.plugins.services.execution.memory.MemoryProcessExecutorPlugin;
import io.vertigo.orchestra.plugins.services.log.db.DbProcessLoggerPlugin;
import io.vertigo.orchestra.plugins.services.report.db.DbProcessReportPlugin;
import io.vertigo.orchestra.plugins.services.schedule.db.DbProcessSchedulerPlugin;
import io.vertigo.orchestra.plugins.services.schedule.memory.MemoryProcessSchedulerPlugin;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.orchestra.webservices.WsDefinition;
import io.vertigo.orchestra.webservices.WsExecution;
import io.vertigo.orchestra.webservices.WsExecutionControl;
import io.vertigo.orchestra.webservices.WsInfos;

/**
 * Defines extension orchestra.
 * @author pchretien
 */
public final class OrchestraFeatures extends Features<OrchestraFeatures> {

	/**
	 * Constructeur de la feature.
	 */
	public OrchestraFeatures() {
		super("vertigo-orchestra");
	}

	private static Param findParamByName(final String paramName, final Param[] params) {
		Assertion.check().isNotBlank(paramName);
		//---
		return Stream.of(params)
				.filter(param -> paramName.equals(param.getName()))
				.findFirst()
				.orElseThrow(() -> new VSystemException("param '{0}' not found in params '{1}' ", paramName, params));

	}

	/**
	 * Activate Orchestra with Database.
	 * @param nodeName the name of the node
	 * @param daemonPeriodSeconds the period for scheduling and execution
	 * @param workersCount the number of workers
	 * @param forecastDurationSeconds the time to forecast planifications
	 * @return these features
	 */
	@Feature("orchestra.database")
	public OrchestraFeatures withDataBase(final Param... params) {
		final Param nodeName = findParamByName("nodeName", params);
		final Param daemonPeriodSeconds = findParamByName("daemonPeriodSeconds", params);
		final Param workersCount = findParamByName("workersCount", params);
		final Param forecastDurationSeconds = findParamByName("forecastDurationSeconds", params);

		getModuleConfigBuilder()
				.addComponent(ONodeManager.class, ONodeManagerImpl.class)
				.addPlugin(DbProcessDefinitionStorePlugin.class)
				.addPlugin(DbProcessSchedulerPlugin.class,
						nodeName,
						Param.of("planningPeriodSeconds", daemonPeriodSeconds.getValueAsString()),
						forecastDurationSeconds)
				.addPlugin(DbProcessExecutorPlugin.class,
						nodeName,
						workersCount,
						Param.of("executionPeriodSeconds", daemonPeriodSeconds.getValueAsString()))
				.addPlugin(DbProcessReportPlugin.class)
				.addPlugin(DbProcessLoggerPlugin.class)
				//----DAO
				.addComponent(OProcessDAO.class)
				.addComponent(OActivityDAO.class)
				.addComponent(OProcessPlanificationDAO.class)
				.addComponent(OActivityExecutionDAO.class)
				.addComponent(OProcessExecutionDAO.class)
				.addComponent(OActivityWorkspaceDAO.class)
				.addComponent(OActivityLogDAO.class)
				.addComponent(ONodeDAO.class)
				//----PAO
				.addComponent(DefinitionPAO.class)
				.addComponent(ExecutionPAO.class)
				.addComponent(PlanificationPAO.class)
				.addComponent(UidefinitionsPAO.class)
				.addComponent(UiexecutionsPAO.class)
				.addComponent(SummaryPAO.class)
				//----Definitions
				.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
						.addDefinitionResource("smarttypes", OrchestraSmartTypes.class.getName())
						.addDefinitionResource("dtobjects", DtDefinitions.class.getName())
						.build());
		return this;
	}

	/**
	 * Activate Orchestra with Memory.
	 * @param workersCount the number of workers
	 * @return these features
	 */
	@Feature("orchestra.memory")
	public OrchestraFeatures withMemory(final Param... params) {
		final Param workersCount = findParamByName("workersCount", params);
		getModuleConfigBuilder()
				.addPlugin(MemoryProcessDefinitionStorePlugin.class)
				.addPlugin(MemoryProcessSchedulerPlugin.class)
				.addPlugin(MemoryProcessExecutorPlugin.class,
						workersCount);

		return this;
	}

	/**
	 * Activate Orchestra's REST WebServices.
	 * @return these features
	 */
	@Feature("orchestra.webapi")
	public OrchestraFeatures withWebApi() {
		getModuleConfigBuilder()
				.addComponent(WsDefinition.class)
				.addComponent(WsExecution.class)
				.addComponent(WsExecutionControl.class)
				.addComponent(WsInfos.class);

		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(OrchestraDefinitionManager.class, OrchestraDefinitionManagerImpl.class)
				.addComponent(OrchestraServices.class, OrchestraServicesImpl.class);

	}
}
