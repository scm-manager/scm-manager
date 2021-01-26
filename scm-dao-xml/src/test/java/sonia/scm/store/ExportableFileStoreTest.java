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

package sonia.scm.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

  @Test
  void shouldNotPutContentIfNoFilesExists(@TempDir Path temp) throws IOException {
    Exporter exporter = mock(Exporter.class);
    ExportableFileStore exportableFileStore = new ExportableFileStore(temp.resolve("store").toFile(), StoreType.CONFIG);

    exportableFileStore.export(exporter);

    verify(exporter, never()).put(anyString(), anyLong());
  }

  @Test
  void shouldPutContentIntoExporterForDataStore(@TempDir Path temp) throws IOException {
    createFile(temp, "data", "trace", "first.xml");
    createFile(temp, "data", "trace", "second.xml");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Exporter exporter = mock(Exporter.class);
    ExportableFileStore exportableFileStore = new ExportableFileStore(temp.toFile(), StoreType.DATA);
    when(exporter.put(anyString(), anyLong())).thenReturn(os);

    exportableFileStore.export(exporter);

    verify(exporter).put(eq("first.xml"), anyLong());
    verify(exporter).put(eq("second.xml"), anyLong());
    assertThat(os.toString()).isNotBlank();
  }

  @Test
  void shouldPutContentIntoExporterForConfigStore(@TempDir Path temp) throws IOException {
    createFile(temp, "config", "", "first.xml");
    createFile(temp, "config", "", "second.xml");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Exporter exporter = mock(Exporter.class);
    ExportableFileStore exportableConfigFileStore = new ExportableFileStore(temp.toFile(), StoreType.CONFIG);
    when(exporter.put(anyString(), anyLong())).thenReturn(os);

    exportableConfigFileStore.export(exporter);

    verify(exporter).put(eq("first.xml"), anyLong());
    verify(exporter).put(eq("second.xml"), anyLong());
    assertThat(os.toString()).isNotBlank();
  }

  @Test
  void shouldPutContentIntoExporterForBlobStore(@TempDir Path temp) throws IOException {
    createFile(temp, "blob", "assets", "first.blob");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Exporter exporter = mock(Exporter.class);
    ExportableFileStore exportableConfigFileStore = new ExportableFileStore(temp.toFile(), StoreType.BLOB);
    when(exporter.put(anyString(), anyLong())).thenReturn(os);

    exportableConfigFileStore.export(exporter);

    verify(exporter).put(eq("first.blob"), anyLong());
    assertThat(os.toString()).isNotBlank();
  }

  @Test
  void shouldSkipFilteredConfigFiles(@TempDir Path temp) throws IOException {
    createFile(temp, "config", "", "first.yaml");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Exporter exporter = mock(Exporter.class);
    ExportableFileStore exportableConfigFileStore = new ExportableFileStore(temp.toFile(), StoreType.CONFIG);

    exportableConfigFileStore.export(exporter);

    verify(exporter, never()).put(anyString(), anyLong());
    assertThat(os.toString()).isBlank();
  }

  @Test
  void shouldSkipFilteredBlobFiles(@TempDir Path temp) throws IOException {
    createFile(temp, "blob", "security", "second.xml");
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Exporter exporter = mock(Exporter.class);
    ExportableFileStore exportableConfigFileStore = new ExportableFileStore(temp.toFile(), StoreType.BLOB);

    exportableConfigFileStore.export(exporter);

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
