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
package io.vertigo.datastore.plugins.filestore.fs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.commons.transaction.VTransaction;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.FileUtil;
import io.vertigo.datastore.filestore.definitions.FileInfoDefinition;
import io.vertigo.datastore.filestore.model.FileInfo;
import io.vertigo.datastore.filestore.model.FileInfoURI;
import io.vertigo.datastore.filestore.model.InputStreamBuilder;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.datastore.impl.filestore.FileStorePlugin;
import io.vertigo.datastore.impl.filestore.model.AbstractFileInfo;
import io.vertigo.datastore.impl.filestore.model.StreamFile;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup, skerdudou
 */
public final class FsFullFileStorePlugin implements FileStorePlugin {

	private static final Logger LOG = LogManager.getLogger(FsFullFileStorePlugin.class);

	private static final Pattern URI_AS_PATH_PATTERN = Pattern.compile("^([0-9]{4})([0-9]{2})([0-9]{2})-");
	private static final String METADATA_SUFFIX = ".info";
	private static final String METADATA_CHARSET = "utf8";
	private static final String DEFAULT_STORE_NAME = "temp";
	private static final String INFOS_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private final String name;
	private final Path documentRoot;
	private final VTransactionManager transactionManager;
	private final Optional<Integer> purgeDelayMinutesOpt;

	private final String fileInfoClassName;

	/**
	 * Constructor.
	 * @param name Store name
	 * @param path Root directory
	 * @param transactionManager Transaction manager
	 * @param purgeDelayMinutesOpt purge files older than this delay
	 */
	@Inject
	public FsFullFileStorePlugin(
			@ParamValue("name") final Optional<String> name,
			@ParamValue("path") final String path,
			@ParamValue("fileInfoClass") final String fileInfoClassName,
			final VTransactionManager transactionManager,
			@ParamValue("purgeDelayMinutes") final Optional<Integer> purgeDelayMinutesOpt) {
		Assertion.check()
				.isNotNull(name)
				.isNotBlank(path)
				.isNotBlank(fileInfoClassName)
				.isNotNull(transactionManager)
				.isTrue(path.endsWith("/"), "store path must ends with / ({0})", path);
		//-----
		this.name = name.orElse(DEFAULT_STORE_NAME);
		this.transactionManager = transactionManager;
		documentRoot = Paths.get(FileUtil.translatePath(path));
		Assertion.check()
				.when(Files.exists(documentRoot), () -> Assertion.check()
						.isTrue(Files.isDirectory(documentRoot), "documentRoot ({0}) must be a directory", documentRoot.toAbsolutePath().toString()));
		try {
			Files.createDirectories(documentRoot);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't create file store root directory {0}", documentRoot.toAbsolutePath().toString());
		}
		this.purgeDelayMinutesOpt = purgeDelayMinutesOpt;
		this.fileInfoClassName = fileInfoClassName;
	}

