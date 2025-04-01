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

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BundleCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FullScmRepositoryExporterTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryService;
  @Mock
  private EnvironmentInformationXmlGenerator environmentGenerator;
  @Mock
  private RepositoryMetadataXmlGenerator metadataGenerator;
  @Mock
  private TarArchiveRepositoryStoreExporter storeExporter;
  @Mock
  private WorkdirProvider workdirProvider;
  @Mock
  private RepositoryExportingCheck repositoryExportingCheck;
  @Mock
  private AdministrationContext administrationContext;
  @Mock
  private RepositoryImportExportEncryption repositoryImportExportEncryption;
  @Mock
  private RepositoryQueryableStoreExporter queryableStoreExporter;

  @InjectMocks
  private FullScmRepositoryExporter exporter;

  private final Collection<Path> workDirsCreated = new ArrayList<>();

  @BeforeEach
  void initRepoService() throws IOException {
    when(serviceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(metadataGenerator.generate(REPOSITORY)).thenReturn(new byte[0]);
    when(repositoryExportingCheck.withExportingLock(any(), any())).thenAnswer(invocation -> invocation.getArgument(1, Supplier.class).get());
    when(repositoryImportExportEncryption.optionallyEncrypt(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

    doAnswer(invocation -> {
      File directory = invocation.getArgument(1, File.class);
      File dummyFile = new File(directory, "dummy.xml");
      try (FileWriter writer = new FileWriter(dummyFile)) {
        writer.write("<dummy>Dummy content for testing</dummy>");
      }
      return null;
    }).when(queryableStoreExporter).addQueryableStoreDataToArchive(
      any(Repository.class),
      any(File.class),
      any(TarArchiveOutputStream.class)
    );
  }

  @Test
  void shouldExportEverythingAsTarArchive(@TempDir Path temp) throws IOException {
    BundleCommandBuilder bundleCommandBuilder = mock(BundleCommandBuilder.class);
    when(repositoryService.getBundleCommand()).thenReturn(bundleCommandBuilder);
    when(repositoryService.getRepository()).thenReturn(REPOSITORY);
    when(workdirProvider.createNewWorkdir(anyString())).thenAnswer(invocation -> createWorkDir(temp, invocation.getArgument(0, String.class)));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    exporter.export(REPOSITORY, baos, "");

    verify(storeExporter, times(1)).export(eq(REPOSITORY), any(OutputStream.class));
    verify(administrationContext, times(1)).runAsAdmin(any(PrivilegedAction.class));
    verify(metadataGenerator, times(1)).generate(REPOSITORY);
    verify(bundleCommandBuilder, times(1)).bundle(any(OutputStream.class));
    verify(repositoryExportingCheck).withExportingLock(eq(REPOSITORY), any());
    verify(queryableStoreExporter, times(1))
      .addQueryableStoreDataToArchive(eq(REPOSITORY), any(File.class), any(TarArchiveOutputStream.class));
    workDirsCreated.forEach(wd -> assertThat(wd).doesNotExist());
  }

  private File createWorkDir(Path temp, String repositoryId) throws IOException {
    Path newWorkDir = temp.resolve("workDir-" + repositoryId);
    workDirsCreated.add(newWorkDir);
    Files.createDirectories(newWorkDir);
    return newWorkDir.toFile();
  }
}
