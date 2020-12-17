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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryArchivedCheck;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.nio.file.Path;

//~--- JDK imports ------------------------------------------------------------

/**
 * Abstract store factory for file based stores.
 *
 * @author Sebastian Sdorra
 */
public abstract class FileBasedStoreFactory {

  /**
   * the logger for FileBasedStoreFactory
   */
  private static final Logger LOG = LoggerFactory.getLogger(FileBasedStoreFactory.class);
  private final SCMContextProvider contextProvider;
  private final RepositoryLocationResolver repositoryLocationResolver;
  private final Store store;
  private final RepositoryArchivedCheck archivedCheck;

  protected FileBasedStoreFactory(SCMContextProvider contextProvider, RepositoryLocationResolver repositoryLocationResolver, Store store, RepositoryArchivedCheck archivedCheck) {
    this.contextProvider = contextProvider;
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.store = store;
    this.archivedCheck = archivedCheck;
  }

  protected File getStoreLocation(StoreParameters storeParameters) {
    return getStoreLocation(storeParameters.getName(), null, storeParameters.getRepositoryId());
  }

  protected File getStoreLocation(TypedStoreParameters storeParameters) {
    return getStoreLocation(storeParameters.getName(), storeParameters.getType(), storeParameters.getRepositoryId());
  }

  protected File getStoreLocation(String name, Class type, String repositoryId) {
    File storeDirectory;
    if (repositoryId != null) {
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
    return storeParameters.getRepositoryId() != null && archivedCheck.isArchived(storeParameters.getRepositoryId());
  }

  /**
   * Get the store directory of a specific repository
   * @param store the type of the store
   * @param repositoryId the id of the repossitory
   * @return the store directory of a specific repository
   */
  private File getStoreDirectory(Store store, String repositoryId) {
    return new File(repositoryLocationResolver.forClass(Path.class).getLocation(repositoryId).toFile(), store.getRepositoryStoreDirectory());
  }

  /**
   * Get the global store directory
   * @param store the type of the store
   * @return the global store directory
   */
  private File getStoreDirectory(Store store) {
    return new File(contextProvider.getBaseDirectory(), store.getGlobalStoreDirectory());
  }

}
