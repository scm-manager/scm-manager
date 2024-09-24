/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.importexport;

import jakarta.inject.Inject;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import sonia.scm.repository.FullRepositoryExporter;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.util.Archives;
import sonia.scm.util.IOUtil;
import sonia.scm.web.security.AdministrationContext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static sonia.scm.ContextEntry.ContextBuilder.entity;


public class FullScmRepositoryExporter implements FullRepositoryExporter {

  static final String SCM_ENVIRONMENT_FILE_NAME = "scm-environment.xml";
  static final String METADATA_FILE_NAME = "metadata.xml";
  static final String STORE_DATA_FILE_NAME = "store-data.tar";
  private final EnvironmentInformationXmlGenerator environmentGenerator;
  private final RepositoryMetadataXmlGenerator metadataGenerator;
  private final RepositoryServiceFactory serviceFactory;
  private final TarArchiveRepositoryStoreExporter storeExporter;
  private final WorkdirProvider workdirProvider;
  private final RepositoryExportingCheck repositoryExportingCheck;
  private final RepositoryImportExportEncryption repositoryImportExportEncryption;
  private final ExportNotificationHandler notificationHandler;

  private final AdministrationContext administrationContext;

  @Inject
  public FullScmRepositoryExporter(EnvironmentInformationXmlGenerator environmentGenerator,
                                   RepositoryMetadataXmlGenerator metadataGenerator,
                                   RepositoryServiceFactory serviceFactory,
                                   TarArchiveRepositoryStoreExporter storeExporter,
                                   WorkdirProvider workdirProvider,
                                   RepositoryExportingCheck repositoryExportingCheck,
                                   RepositoryImportExportEncryption repositoryImportExportEncryption, ExportNotificationHandler notificationHandler, AdministrationContext administrationContext) {
    this.environmentGenerator = environmentGenerator;
    this.metadataGenerator = metadataGenerator;
    this.serviceFactory = serviceFactory;
    this.storeExporter = storeExporter;
    this.workdirProvider = workdirProvider;
    this.repositoryExportingCheck = repositoryExportingCheck;
    this.repositoryImportExportEncryption = repositoryImportExportEncryption;
    this.notificationHandler = notificationHandler;
    this.administrationContext = administrationContext;
  }

  public void export(Repository repository, OutputStream outputStream, String password) {
    try {
      repositoryExportingCheck.withExportingLock(repository, () -> {
        exportInLock(repository, outputStream, password);
        return null;
      });
    } catch (ExportFailedException ex) {
      notificationHandler.handleFailedExport(repository);
      throw ex;
    }
  }

  private void exportInLock(Repository repository, OutputStream outputStream, String password) {
    try (
      RepositoryService service = serviceFactory.create(repository);
      BufferedOutputStream bos = new BufferedOutputStream(outputStream);
      OutputStream cos = repositoryImportExportEncryption.optionallyEncrypt(bos, password);
      GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(cos);
      TarArchiveOutputStream taos = Archives.createTarOutputStream(gzos);
    ) {
      writeEnvironmentData(repository, taos);
      writeMetadata(repository, taos);
      writeStoreData(repository, taos);
      writeRepository(service, taos);
      taos.finish();
    } catch (IOException e) {
      throw new ExportFailedException(
        entity(repository).build(),
        "Could not export repository with metadata",
        e
      );
    }
  }

  private void writeEnvironmentData(Repository repository, TarArchiveOutputStream taos) {
    administrationContext.runAsAdmin(() -> {
      byte[] envBytes = environmentGenerator.generate();
      TarArchiveEntry entry = new TarArchiveEntry(SCM_ENVIRONMENT_FILE_NAME);
      entry.setSize(envBytes.length);
      try {
        taos.putArchiveEntry(entry);
        taos.write(envBytes);
        taos.closeArchiveEntry();
      } catch (IOException e) {
        throw new ExportFailedException(entity(repository).build(), "Failed to collect instance environment for repository export", e);
      }
    });
  }

  private void writeMetadata(Repository repository, TarArchiveOutputStream taos) throws IOException {
    byte[] metadataBytes = metadataGenerator.generate(repository);
    TarArchiveEntry entry = new TarArchiveEntry(METADATA_FILE_NAME);
    entry.setSize(metadataBytes.length);
    taos.putArchiveEntry(entry);
    taos.write(metadataBytes);
    taos.closeArchiveEntry();
  }

  private void writeRepository(RepositoryService service, TarArchiveOutputStream taos) throws IOException {
    File newWorkdir = workdirProvider.createNewWorkdir(service.getRepository().getId());
    try {
      File repositoryFile = Files.createFile(Paths.get(newWorkdir.getPath(), "repository")).toFile();
      try (FileOutputStream repositoryFos = new FileOutputStream(repositoryFile)) {
        service.getBundleCommand().bundle(repositoryFos);
      }
      TarArchiveEntry entry = new TarArchiveEntry(createRepositoryEntryName(service));
      entry.setSize(repositoryFile.length());
      taos.putArchiveEntry(entry);
      Files.copy(repositoryFile.toPath(), taos);
      taos.closeArchiveEntry();
    } finally {
      IOUtil.deleteSilently(newWorkdir);
    }
  }

  private String createRepositoryEntryName(RepositoryService service) {
    return String.format("%s.%s", service.getRepository().getName(), service.getBundleCommand().getFileExtension());
  }

  private void writeStoreData(Repository repository, TarArchiveOutputStream taos) throws IOException {
    File newWorkdir = workdirProvider.createNewWorkdir(repository.getId());
    try {
      File metadata = Files.createFile(Paths.get(newWorkdir.getPath(), "metadata")).toFile();
      try (FileOutputStream metadataFos = new FileOutputStream(metadata)) {
        storeExporter.export(repository, metadataFos);
      }
      TarArchiveEntry entry = new TarArchiveEntry(STORE_DATA_FILE_NAME);
      entry.setSize(metadata.length());
      taos.putArchiveEntry(entry);
      Files.copy(metadata.toPath(), taos);
      taos.closeArchiveEntry();
    } finally {
      IOUtil.deleteSilently(newWorkdir);
    }
  }
}
