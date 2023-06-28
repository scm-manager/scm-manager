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

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class JAXBConfigurationEntryStoreFactory extends FileBasedStoreFactory
  implements ConfigurationEntryStoreFactory {

  private final KeyGenerator keyGenerator;

  private final StoreCache<ConfigurationEntryStore<?>> storeCache;

  @Inject
  public JAXBConfigurationEntryStoreFactory(SCMContextProvider contextProvider, RepositoryLocationResolver repositoryLocationResolver, KeyGenerator keyGenerator, RepositoryReadOnlyChecker readOnlyChecker) {
    super(contextProvider, repositoryLocationResolver, Store.CONFIG, readOnlyChecker);
    this.keyGenerator = keyGenerator;
    this.storeCache = new StoreCache<>(this::createStore);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> ConfigurationEntryStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    return (ConfigurationEntryStore<T>) storeCache.getStore(storeParameters);
  }

  private  <T> ConfigurationEntryStore<T> createStore(TypedStoreParameters<T> storeParameters) {
    return new JAXBConfigurationEntryStore<>(
      getStoreLocation(storeParameters.getName().concat(StoreConstants.FILE_EXTENSION),
        storeParameters.getType(),
        storeParameters.getRepositoryId(),
        storeParameters.getNamespace()),
      keyGenerator,
      storeParameters.getType(),
      TypedStoreContext.of(storeParameters)
    );
  }
}
