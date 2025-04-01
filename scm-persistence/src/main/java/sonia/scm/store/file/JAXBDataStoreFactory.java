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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.TypedStoreParameters;
import sonia.scm.util.IOUtil;

import java.io.File;


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
    StoreCacheFactory storeCacheFactory
  ) {
    super(contextProvider, repositoryLocationResolver, Store.DATA, readOnlyChecker);
    this.keyGenerator = keyGenerator;
    this.dataFileCache = dataFileCache;
    this.storeCache = storeCacheFactory.createStoreCache(this::createStore);
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
