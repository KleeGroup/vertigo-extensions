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
package io.vertigo.orchestra.plugins.services.report.db;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.lang.Assertion;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;
import io.vertigo.orchestra.impl.services.ProcessReportPlugin;
import io.vertigo.orchestra.monitoring.dao.summary.SummaryPAO;
import io.vertigo.orchestra.monitoring.dao.uiexecutions.UiexecutionsPAO;
import io.vertigo.orchestra.monitoring.domain.summary.OExecutionSummary;
import io.vertigo.orchestra.monitoring.domain.uiexecutions.OActivityExecutionUi;
import io.vertigo.orchestra.monitoring.domain.uiexecutions.OProcessExecutionUi;
import io.vertigo.orchestra.services.report.ActivityExecution;
import io.vertigo.orchestra.services.report.ExecutionSummary;
import io.vertigo.orchestra.services.report.ProcessExecution;

/**
 * Récupération des reporting d'execution en BDD.
 * @author mlaroche
 *
 */
@Transactional
public class DbProcessReportPlugin implements ProcessReportPlugin {

	@Inject
	private UiexecutionsPAO uiexecutionsPAO;
	@Inject
	private SummaryPAO summaryPAO;

	private static void checkProcessDefinition(final ProcessDefinition processDefinition) {
		Assertion.check()
				.isNotNull(processDefinition)
				.isTrue(processDefinition.getProcessType() == ProcessType.SUPERVISED, "Only supervised process can retrieve executions. Process {0} isn't", processDefinition.getName());
	}

	@Override
	public List<ProcessExecution> getProcessExecutions(final ProcessDefinition processDefinition, final String status, final Integer limit, final Integer offset) {
		checkProcessDefinition(processDefinition);
		//---
		return decodeExecutionList(uiexecutionsPAO.getExecutionsByProcessName(processDefinition.getName(), status, limit, offset));
	}

	@Override
	public List<ExecutionSummary> getSummariesByDate(final Instant minDate, final Instant maxDate, final Optional<String> status) {
		return decodeSummaryList(summaryPAO.getExecutionSummariesByDate(minDate, maxDate, status.orElse(null)));
	}

	@Override
	public ExecutionSummary getSummaryByDate(final ProcessDefinition processDefinition, final Instant minDate, final Instant maxDate) {
		checkProcessDefinition(processDefinition);
		//---
		return decodeSummary(summaryPAO.getExecutionSummaryByDateAndName(minDate, maxDate, processDefinition.getName()));
	}

	@Override
	public ProcessExecution getProcessExecution(final Long preId) {
		return decodeExecution(uiexecutionsPAO.getExecutionByPreId(preId));
	}

	@Override
	public List<ActivityExecution> getActivityExecutionsByProcessExecution(final Long preId) {
		return decodeActivityExecutionList(uiexecutionsPAO.getActivitiesByPreId(preId));
	}

	@Override
	public ActivityExecution getActivityExecution(final Long aceId) {
		return decodeActivityExecution(uiexecutionsPAO.getActivitiyByAceId(aceId));
	}

	// Utilitaire de transformation//
	private static ExecutionSummary decodeSummary(final OExecutionSummary summary) {
		return new ExecutionSummary(
				summary.getProId(),
				summary.getProcessName(),
				summary.getProcessLabel(),
				summary.getLastExecutionTime(),
				summary.getNextExecutionTime(),
				summary.getErrorsCount(),
				summary.getMisfiredCount(),
				summary.getSuccessfulCount(),
				summary.getRunningCount(),
				summary.getAverageExecutionTime(),
				summary.getHealth());
	}

	private static List<ExecutionSummary> decodeSummaryList(final List<OExecutionSummary> summaries) {
		return summaries
				.stream()
				.map(DbProcessReportPlugin::decodeSummary)
				.collect(Collectors.toList());

	}

	private static ProcessExecution decodeExecution(final OProcessExecutionUi execution) {
		return new ProcessExecution(
				execution.getPreId(),
				execution.getBeginTime(),
				execution.getEndTime(),
				execution.getExecutionTime(),
				execution.getStatus(),
				execution.getChecked(),
				execution.getCheckingDate(),
				execution.getCheckingComment(),
				execution.getHasAttachment());
	}

	private static List<ProcessExecution> decodeExecutionList(final List<OProcessExecutionUi> executions) {
		return executions
				.stream()
				.map(DbProcessReportPlugin::decodeExecution)
				.collect(Collectors.toList());

	}

	private static ActivityExecution decodeActivityExecution(final OActivityExecutionUi execution) {
		return new ActivityExecution(
				execution.getAceId(),
				execution.getLabel(),
				execution.getBeginTime(),
				execution.getEndTime(),
				execution.getExecutionTime(),
				execution.getStatus(),
				execution.getWorkspaceIn(),
				execution.getWorkspaceOut(),
				execution.getHasAttachment(),
				execution.getHasTechnicalLog());
	}

	private static List<ActivityExecution> decodeActivityExecutionList(final List<OActivityExecutionUi> executions) {
		return executions
				.stream()
				.map(DbProcessReportPlugin::decodeActivityExecution)
				.collect(Collectors.toList());

	}

}
