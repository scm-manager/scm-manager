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
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.api.ExportFailedException;

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
  public List<ExportableStore> listExportableStores(Repository repository) {
    List<ExportableStore> exportableStores = new ArrayList<>();
    File[] storeTypeDirectories = resolveStoreTypeDirectories(repository);
    if (storeTypeDirectories != null) {
      for (File storeTypeDirectory : storeTypeDirectories) {
        exportStoreTypeDirectories(exportableStores, storeTypeDirectory);
      }
    }
    return exportableStores;
  }

  private File[] resolveStoreTypeDirectories(Repository repository) {
    File storeLocation = locationResolver
      .forClass(Path.class)
      .getLocation(repository.getId())
      .resolve("store")
      .toFile();
    return storeLocation.listFiles();
  }

  private void exportStoreTypeDirectories(List<ExportableStore> exportableStores, File storeTypeDirectory) {
    File[] storeDirectories = storeTypeDirectory.listFiles();
    if (storeDirectories != null) {
      for (File storeDirectory : storeDirectories) {
        addExportableFileStore(exportableStores, storeTypeDirectory, storeDirectory);
      }
    }
  }

  private void addExportableFileStore(List<ExportableStore> exportableStores, File storeTypeDirectory, File storeDirectory) {
    if (storeDirectory.isDirectory()) {
      exportableStores.add(new ExportableFileStore(storeDirectory, parseValueToStoreTypeEnum(storeTypeDirectory)));
    } else if (shouldAddConfigStore(exportableStores)) {
      exportableStores.add(new ExportableFileStore(storeTypeDirectory, parseValueToStoreTypeEnum(storeTypeDirectory)));
    }
  }

  private StoreType parseValueToStoreTypeEnum(File storeTypeDirectory) {
    for (StoreType type : StoreType.values()) {
      if (type.getValue().equals(storeTypeDirectory.getName())) {
        return type;
      }
    }
    throw new ExportFailedException(
      ContextEntry.ContextBuilder.noContext(),
      String.format("Unsupported store type found: %s", storeTypeDirectory.getName())
    );
  }

  private boolean shouldAddConfigStore(List<ExportableStore> exportableStores) {
    return exportableStores.stream().noneMatch(es -> StoreType.CONFIG.equals(es.getMetaData().getType()));
  }
}
