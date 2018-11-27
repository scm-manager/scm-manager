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
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

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
  private RepositoryLocationResolver repositoryLocationResolver;

  private final String dataDirectoryName;

  private File dataDirectory;

  protected FileBasedStoreFactory(RepositoryLocationResolver repositoryLocationResolver, String dataDirectoryName) {
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.dataDirectoryName = dataDirectoryName;
  }

  //~--- get methods ----------------------------------------------------------
  /**
   * Returns data directory for given name.
   *
   * @param name name of data directory
   *
   * @return data directory
   */
  protected File getDirectory(String name) {
    if (dataDirectory == null) {
      dataDirectory = new File(repositoryLocationResolver.getGlobalStoreDirectory(), dataDirectoryName);
      LOG.debug("get data directory {}", dataDirectory);
    }

    File storeDirectory = new File(dataDirectory, name);
    IOUtil.mkdirs(storeDirectory);
    return storeDirectory;
  }

  /**
   * Returns data directory for given name.
   *
   * @param name name of data directory
   *
   * @return data directory
   */
  protected File getDirectory(String name, Repository repository) {
    if (dataDirectory == null) {
      try {
        dataDirectory = new File(repositoryLocationResolver.getStoresDirectory(repository), dataDirectoryName);
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, MessageFormat.format("Error on getting the store directory {0} of the repository {1}", dataDirectory.getAbsolutePath(), repository.getNamespaceAndName()), e);
      }
      LOG.debug("create data directory {}", dataDirectory);
    }
    File storeDirectory = new File(dataDirectory, name);
    IOUtil.mkdirs(storeDirectory);
    return storeDirectory;
  }

}
