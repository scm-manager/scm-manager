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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;
import sonia.scm.update.UpdateEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnstableApiUsage")
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
  @Mock
  private UpdateEngine updateEngine;

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
    FileInputStream inputStream = new FileInputStream(emptyFile);
    assertThrows(
      ImportFailedException.class,
      () -> fullImporter.importFromStream(REPOSITORY, inputStream)
    );
  }

  @Test
  void shouldFailIfScmEnvironmentIsIncompatible() throws IOException {
    when(compatibilityChecker.check(any())).thenReturn(false);

    InputStream importStream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();
    assertThrows(
      ImportFailedException.class,
      () -> fullImporter.importFromStream(REPOSITORY, importStream)
    );
  }

  @Nested
  class WithValidEnvironment {

    @BeforeEach
    void setUpEnvironment() {
      when(compatibilityChecker.check(any())).thenReturn(true);
      when(repositoryManager.create(eq(REPOSITORY), any())).thenAnswer(invocation -> {
        invocation.getArgument(1, Consumer.class).accept(REPOSITORY);
        return REPOSITORY;
      });
    }

    @Test
    void shouldImportScmRepositoryArchive() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();

      Repository repository = fullImporter.importFromStream(REPOSITORY, stream);

      assertThat(repository).isEqualTo(REPOSITORY);
      verify(storeImporter).importFromTarArchive(eq(REPOSITORY), any(InputStream.class));
      verify(repositoryManager).modify(REPOSITORY);
      Collection<RepositoryPermission> updatedPermissions = REPOSITORY.getPermissions();
      assertThat(updatedPermissions).hasSize(2);
      verify(unbundleCommandBuilder).unbundle((InputStream) argThat(argument -> argument.getClass().equals(FullScmRepositoryImporter.NoneClosingInputStream.class)));
    }

    @Test
    void shouldTriggerUpdateForImportedRepository() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();

      fullImporter.importFromStream(REPOSITORY, stream);

      verify(updateEngine).update(REPOSITORY.getId());
    }
  }
}
