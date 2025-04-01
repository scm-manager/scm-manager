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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryableStoreImportStepTest {
  private static final String QUERYABLE_STORE_DATA_FILE_NAME = "queryable-store-data.tar";

  @Mock
  private RepositoryQueryableStoreExporter queryableStoreExporter;
  @Mock
  private ImportState importState;
  @Mock
  private RepositoryImportLogger logger;
  @Mock
  private Repository repository;
  @Mock
  private RepositoryLocationResolver locationResolver;
  @Mock
  private RepositoryLocationResolver.RepositoryLocationResolverInstance<Path> forClass;

  @InjectMocks
  private QueryableStoreImportStep queryableStoreImportStep;

  private File tarFile;
  @TempDir
  private File tempWorkDir;

  @BeforeEach
  void setUp() {
    when(importState.getRepository()).thenReturn(repository);
    when(importState.getLogger()).thenReturn(logger);
    when(repository.getId()).thenReturn("42");
    doNothing().when(logger).step(anyString());

    when(locationResolver.forClass(Path.class)).thenReturn(forClass);
    when(forClass.getLocation(anyString())).thenReturn(tempWorkDir.toPath());

    tarFile = new File(Resources.getResource("sonia/scm/importexport/queryable-store-data.tar").getFile());
  }

  @Test
  void shouldHandleQueryableStoreTarFileCorrectly() throws Exception {
    TarArchiveEntry entry = new TarArchiveEntry(tarFile, QUERYABLE_STORE_DATA_FILE_NAME);
    entry.setSize(tarFile.length());

    doAnswer(
      invocation -> {
        assertThat(tempWorkDir.listFiles())
          .containsExactlyInAnyOrder(
            new File(tempWorkDir, "sonia.scm.importexport.SimpleType.xml"),
            new File(tempWorkDir, "sonia.scm.importexport.SimpleTypeWithTwoParents.xml")
          );
        return null;
      }
    ).when(queryableStoreExporter).importStores("42", tempWorkDir);

    try (InputStream inputStream = new FileInputStream(tarFile)) {
      boolean result = queryableStoreImportStep.handle(entry, importState, inputStream);
      assertThat(result).isTrue();

      verify(queryableStoreExporter).importStores("42", tempWorkDir);
    }
  }
}
