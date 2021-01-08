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
import org.mockito.Matchers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportableFileStoreTest {


  @Test
  void shouldNotPutContentIfNoFilesExists(@TempDir Path temp) throws IOException {
    Exporter exporter = mock(Exporter.class);
    ExportableFileStore exportableFileStore = new ExportableFileStore(temp.toFile(), "config");

    exportableFileStore.export(exporter);

    verify(exporter, never()).put(any());
  }

  @Test
  void shouldPutContentIntoExporterForEachFile(@TempDir Path temp) throws IOException {
    createFile(temp, "config", "trace", "first.xml");
    createFile(temp, "config", "trace", "second.xml");
    OutputStream os = mock(OutputStream.class);
    Exporter exporter = mock(Exporter.class);
    ExportableFileStore exportableFileStore = new ExportableFileStore(temp.resolve("config").resolve("trace").toFile(), "first.xml");
    when(exporter.put(anyString())).thenReturn(os);

    exportableFileStore.export(exporter);

    verify(exporter).put("first.xml");
    verify(exporter).put("second.xml");
    // TODO I can't figure out how to check if the outputstream was written properly
//    verify(os).write(any(byte[].class), anyInt(), anyInt());
  }

  private File createFile(Path temp, String type, String name, String fileName) throws IOException {
    Path path = temp.resolve(type).resolve(name);
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
