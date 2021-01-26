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

import sonia.scm.ContextEntry;
import sonia.scm.repository.api.ImportFailedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBasedStoreEntryImporterFactory implements StoreEntryImporterFactory {

  private final Path directory;

  FileBasedStoreEntryImporterFactory(Path directory) {
    this.directory = directory;
  }

  @Override
  public StoreEntryImporter importStore(StoreEntryMetaData metaData) {
    StoreType storeType = metaData.getType();
    String storeName = metaData.getName();
    Path storeDirectory = directory.resolve(Store.STORE_DIRECTORY);
    try {
      storeDirectory = storeDirectory.resolve(resolveFilePath(storeType.getValue(), storeName));
      Files.createDirectories(storeDirectory);
      if (!Files.exists(storeDirectory)) {
        throw new ImportFailedException(
          ContextEntry.ContextBuilder.noContext(),
          String.format("Could not create store for type %s and name %s", storeType, storeName)
        );
      }
      return new FileBasedStoreEntryImporter(storeDirectory);

    } catch (IOException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        String.format("Could not create store directory %s for type %s and name %s", storeDirectory, storeType, storeName)
      );
    }
  }

  private Path resolveFilePath(String type, String name) {
    if (name == null || name.isEmpty()) {
      return Paths.get(type);
    }
    return Paths.get(type, name);
  }
}
