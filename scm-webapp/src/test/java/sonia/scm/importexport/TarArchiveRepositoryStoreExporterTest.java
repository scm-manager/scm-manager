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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ExportableStore;
import sonia.scm.store.Exporter;
import sonia.scm.store.StoreEntryMetaData;
import sonia.scm.store.StoreExporter;
import sonia.scm.store.StoreType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarArchiveRepositoryStoreExporterTest {

  private static final Repository REPOSITORY = RepositoryTestData.create42Puzzle();

  @Mock
  private StoreExporter storeExporter;

  @InjectMocks
  private TarArchiveRepositoryStoreExporter tarArchiveRepositoryStoreExporter;

  @Test
  void shouldExportNothingIfNoStoresFound() throws IOException {
    when(storeExporter.listExportableStores(REPOSITORY)).thenReturn(Collections.emptyList());
    OutputStream outputStream = mock(OutputStream.class);
    tarArchiveRepositoryStoreExporter.export(REPOSITORY, outputStream);

    verify(outputStream, never()).write(any());
  }

  @Test
  void shouldWriteDataIfRepoStoreFound() {
    when(storeExporter.listExportableStores(REPOSITORY)).thenReturn(ImmutableList.of(new TestExportableStore()));
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    tarArchiveRepositoryStoreExporter.export(REPOSITORY, outputStream);

    String content = outputStream.toString();
    assertThat(content).isNotBlank();
  }

  @Test
  void shouldExportFromFoundRepoStore() throws IOException {
    ExportableStore exportableStore = mock(ExportableStore.class);
    when(storeExporter.listExportableStores(REPOSITORY)).thenReturn(ImmutableList.of(exportableStore));
    OutputStream outputStream = mock(OutputStream.class);
    tarArchiveRepositoryStoreExporter.export(REPOSITORY, outputStream);

    verify(exportableStore).export(any(Exporter.class));
  }

  static class TestExportableStore implements ExportableStore {

    @Override
    public StoreEntryMetaData getMetaData() {
      return new StoreEntryMetaData(StoreType.CONFIG, "puzzle42");
    }

    @Override
    public void export(Exporter exporter) throws IOException {
      try (OutputStream stream = exporter.put("testStore", 0)) {
        stream.flush();
      }
    }
  }
}
