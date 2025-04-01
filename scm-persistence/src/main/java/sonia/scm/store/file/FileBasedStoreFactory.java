/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.store.file;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.store.StoreParameters;
import sonia.scm.store.TypedStoreParameters;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.nio.file.Path;

/**
 * Abstract store factory for file based stores.
 *
 */
abstract class FileBasedStoreFactory {

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
