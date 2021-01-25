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

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.ContextEntry;
import sonia.scm.repository.api.ImportFailedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBasedStoreEntryImporter implements StoreEntryImporter {

  private final Path directory;
  private final String type;
  private final String name;

  FileBasedStoreEntryImporter(Path directory, String type, String name) {
    this.directory = directory;
    this.type = type;
    this.name = name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getName() {
    return name;
  }

  @VisibleForTesting
  public Path getDirectory() {
    return this.directory;
  }

  @Override
  public void importEntry(String name, InputStream stream) {
    Path filePath = directory.resolve(name);
    try {
      Files.copy(stream, filePath);
    } catch (IOException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        String.format("Could not import file %s for store %s of type %s", name, this.name, type),
        e
      );
    }
  }
}
