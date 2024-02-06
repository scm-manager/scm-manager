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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.nio.file.Path;

/**
 * Abstract store factory for file based stores.
 *
 */
public abstract class FileBasedStoreFactory {

  private static final String NAMESPACES_DIR = "namespaces";

  private static final Logger LOG = LoggerFactory.getLogger(FileBasedStoreFactory.class);
  private final SCMContextProvider contextProvider;
  private final RepositoryLocationResolver repositoryLocationResolver;
  private final Store store;
  private final RepositoryReadOnlyChecker readOnlyChecker;

  protected FileBasedStoreFactory(SCMContextProvider contextProvider, RepositoryLocationResolver repositoryLocationResolver, Store store, RepositoryReadOnlyChecker readOnlyChecker) {
    this.contextProvider = contextProvider;
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.store = store;
    this.readOnlyChecker = readOnlyChecker;
  }

  protected File getStoreLocation(StoreParameters storeParameters) {
    return getStoreLocation(storeParameters.getName(), null, storeParameters.getRepositoryId(), storeParameters.getNamespace());
  }

  protected File getStoreLocation(TypedStoreParameters<?> storeParameters) {
    return getStoreLocation(storeParameters.getName(), storeParameters.getType(), storeParameters.getRepositoryId(), storeParameters.getNamespace());
  }

  protected File getStoreLocation(String name, Class<?> type, String repositoryId, String namespace) {
    File storeDirectory;
    if (namespace != null) {
      LOG.debug("create store with type: {}, name: {} and namespace: {}", type, name, namespace);
      storeDirectory = this.getNamespaceStoreDirectory(store, namespace);
    } else if (repositoryId != null) {
      LOG.debug("create store with type: {}, name: {} and repository id: {}", type, name, repositoryId);
      storeDirectory = this.getStoreDirectory(store, repositoryId);
    } else {
      LOG.debug("create store with type: {} and name: {} ", type, name);
      storeDirectory = this.getStoreDirectory(store);
    }
    IOUtil.mkdirs(storeDirectory);
    return new File(storeDirectory, name);
  }

  protected boolean mustBeReadOnly(StoreParameters storeParameters) {
    return storeParameters.getRepositoryId() != null && readOnlyChecker.isReadOnly(storeParameters.getRepositoryId());
  }

  /**
   * Get the store directory of a specific repository
   *
   * @param store        the type of the store
   * @param repositoryId the id of the repossitory
   * @return the store directory of a specific repository
   */
  private File getStoreDirectory(Store store, String repositoryId) {
    return new File(repositoryLocationResolver.forClass(Path.class).getLocation(repositoryId).toFile(), store.getRepositoryStoreDirectory());
  }

  /**
   * Get the store directory of a specific namespace
   *
   * @param store        the type of the store
   * @param namespace the name of the namespace
   * @return the store directory of a specific namespace
   */
  private File getNamespaceStoreDirectory(Store store, String namespace) {
    return new File(contextProvider.getBaseDirectory().toPath().resolve(NAMESPACES_DIR).resolve(namespace).toString(), store.getNamespaceStoreDirectory());
  }

  /**
   * Get the global store directory
   *
   * @param store the type of the store
   * @return the global store directory
   */
  private File getStoreDirectory(Store store) {
    return new File(contextProvider.getBaseDirectory(), store.getGlobalStoreDirectory());
  }

}
