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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.security.KeyGenerator;
import sonia.scm.util.IOUtil;

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class JAXBDataStoreFactory extends FileBasedStoreFactory
  implements DataStoreFactory {

  private final KeyGenerator keyGenerator;

  private final StoreCache<DataStore<?>> storeCache;

  private final DataFileCache dataFileCache;

  @Inject
  public JAXBDataStoreFactory(
    SCMContextProvider contextProvider,
    RepositoryLocationResolver repositoryLocationResolver,
    KeyGenerator keyGenerator,
    RepositoryReadOnlyChecker readOnlyChecker,
    DataFileCache dataFileCache,
    StoreCacheConfigProvider storeCacheConfigProvider
  ) {
    super(contextProvider, repositoryLocationResolver, Store.DATA, readOnlyChecker);
    this.keyGenerator = keyGenerator;
    this.dataFileCache = dataFileCache;
    this.storeCache = new StoreCache<>(this::createStore, storeCacheConfigProvider.isStoreCacheEnabled());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> DataStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    return (DataStore<T>) storeCache.getStore(storeParameters);
  }

  private  <T> DataStore<T> createStore(TypedStoreParameters<T> storeParameters) {
    File storeLocation = getStoreLocation(storeParameters);
    IOUtil.mkdirs(storeLocation);
    return new JAXBDataStore<>(
      keyGenerator,
      TypedStoreContext.of(storeParameters),
      storeLocation,
      mustBeReadOnly(storeParameters),
      dataFileCache.instanceFor(storeParameters.getType())
    );
  }
}
