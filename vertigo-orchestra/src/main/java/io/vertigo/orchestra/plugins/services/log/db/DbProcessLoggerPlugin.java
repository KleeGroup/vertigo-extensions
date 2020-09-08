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
package io.vertigo.orchestra.plugins.services.log.db;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamManager;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.model.FSFile;
import io.vertigo.datastore.impl.filestore.model.StreamFile;
import io.vertigo.orchestra.dao.execution.OActivityLogDAO;
import io.vertigo.orchestra.domain.execution.OActivityLog;
import io.vertigo.orchestra.impl.services.ProcessLoggerPlugin;

/**
 * Récupération des logs en BDD.
 * @author mlaroche
 *
 */
@Transactional
public class DbProcessLoggerPlugin implements ProcessLoggerPlugin {

	private static final String ROOT_DIRECTORY = "orchestra.root.directory";
	private static final String TECHNICAL_LOG_PREFIX = "technicalLog_";
	private static final String TECHNICAL_LOG_EXTENSION = ".log";
	private static final String TECHNICAL_LOG_MIMETYPE = "text/plain";

	@Inject
	private OActivityLogDAO activityLogDAO;
	@Inject
	private ParamManager paramManager;

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getLogFileForProcess(final Long processExecutionId) {
		Assertion.check().isNotNull(processExecutionId);
		// ---
		final Optional<OActivityLog> activityLogOpt = activityLogDAO.getLogByPreId(processExecutionId);
		return getLogFileFromActivityLog(activityLogOpt);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getActivityAttachment(final Long actityExecutionId) {
		Assertion.check().isNotNull(actityExecutionId);
		// ---
		final Optional<OActivityLog> activityLogOpt = activityLogDAO.getActivityLogByAceId(actityExecutionId);
		return getLogFileFromActivityLog(activityLogOpt);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getActivityLogFile(final Long actityExecutionId) {
		Assertion.check().isNotNull(actityExecutionId);
		// ---
		return activityLogDAO.getActivityLogByAceId(actityExecutionId)
				.map(activityLog -> {
					final byte[] stringByteArray = activityLog.getLog().getBytes(StandardCharsets.UTF_8);
					final InputStreamBuilder inputStreamBuilder = () -> new ByteArrayInputStream(stringByteArray);
					final String fileName = TECHNICAL_LOG_PREFIX + actityExecutionId + TECHNICAL_LOG_EXTENSION;
					return StreamFile.of(fileName, TECHNICAL_LOG_MIMETYPE, Instant.now(), stringByteArray.length, inputStreamBuilder);
				});
	}

	private Optional<VFile> getLogFileFromActivityLog(final Optional<OActivityLog> activityLogOpt) {
		Assertion.check().isNotNull(activityLogOpt);
		// ---
		return activityLogOpt
				.map(activityLog -> {
					final Path file = Path.of(paramManager.getParam(ROOT_DIRECTORY).getValueAsString(), activityLogOpt.get().getAttachment());
					if (!Files.exists(file)) {
						throw new IllegalArgumentException("Log File" + file + " not found");
					}
					return FSFile.of(file);
				});
	}
}
