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
