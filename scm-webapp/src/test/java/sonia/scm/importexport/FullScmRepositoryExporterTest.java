/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.importexport;

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

  @InjectMocks
  private FullScmRepositoryExporter exporter;

  private final Collection<Path> workDirsCreated = new ArrayList<>();

  @BeforeEach
  void initRepoService() throws IOException {
    when(serviceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(metadataGenerator.generate(REPOSITORY)).thenReturn(new byte[0]);
    when(repositoryExportingCheck.withExportingLock(any(), any())).thenAnswer(invocation -> invocation.getArgument(1, Supplier.class).get());
    when(repositoryImportExportEncryption.optionallyEncrypt(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void shouldExportEverythingAsTarArchive(@TempDir Path temp) {
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
    workDirsCreated.forEach(wd -> assertThat(wd).doesNotExist());
  }

  private File createWorkDir(Path temp, String repositoryId) throws IOException {
    Path newWorkDir = temp.resolve("workDir-" + repositoryId);
    workDirsCreated.add(newWorkDir);
    Files.createDirectories(newWorkDir);
    return newWorkDir.toFile();
  }
}
