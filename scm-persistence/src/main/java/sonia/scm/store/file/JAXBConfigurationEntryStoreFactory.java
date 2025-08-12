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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.MeterRegistry;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.StoreInvocationMicrometerWrapper;
import sonia.scm.store.TypedStoreParameters;

@Singleton
public class JAXBConfigurationEntryStoreFactory extends FileBasedStoreFactory
  implements ConfigurationEntryStoreFactory {

  private final KeyGenerator keyGenerator;

  private final StoreCache<ConfigurationEntryStore<?>> storeCache;

  private final MeterRegistry meterRegistry;

  @VisibleForTesting
  public JAXBConfigurationEntryStoreFactory(
    SCMContextProvider contextProvider,
    RepositoryLocationResolver repositoryLocationResolver,
    KeyGenerator keyGenerator,
    RepositoryReadOnlyChecker readOnlyChecker,
    StoreCacheFactory storeCacheFactory
  ) {
    this(
      contextProvider,
      repositoryLocationResolver,
      keyGenerator,
      readOnlyChecker,
      storeCacheFactory,
      null
    );
  }

  @Inject
  public JAXBConfigurationEntryStoreFactory(
    SCMContextProvider contextProvider,
    RepositoryLocationResolver repositoryLocationResolver,
    KeyGenerator keyGenerator,
    RepositoryReadOnlyChecker readOnlyChecker,
    StoreCacheFactory storeCacheFactory,
    MeterRegistry meterRegistry
  ) {
    super(contextProvider, repositoryLocationResolver, Store.CONFIG, readOnlyChecker);
    this.keyGenerator = keyGenerator;
    this.storeCache = storeCacheFactory.createStoreCache(this::createStore);
    this.meterRegistry = meterRegistry;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> ConfigurationEntryStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    return (ConfigurationEntryStore<T>) storeCache.getStore(storeParameters);
  }

  private <T> ConfigurationEntryStore<T> createStore(TypedStoreParameters<T> storeParameters) {
    JAXBConfigurationEntryStore<T> store = new JAXBConfigurationEntryStore<>(
      getStoreLocation(storeParameters.getName().concat(StoreConstants.FILE_EXTENSION),
        storeParameters.getType(),
        storeParameters.getRepositoryId(),
        storeParameters.getNamespace()),
      keyGenerator,
      storeParameters.getType(),
      TypedStoreContext.of(storeParameters)
    );

    return StoreInvocationMicrometerWrapper.create(
      "configuration-entry-store",
      storeParameters,
      ConfigurationEntryStore.class,
      store,
      meterRegistry
    );
  }
}
