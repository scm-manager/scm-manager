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

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class ExportableFileStore implements ExportableStore {

  private final File directory;
  private final String type;

  @Inject
  public ExportableFileStore(File directory, String type) {
    this.directory = directory;
    this.type = type;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getName() {
    return directory.getName();
  }

  @Override
  public void export(Exporter exporter) throws IOException {
    exportDirectoryEntries(exporter, directory);
  }

  private void exportDirectoryEntries(Exporter exporter, File directory) throws IOException {
    if (directory.listFiles() != null) {
      for (File fileOrDir : directory.listFiles()) {
        if (fileOrDir.isDirectory()) {
          exportDirectoryEntries(exporter, fileOrDir);
        } else if (!shouldSkipFile(fileOrDir)) {
          putFileContentIntoStream(exporter, fileOrDir);
        }
      }
    }
  }

  private boolean shouldSkipFile(File fileOrDir) {
    return type.equals("config") && !fileOrDir.getName().endsWith(".xml");
  }

  private void putFileContentIntoStream(Exporter exporter, File file) throws IOException {
    try (OutputStream stream = exporter.put(file.getName(), file.length())) {
      Files.copy(file.toPath(), stream);
    }
  }
}