	/**
	 * Daemon to purge old files
	 */
	@DaemonScheduled(name = "DmnPurgeFileStoreDaemon", periodInSeconds = 5 * 60)
	public void deleteOldFiles() {
		if (purgeDelayMinutesOpt.isPresent()) {
			final long maxTime = System.currentTimeMillis() - purgeDelayMinutesOpt.get() * 60L * 1000L;
			doDeleteOldFiles(documentRoot, maxTime);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo read(final FileInfoURI uri) {
		/* read metadata*/
		try {
			final Path metadataPath = obtainFullMetaDataFilePath(uri);
			final List<String> infos = Files.readAllLines(metadataPath, Charset.forName(METADATA_CHARSET));
			// récupération des infos
			final String fileName = infos.get(0);
			final String mimeType = infos.get(1);
			final Instant lastModified = Instant.from(DateTimeFormatter.ofPattern(INFOS_DATE_PATTERN).withZone(ZoneOffset.UTC).parse(infos.get(2)));
			final long length = Long.parseLong(infos.get(3));

			final InputStreamBuilder inputStreamBuilder = new PathInputStreamBuilder(obtainFullFilePath(uri));
			final VFile vFile = StreamFile.of(fileName, mimeType, lastModified, length, inputStreamBuilder);

			// retourne le fileinfo avec le fichier et son URI
			final FsFileInfo fsFileInfo = new FsFileInfo(uri.getDefinition(), vFile);
			fsFileInfo.setURIStored(uri);
			return fsFileInfo;
		} catch (final NoSuchFileException e) { //throw NulPoint if not found, to respect FileStore read contract
			throw (NullPointerException) new NullPointerException("fileInfo not found '" + uri + "'")
					.initCause(e);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't read fileInfo {0}", uri.toURN());
		}
	}

	private static class FsFileInfo extends AbstractFileInfo {
		private static final long serialVersionUID = -1610176974946554828L;

		protected FsFileInfo(final FileInfoDefinition fileInfoDefinition, final VFile vFile) {
			super(fileInfoDefinition, vFile);
		}
	}

	private void saveFile(final String metaData, final FileInfo fileInfo) {
		FileActionSave fileActionSave;
		try (final InputStream inputStream = fileInfo.getVFile().createInputStream()) {
			fileActionSave = new FileActionSave(inputStream, obtainFullFilePath(fileInfo.getURI()).toString());
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't read uploaded file.");
		}
		try (final InputStream inputStream = new ByteArrayInputStream(metaData.getBytes(METADATA_CHARSET))) {
			fileActionSave.add(inputStream, obtainFullMetaDataFilePath(fileInfo.getURI()).toString());
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't read metadata file.");
		}
		getCurrentTransaction().addAfterCompletion(fileActionSave);
	}

	private Path obtainFullFilePath(final FileInfoURI uri) {
		final String uriAsString = String.class.cast(uri.getKey());
		final String uriAsPath = URI_AS_PATH_PATTERN.matcher(uriAsString).replaceFirst("$1/$2/$3/");
		return documentRoot.resolve(uriAsPath);
	}

	private Path obtainFullMetaDataFilePath(final FileInfoURI uri) {
		final String uriAsString = String.class.cast(uri.getKey());
		final String uriAsPath = URI_AS_PATH_PATTERN.matcher(uriAsString).replaceFirst("$1/$2/$3/");
		return documentRoot.resolve(uriAsPath + METADATA_SUFFIX);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo create(final FileInfo fileInfo) {
		Assertion.check()
				.isNotNull(fileInfo)
				.isNull(fileInfo.getURI(), "Only file without any id can be created.");
		//-----
		final VFile vFile = fileInfo.getVFile();
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(INFOS_DATE_PATTERN)
				.withZone(ZoneId.of("UTC"));
		final String metaData = new StringBuilder()
				.append(vFile.getFileName()).append('\n')
				.append(vFile.getMimeType()).append('\n')
				.append(formatter.format(vFile.getLastModified())).append('\n')
				.append(vFile.getLength()).append('\n')
				.toString();

		final FileInfoURI uri = createNewFileInfoURI(fileInfo.getDefinition());
		fileInfo.setURIStored(uri);
		saveFile(metaData, fileInfo);
		return fileInfo;
	}

	private static FileInfoURI createNewFileInfoURI(final FileInfoDefinition fileInfoDefinition) {
		final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-", Locale.FRANCE);
		final String pathToSave = format.format(new Date()) + UUID.randomUUID();
		return new FileInfoURI(fileInfoDefinition, pathToSave);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.check().isNotNull(fileInfo.getURI() != null, "Only file with an id can be updated.");
		//-----
		final VFile vFile = fileInfo.getVFile();
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(INFOS_DATE_PATTERN)
				.withZone(ZoneId.of("UTC"));
		final String metaData = new StringBuilder()
				.append(vFile.getFileName()).append('\n')
				.append(vFile.getMimeType()).append('\n')
				.append(formatter.format(vFile.getLastModified())).append('\n')
				.append(vFile.getLength()).append('\n')
				.toString();

		saveFile(metaData, fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final FileInfoURI uri) {
		//-----suppression du fichier
		getCurrentTransaction().addAfterCompletion(new FileActionDelete(obtainFullFilePath(uri).toString()));
		getCurrentTransaction().addAfterCompletion(new FileActionDelete(obtainFullMetaDataFilePath(uri).toString()));
	}

	@Override
	public Class<? extends FileInfo> getFileInfoClass() {
		return ClassUtil.classForName(fileInfoClassName, FileInfo.class);
	}

	private static final class PathInputStreamBuilder implements InputStreamBuilder {
		private final Path path;

		PathInputStreamBuilder(final Path path) {
			this.path = path;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return Files.newInputStream(path);
		}
	}

	/** récupère la transaction courante. */
	private VTransaction getCurrentTransaction() {
		return transactionManager.getCurrentTransaction();
	}

	private static void doDeleteOldFiles(final Path documentRootFile, final long maxTime) {
		final List<RuntimeException> processIOExceptions = new ArrayList<>();
		if (Files.exists(documentRootFile)) { // if this is for temp files, the root directy might have been deleted by the os!
			try (Stream<Path> fileStream = Files.list(documentRootFile)) {
				fileStream.forEach(subFile -> {
					if (Files.isDirectory(subFile) && Files.isReadable(subFile)) { //canRead pour les pbs de droits
						doDeleteOldFiles(subFile, maxTime);
					} else {
						boolean shouldDelete = false;
						try {
							shouldDelete = Files.getLastModifiedTime(subFile).toMillis() <= maxTime;
							if (shouldDelete) {
								Files.delete(subFile);
							}
						} catch (final IOException e) {
							managedIOException(processIOExceptions, e);
							if (shouldDelete) {
								subFile.toFile().deleteOnExit();
							}
						}
					}
				});
			} catch (final IOException e) {
				managedIOException(processIOExceptions, e);
			}
			if (!processIOExceptions.isEmpty()) {
				throw processIOExceptions.get(0); //We throw the first exception (for daemon health stats), and log the others
			}
		}
	}

	private static void managedIOException(final List<RuntimeException> processIOExceptions, final IOException causeException) {
		processIOExceptions.add(WrappedException.wrap(causeException));
		LOG.error("doDeleteOldFiles error", causeException);
	}
}
