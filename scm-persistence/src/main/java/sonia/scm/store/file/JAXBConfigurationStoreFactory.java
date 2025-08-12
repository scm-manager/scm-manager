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
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreDecoratorFactory;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.StoreInvocationMicrometerWrapper;
import sonia.scm.store.StoreDecoratorFactory;
import sonia.scm.store.TypedStoreParameters;

import java.util.Set;

/**
 * JAXB implementation of {@link ConfigurationStoreFactory}.
 *
 */
@Singleton
public class JAXBConfigurationStoreFactory extends FileBasedStoreFactory implements ConfigurationStoreFactory {

  private final Set<ConfigurationStoreDecoratorFactory> decoratorFactories;

  private final StoreCache<ConfigurationStore<?>> storeCache;

  private final MeterRegistry meterRegistry;

  @VisibleForTesting
  public JAXBConfigurationStoreFactory(
    SCMContextProvider contextProvider,
    RepositoryLocationResolver repositoryLocationResolver,
    RepositoryReadOnlyChecker readOnlyChecker,
    Set<ConfigurationStoreDecoratorFactory> decoratorFactories,
    StoreCacheFactory storeCacheFactory
  ) {
    this(
      contextProvider,
      repositoryLocationResolver,
      readOnlyChecker,
      decoratorFactories,
      storeCacheFactory,
      null
    );
  }

  @Inject
  public JAXBConfigurationStoreFactory(
    SCMContextProvider contextProvider,
    RepositoryLocationResolver repositoryLocationResolver,
    RepositoryReadOnlyChecker readOnlyChecker,
    Set<ConfigurationStoreDecoratorFactory> decoratorFactories,
    StoreCacheFactory storeCacheFactory,
    MeterRegistry meterRegistry
  ) {
    super(contextProvider, repositoryLocationResolver, Store.CONFIG, readOnlyChecker);
    this.decoratorFactories = decoratorFactories;
    this.storeCache = storeCacheFactory.createStoreCache(this::createStore);
    this.meterRegistry = meterRegistry;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> ConfigurationStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    return (ConfigurationStore<T>) storeCache.getStore(storeParameters);
  }

  private <T> ConfigurationStore<T> createStore(TypedStoreParameters<T> storeParameters) {
    TypedStoreContext<T> context = TypedStoreContext.of(storeParameters);

    ConfigurationStore<T> store = new JAXBConfigurationStore<>(
      context,
      storeParameters.getType(),
      getStoreLocation(storeParameters.getName().concat(StoreConstants.FILE_EXTENSION),
        storeParameters.getType(),
        storeParameters.getRepositoryId(),
        storeParameters.getNamespace()),
      () -> mustBeReadOnly(storeParameters)
    );

    for (ConfigurationStoreDecoratorFactory factory : decoratorFactories) {
      store = factory.createDecorator(store, new StoreDecoratorFactory.Context(storeParameters));
    }

    return StoreInvocationMicrometerWrapper.create(
      "configuration-store",
      storeParameters,
      ConfigurationStore.class,
      store,
      meterRegistry
    );
  }
}
