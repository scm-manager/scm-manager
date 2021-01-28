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

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FullScmRepositoryImporterTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold("svn");

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService service;
  @Mock
  private UnbundleCommandBuilder unbundleCommandBuilder;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private ScmEnvironmentCompatibilityChecker compatibilityChecker;
  @Mock
  private TarArchiveRepositoryStoreImporter storeImporter;

  @InjectMocks
  private FullScmRepositoryImporter fullImporter;

  @BeforeEach
  void initRepositoryService() {
    lenient().when(serviceFactory.create(REPOSITORY)).thenReturn(service);
    lenient().when(service.getUnbundleCommand()).thenReturn(unbundleCommandBuilder);
  }

  @Test
  void shouldNotImportRepositoryIfFileNotExists(@TempDir Path temp) throws IOException {
    File emptyFile = new File(temp.resolve("empty").toString());
    Files.touch(emptyFile);
    assertThrows(ImportFailedException.class, () -> fullImporter.importFromStream(REPOSITORY, new FileInputStream(emptyFile)));
  }

  @Test
  void shouldFailIfScmEnvironmentIsIncompatible() {
    when(compatibilityChecker.check(any())).thenReturn(false);

    assertThrows(
      ImportFailedException.class,
      () -> fullImporter.importFromStream(REPOSITORY, Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream())
    );
  }

  @Test
  void shouldImportScmRepositoryArchive() throws IOException {
    when(compatibilityChecker.check(any())).thenReturn(true);
    when(repositoryManager.create(eq(REPOSITORY), any())).thenReturn(REPOSITORY);
    when(service.getBundleCommand().getFileExtension()).thenReturn("dump");

    Repository repository = fullImporter.importFromStream(REPOSITORY, Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream());
    assertThat(repository).isEqualTo(REPOSITORY);
    verify(storeImporter).importFromTarArchive(eq(REPOSITORY), any(InputStream.class));
  }
}
