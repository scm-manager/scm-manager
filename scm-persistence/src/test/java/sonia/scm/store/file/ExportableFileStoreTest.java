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

package sonia.scm.store.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ExportableStore;
import sonia.scm.store.Exporter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportableFileStoreTest {

  @Mock
  Exporter exporter;

  @Test
  void shouldNotPutContentIfNoFilesExists(@TempDir Path temp) throws IOException {
    Path dataStoreDir = temp.resolve("some-store");
    Files.createDirectories(dataStoreDir);

    ExportableStore exportableFileStore = new ExportableDataFileStore(dataStoreDir);
    exportableFileStore.export(exporter);

    verify(exporter, never()).put(anyString(), anyLong());
  }

  @Test
  void shouldPutContentIntoExporterForDataStore(@TempDir Path temp) throws IOException {
    createFile(temp, "data", "trace", "first.xml");
    createFile(temp, "data", "trace", "second.xml");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ExportableStore exportableFileStore = new ExportableDataFileStore(temp.resolve("data").resolve("trace"));
    when(exporter.put(anyString(), anyLong())).thenReturn(os);

    exportableFileStore.export(exporter);

    verify(exporter).put(eq("first.xml"), anyLong());
    verify(exporter).put(eq("second.xml"), anyLong());
    assertThat(os.toString()).isNotBlank();
  }

  @Test
  void shouldPutContentIntoExporterForConfigStore(@TempDir Path temp) throws IOException {
    createFile(temp, "config", "", "first.xml");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ExportableStore exportableConfigFileStore = new ExportableConfigFileStore(temp.resolve("config").resolve("first.xml"));
    when(exporter.put(anyString(), anyLong())).thenReturn(os);

    exportableConfigFileStore.export(exporter);

    verify(exporter).put(eq("first.xml"), anyLong());
    assertThat(os.toString()).isNotBlank();
  }

  @Test
  void shouldPutContentIntoExporterForBlobStore(@TempDir Path temp) throws IOException {
    createFile(temp, "blob", "assets", "first.blob");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Exporter exporter = mock(Exporter.class);
    ExportableStore exportableBlobFileStore = new ExportableBlobFileStore(temp.resolve("blob").resolve("assets"));
    when(exporter.put(anyString(), anyLong())).thenReturn(os);

    exportableBlobFileStore.export(exporter);

    verify(exporter).put(eq("first.blob"), anyLong());
    assertThat(os.toString()).isNotBlank();
  }

  @Test
  void shouldSkipFilteredBlobFiles(@TempDir Path temp) throws IOException {
    createFile(temp, "blob", "security", "second.xml");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Exporter exporter = mock(Exporter.class);
    ExportableStore exportableBlobFileStore = new ExportableBlobFileStore(temp.resolve("blob").resolve("security"));

    exportableBlobFileStore.export(exporter);

    verify(exporter, never()).put(anyString(), anyLong());
    assertThat(os.toString()).isBlank();
  }

  private File createFile(Path temp, String type, String name, String fileName) throws IOException {
    Path path = name != null ? temp.resolve(type).resolve(name) : temp.resolve(type);

    new File(path.toUri()).mkdirs();
    File file = new File(path.toFile(), fileName);
    if (!file.exists()) {
      file.createNewFile();
    }
    FileWriter source = new FileWriter(file);
    source.write("something");
    source.close();
    return file;
  }
}
