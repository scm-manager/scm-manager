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
import sonia.scm.repository.api.ExportFailedException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static sonia.scm.ContextEntry.ContextBuilder.noContext;
import static sonia.scm.store.ExportableBlobFileStore.BLOB_FACTORY;
import static sonia.scm.store.ExportableConfigFileStore.CONFIG_FACTORY;
import static sonia.scm.store.ExportableDataFileStore.DATA_FACTORY;

public class FileStoreExporter implements StoreExporter {

  private final RepositoryLocationResolver locationResolver;

  private static final Collection<Function<StoreType, Optional<Function<Path, ExportableStore>>>> STORE_FACTORIES =
    asList(DATA_FACTORY, BLOB_FACTORY, CONFIG_FACTORY);

  @Inject
  public FileStoreExporter(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  @Override
  public List<ExportableStore> listExportableStores(Repository repository) {
    List<ExportableStore> exportableStores = new ArrayList<>();
    Path storeDirectory = resolveStoreDirectory(repository);
    if (!Files.exists(storeDirectory)) {
      return emptyList();
    }
    try (Stream<Path> storeTypeDirectories = Files.list(storeDirectory)) {
      storeTypeDirectories.forEach(storeTypeDirectory ->
        exportStoreTypeDirectories(exportableStores, storeTypeDirectory)
      );
    } catch (IOException e) {
      throw new ExportFailedException(
        noContext(),
        "Could not list content of directory " + storeDirectory,
        e
      );
    }
    return exportableStores;
  }

  private Path resolveStoreDirectory(Repository repository) {
    return locationResolver
      .forClass(Path.class)
      .getLocation(repository.getId())
      .resolve(Store.STORE_DIRECTORY);
  }

  private void exportStoreTypeDirectories(List<ExportableStore> exportableStores, Path storeTypeDirectory) {
    try (Stream<Path> storeDirectories = Files.list(storeTypeDirectory)) {
      storeDirectories.forEach(storeDirectory ->
        getStoreFor(storeDirectory).ifPresent(exportableStores::add)
      );
    } catch (IOException e) {
      throw new ExportFailedException(
        noContext(),
        "Could not list content of directory " + storeTypeDirectory,
        e
      );
    }
  }

  private Optional<ExportableStore> getStoreFor(Path storePath) {
    return STORE_FACTORIES
      .stream()
      .map(factory -> factory.apply(getEnumForValue(storePath.getParent())))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findFirst()
      .map(f -> f.apply(storePath));
  }

  private StoreType getEnumForValue(Path storeTypeDirectory) {
    for (StoreType type : StoreType.values()) {
      if (type.getValue().equals(storeTypeDirectory.getFileName().toString())) {
        return type;
      }
    }
    return null;
  }
}
