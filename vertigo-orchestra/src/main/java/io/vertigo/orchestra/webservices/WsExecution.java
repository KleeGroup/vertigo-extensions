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
package io.vertigo.orchestra.webservices;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.orchestra.services.report.ActivityExecution;
import io.vertigo.orchestra.services.report.ExecutionSummary;
import io.vertigo.orchestra.services.report.ProcessExecution;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;

/**
 * WebService API for managing Executions
 * @author mlaroche.
 * @version $Id$
 */
@PathPrefix("/orchestra/executions")
public class WsExecution implements WebServices {

	private static final Integer DEFAULT_PAGE_SIZE = 50;
	private static final Integer DEFAULT_OFFSET = 0;

	private static final Integer WEEK_DAYS = 7;

	@Inject
	private OrchestraDefinitionManager orchestraDefinitionManager;
	@Inject
	private OrchestraServices orchestraServices;

	/**
	 * Retourne la liste des executions d'un processus répondant à des critères triés par ordre chronologique décroissant
	 * @param processName l'id du processus concerné par la requête
	 * @param status le status des executions à retourner
	 * @param limit le nombre de resultat souhaités
	 * @param offset le rang du premier résultat retourné
	 * @return la liste des éxécutions répondant aux critères
	 */
	@GET("/")
	public List<ProcessExecution> getProcessExecutionsByProcessName(@QueryParam("processName") final String processName, @QueryParam("status") final Optional<String> status,
			@QueryParam("limit") final Optional<Integer> limit, @QueryParam("offset") final Optional<Integer> offset) {
		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition(processName);
		return orchestraServices.getReport()
				.getProcessExecutions(processDefinition, status.orElse(""), limit.orElse(DEFAULT_PAGE_SIZE), offset.orElse(DEFAULT_OFFSET));
	}

	/**
	 * Retourne une execution de processus.
	 * @param preId l'id de l'execution
	 * @return l'execution
	 */
	@GET("/{preId}")
	public ProcessExecution getProcessExecutionById(@PathParam("preId") final Long preId) {
		return orchestraServices.getReport()
				.getProcessExecution(preId);
	}

	/**
	 * Récupère la liste des activités d'une execution de processus.
	 * @param preId l'id de l'exécution
	 * @return la liste des activités associées
	 */
	@GET("/{preId}/activities")
	public List<ActivityExecution> getActivityExecutionsByPreId(@PathParam("preId") final Long preId) {
		return orchestraServices.getReport()
				.getActivityExecutionsByProcessExecution(preId);
	}

	/**
	 * Récupère le fichier de log d'une execution de processus
	 * @param preId l'id de l'exécution
	 * @return le fichier de log
	 */
	@GET("/{preId}/logFile")
	public VFile getLogFileByPreId(@PathParam("preId") final Long preId) {
		return orchestraServices.getLogger()
				.getLogFileForProcess(preId).get();
	}

	/**
	 * Retourne une execution d'activité par son id.
	 * @param aceId l'id de l'execution d'activité
	 * @return l'activité
	 */
	@GET("/{preId}/activities/{aceId}")
	public ActivityExecution getActivityExecutionById(@PathParam("aceId") final Long aceId, @PathParam("preId") final Long preId) {
		return orchestraServices.getReport()
				.getActivityExecution(aceId);
	}

	/**
	 * Récupère le fichier de log d'une execution d'activité
	 * @param aceId l'id de l'exécution
	 * @return le fichier de log
	 */
	@GET("/{preId}/activities/{aceId}/attachment")
	public VFile getLogFileByAceId(@PathParam("aceId") final Long aceId, @PathParam("preId") final Long preId) {
		return orchestraServices.getLogger()
				.getActivityAttachment(aceId).get();
	}

	/**
	 * Récupère le fichier de log d'une execution d'activité
	 * @param aceId l'id de l'exécution
	 * @return le fichier de log
	 */
	@GET("/{preId}/activities/{aceId}/logFile")
	public VFile getTechnicalLogFileByAceId(@PathParam("aceId") final Long aceId, @PathParam("preId") final Long preId) {
		return orchestraServices.getLogger()
				.getActivityLogFile(aceId).get();
	}

	/**
	 * Retourne le rapport d'execution d'un processus sur une période.
	 * @param processName l'id du processus
	 * @return le résumé
	 */
	@GET("/summaries/{processName}")
	public ExecutionSummary getWeekSummaryByProcessName(@PathParam("processName") final String processName) {
		final ProcessDefinition processDefinition = orchestraDefinitionManager.getProcessDefinition(processName);
		final Calendar firstDayOfWeek = getFirstDayOfWeek();
		return orchestraServices.getReport()
				.getSummaryByDate(processDefinition, firstDayOfWeek.toInstant(), getFirstDayOfNextWeekDate(firstDayOfWeek));
	}

	/**
	 * Retourne le rapport d'execution d'orchestra de la semaine courante.
	 * @param weekOffset le décalage de semaine (-1 semaine dernière etc...)
	 * @param status permet de filtrer sur un état d'execution (par exemple voir les processus qui ont eu une execution en erreur sur la période.
	 * @return la liste de résumés répondant aux critères
	 */
	@GET("/summaries")
	public List<ExecutionSummary> getWeekSummaries(@QueryParam("weekOffset") final int weekOffset, @QueryParam("status") final Optional<String> status) {
		// We take the first day of the current week
		final Calendar firstDayOfWeek = getFirstDayOfWeek();
		// We deal with the offset
		firstDayOfWeek.add(Calendar.DAY_OF_YEAR, weekOffset * WEEK_DAYS);
		// We make the call with the proper week dates
		return orchestraServices.getReport()
				.getSummariesByDate(firstDayOfWeek.toInstant(), getFirstDayOfNextWeekDate(firstDayOfWeek), status);
	}

	private static Instant getFirstDayOfNextWeekDate(final Calendar first) {
		// and add seven days to the end date
		final Calendar last = (Calendar) first.clone();
		last.add(Calendar.DAY_OF_YEAR, WEEK_DAYS);

		return last.toInstant();
	}

	private static Calendar getFirstDayOfWeek() {
		final Calendar cal = new GregorianCalendar(Locale.FRANCE);
		// "calculate" the start date of the week
		final Calendar first = (Calendar) cal.clone();
		first.set(Calendar.DAY_OF_WEEK, first.getFirstDayOfWeek());
		first.set(Calendar.HOUR_OF_DAY, 0);
		first.set(Calendar.MINUTE, 0);
		first.set(Calendar.SECOND, 0);

		first.set(Calendar.MILLISECOND, 0);

		return first;

	}
}
