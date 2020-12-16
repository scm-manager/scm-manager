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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryArchivedCheck;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.security.KeyGenerator;
import sonia.scm.util.IOUtil;

import java.io.File;

/**
 * File based store factory.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class FileBlobStoreFactory extends FileBasedStoreFactory implements BlobStoreFactory {

  /**
   * the logger for FileBlobStoreFactory
   */
  private static final Logger LOG = LoggerFactory.getLogger(FileBlobStoreFactory.class);

  private final KeyGenerator keyGenerator;

  /**
   * Constructs a new instance.
   *
   * @param repositoryLocationResolver location resolver
   * @param keyGenerator key generator
   */
  @Inject
  public FileBlobStoreFactory(SCMContextProvider contextProvider , RepositoryLocationResolver repositoryLocationResolver, KeyGenerator keyGenerator, RepositoryArchivedCheck archivedCheck) {
    super(contextProvider, repositoryLocationResolver, Store.BLOB, archivedCheck);
    this.keyGenerator = keyGenerator;
  }

  @Override
  @SuppressWarnings("unchecked")
  public BlobStore getStore(StoreParameters storeParameters) {
    File storeLocation = getStoreLocation(storeParameters);
    IOUtil.mkdirs(storeLocation);
    return new FileBlobStore(keyGenerator, storeLocation, mustBeReadOnly(storeParameters));
  }
}
