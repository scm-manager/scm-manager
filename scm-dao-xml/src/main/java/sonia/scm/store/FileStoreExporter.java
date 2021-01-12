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

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileStoreExporter implements StoreExporter {

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public FileStoreExporter(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  @Override
  public List<ExportableStore> findExportableStores(Repository repository) {
    List<ExportableStore> exportableStores = new ArrayList<>();
    File location = locationResolver.forClass(Path.class).getLocation(repository.getId()).resolve("store").toFile();
    File[] storeTypeDirectories = location.listFiles();
    if (storeTypeDirectories != null) {
      for (File storeTypeDirectory : storeTypeDirectories) {
        File[] storeDirectories = storeTypeDirectory.listFiles();
        if (storeDirectories != null) {
          for (File storeDirectory : storeDirectories) {
            if (storeDirectory.isDirectory()) {
              exportableStores.add(new ExportableFileStore(storeDirectory, storeTypeDirectory.getName()));
            } else if (shouldAddConfigStore(exportableStores)) {
              exportableStores.add(new ExportableFileStore(storeTypeDirectory, storeTypeDirectory.getName()));
            }
          }
        }
      }
    }
    return exportableStores;
  }

  private boolean shouldAddConfigStore(List<ExportableStore> exportableStores) {
    return exportableStores.isEmpty() || exportableStores.stream().noneMatch(es -> "config".equalsIgnoreCase(es.getType()));
  }
}
