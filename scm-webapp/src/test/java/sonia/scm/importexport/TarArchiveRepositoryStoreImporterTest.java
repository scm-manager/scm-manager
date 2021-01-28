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

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.store.RepositoryStoreImporter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TarArchiveRepositoryStoreImporterTest {

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryStoreImporter repositoryStoreImporter;

  @InjectMocks
  private TarArchiveRepositoryStoreImporter tarArchiveRepositoryStoreImporter;

  @Test
  void shouldDoNothingIfNoEntries() {
    ByteArrayInputStream bais = new ByteArrayInputStream("".getBytes());
    tarArchiveRepositoryStoreImporter.importFromTarArchive(repository, bais);
    verify(repositoryStoreImporter, never()).doImport(any(Repository.class));
  }

  @Test
  void shouldImportEachEntry() throws IOException {
    InputStream inputStream = Resources.getResource("sonia/scm/repository/import/scm-metadata.tar").openStream();
    tarArchiveRepositoryStoreImporter.importFromTarArchive(repository, inputStream);
    verify(repositoryStoreImporter, times(2)).doImport(repository);
  }

  @Test
  void shouldThrowImportFailedExceptionIfInvalidStorePath() throws IOException {
    InputStream inputStream = Resources.getResource("sonia/scm/repository/import/scm-metadata_invalid.tar").openStream();
    assertThrows(ImportFailedException.class, () -> tarArchiveRepositoryStoreImporter.importFromTarArchive(repository, inputStream));
  }
}
