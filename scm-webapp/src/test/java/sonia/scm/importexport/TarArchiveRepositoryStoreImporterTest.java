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
  @Mock
  private RepositoryImportLogger logger;

  @InjectMocks
  private TarArchiveRepositoryStoreImporter tarArchiveRepositoryStoreImporter;

  @Test
  void shouldDoNothingIfNoEntries() {
    ByteArrayInputStream bais = new ByteArrayInputStream("".getBytes());
    tarArchiveRepositoryStoreImporter.importFromTarArchive(repository, bais, logger);
    verify(repositoryStoreImporter, never()).doImport(any(Repository.class));
  }

  @Test
  void shouldImportEachEntry() throws IOException {
    InputStream inputStream = Resources.getResource("sonia/scm/repository/import/scm-metadata.tar").openStream();
    tarArchiveRepositoryStoreImporter.importFromTarArchive(repository, inputStream, logger);
    verify(repositoryStoreImporter, times(2)).doImport(repository);
  }

  @Test
  void shouldThrowImportFailedExceptionIfInvalidStorePath() throws IOException {
    InputStream inputStream = Resources.getResource("sonia/scm/repository/import/scm-metadata_invalid.tar").openStream();
    assertThrows(ImportFailedException.class, () -> tarArchiveRepositoryStoreImporter.importFromTarArchive(repository, inputStream, logger));
  }
}
