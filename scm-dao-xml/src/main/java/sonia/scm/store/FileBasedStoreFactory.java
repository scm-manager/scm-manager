/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.util.IOUtil;

import java.io.File;

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
  private SCMContextProvider contextProvider;
  private RepositoryLocationResolver repositoryLocationResolver;
  private Store store;

  protected FileBasedStoreFactory(SCMContextProvider contextProvider , RepositoryLocationResolver repositoryLocationResolver, Store store) {
    this.contextProvider = contextProvider;
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.store = store;
  }

  protected File getStoreLocation(StoreParameters storeParameters) {
    return getStoreLocation(storeParameters.getName(), null, storeParameters.getRepository());
  }

  protected File getStoreLocation(TypedStoreParameters storeParameters) {
    return getStoreLocation(storeParameters.getName(), storeParameters.getType(), storeParameters.getRepository());
  }

  protected File getStoreLocation(String name, Class type, Repository repository) {
    File storeDirectory;
    if (repository != null) {
      LOG.debug("create store with type: {}, name: {} and repository: {}", type, name, repository.getNamespaceAndName());
      storeDirectory = this.getStoreDirectory(store, repository);
    } else {
      LOG.debug("create store with type: {} and name: {} ", type, name);
      storeDirectory = this.getStoreDirectory(store);
    }
    IOUtil.mkdirs(storeDirectory);
    return new File(storeDirectory, name);
  }

  /**
   * Get the store directory of a specific repository
   * @param store the type of the store
   * @param repository the repo
   * @return the store directory of a specific repository
   */
  private File getStoreDirectory(Store store, Repository repository) {
    return new File(repositoryLocationResolver.getPath(repository.getId()).toFile(), store.getRepositoryStoreDirectory());
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
