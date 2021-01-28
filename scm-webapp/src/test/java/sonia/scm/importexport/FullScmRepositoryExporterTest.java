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
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BundleCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.work.WorkdirProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

  @InjectMocks
  private FullScmRepositoryExporter exporter;

  private Collection<Path> workDirsCreated = new ArrayList<>();

  @BeforeEach
  void initRepoService() {
    when(serviceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(environmentGenerator.generate()).thenReturn(new byte[0]);
    when(metadataGenerator.generate(REPOSITORY)).thenReturn(new byte[0]);
  }

  @Test
  void shouldExportEverythingAsTarArchive(@TempDir Path temp) throws IOException {
    BundleCommandBuilder bundleCommandBuilder = mock(BundleCommandBuilder.class);
    when(repositoryService.getBundleCommand()).thenReturn(bundleCommandBuilder);
    when(workdirProvider.createNewWorkdir()).thenAnswer(invocation -> createWorkDir(temp));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    exporter.export(REPOSITORY, baos);

    verify(storeExporter, times(1)).export(eq(REPOSITORY), any(OutputStream.class));
    verify(environmentGenerator, times(1)).generate();
    verify(metadataGenerator, times(1)).generate(REPOSITORY);
    verify(bundleCommandBuilder, times(1)).bundle(any(OutputStream.class));
    workDirsCreated.forEach(wd -> assertThat(wd).doesNotExist());
  }

  private File createWorkDir(Path temp) throws IOException {
    Path newWorkDir = temp.resolve("workDir-" + workDirsCreated.size());
    workDirsCreated.add(newWorkDir);
    Files.createDirectories(newWorkDir);
    return newWorkDir.toFile();
  }
}
