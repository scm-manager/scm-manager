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

import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateException;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.StoreUpdateStepUtilFactory;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileStoreUpdateStepUtil implements StoreUpdateStepUtilFactory.StoreUpdateStepUtil {

  private final RepositoryLocationResolver locationResolver;
  private final SCMContextProvider contextProvider;

  private final StoreParameters parameters;
  private final StoreType type;

  FileStoreUpdateStepUtil(RepositoryLocationResolver locationResolver, SCMContextProvider contextProvider, StoreParameters parameters, StoreType type) {
    this.locationResolver = locationResolver;
    this.contextProvider = contextProvider;
    this.parameters = parameters;
    this.type = type;
  }

  @Override
  public void renameStore(String newName) {
    Path oldStorePath = resolveBasePath().resolve(parameters.getName());
    if (Files.exists(oldStorePath)) {
      Path newStorePath = resolveBasePath().resolve(newName);
      try {
        Files.move(oldStorePath, newStorePath);
      } catch (IOException e) {
        throw new UpdateException(String.format("Could not move store path %s to %s", oldStorePath, newStorePath), e);
      }
    }
  }

  @Override
  public void deleteStore() {
    Path oldStorePath = resolveBasePath().resolve(parameters.getName());
    IOUtil.deleteSilently(oldStorePath.toFile());
  }

  private Path resolveBasePath() {
    Path basePath;
    if (parameters.getRepositoryId() != null) {
      basePath = locationResolver.forClass(Path.class).getLocation(parameters.getRepositoryId());
    } else {
      basePath = contextProvider.getBaseDirectory().toPath();
    }
    Path storeBasePath;
    if (parameters.getRepositoryId() == null) {
      storeBasePath = basePath.resolve(Store.forStoreType(type).getGlobalStoreDirectory());
    } else {
      storeBasePath = basePath.resolve(Store.forStoreType(type).getRepositoryStoreDirectory());
    }
    return storeBasePath;
  }
}
