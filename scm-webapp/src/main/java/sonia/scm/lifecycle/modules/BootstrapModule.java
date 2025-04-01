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

package sonia.scm.lifecycle.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.GuavaCacheManager;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.lifecycle.DefaultRestarter;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.metrics.MeterRegistryProvider;
import sonia.scm.metrics.MonitoringSystem;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.DefaultRepositoryExportingCheck;
import sonia.scm.repository.EventDrivenRepositoryArchiveCheck;
import sonia.scm.repository.RepositoryArchivedCheck;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.RepositoryLocationOverride;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.xml.MetadataStore;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;
import sonia.scm.security.CipherHandler;
import sonia.scm.security.CipherUtil;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreDecoratorFactory;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.FileStoreUpdateStepUtilFactory;
import sonia.scm.store.QueryableStoreFactory;
import sonia.scm.store.StoreMetaDataProvider;
import sonia.scm.store.file.DefaultBlobDirectoryAccess;
import sonia.scm.store.file.FileBlobStoreFactory;
import sonia.scm.store.file.FileNamespaceUpdateIterator;
import sonia.scm.store.file.FileRepositoryUpdateIterator;
import sonia.scm.store.file.JAXBConfigurationEntryStoreFactory;
import sonia.scm.store.file.JAXBConfigurationStoreFactory;
import sonia.scm.store.file.JAXBDataStoreFactory;
import sonia.scm.store.file.JAXBPropertyFileAccess;
import sonia.scm.store.sqlite.SQLiteQueryableStoreFactory;
import sonia.scm.store.sqlite.SQLiteStoreMetaDataProvider;
import sonia.scm.update.BlobDirectoryAccess;
import sonia.scm.update.DefaultRepositoryPermissionUpdater;
import sonia.scm.update.NamespaceUpdateIterator;
import sonia.scm.update.PropertyFileAccess;
import sonia.scm.update.RepositoryPermissionUpdater;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.update.StoreUpdateStepUtilFactory;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.update.xml.XmlV1PropertyDAO;

import java.nio.file.Path;

public class BootstrapModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(BootstrapModule.class);

  private final ClassOverrides overrides;
  private final PluginLoader pluginLoader;

  public BootstrapModule(PluginLoader pluginLoader) {
    this.overrides = ClassOverrides.findOverrides(pluginLoader.getUberClassLoader());
    this.pluginLoader = pluginLoader;
  }

  @Override
  protected void configure() {
    install(ThrowingProviderBinder.forModule(this));

    SCMContextProvider context = SCMContext.getContext();

    bind(SCMContextProvider.class).toInstance(context);

    bind(KeyGenerator.class).to(DefaultKeyGenerator.class);

    bind(RepositoryLocationResolver.class).to(PathBasedRepositoryLocationResolver.class);

    bind(FileSystem.class, DefaultFileSystem.class);

    bind(Restarter.class, DefaultRestarter.class);

    // note CipherUtil uses an other generator
    bind(CipherHandler.class).toInstance(CipherUtil.getInstance().getCipherHandler());

    // Bind empty set in the bootstrap module
    Multibinder.newSetBinder(binder(), ConfigurationStoreDecoratorFactory.class).addBinding()
      .to(NoOpConfigurationStoreDecoratorFactory.class);

    // bind core
    bind(RepositoryArchivedCheck.class, EventDrivenRepositoryArchiveCheck.class);
    bind(RepositoryExportingCheck.class, DefaultRepositoryExportingCheck.class);
    bind(ConfigurationStoreFactory.class, JAXBConfigurationStoreFactory.class);
    bind(ConfigurationEntryStoreFactory.class, JAXBConfigurationEntryStoreFactory.class);
    bind(DataStoreFactory.class, JAXBDataStoreFactory.class);
    bind(BlobStoreFactory.class, FileBlobStoreFactory.class);
    bind(QueryableStoreFactory.class, SQLiteQueryableStoreFactory.class);
    bind(StoreMetaDataProvider.class, SQLiteStoreMetaDataProvider.class);
    bind(PluginLoader.class).toInstance(pluginLoader);
    bind(V1PropertyDAO.class, XmlV1PropertyDAO.class);
    bind(PropertyFileAccess.class, JAXBPropertyFileAccess.class);
    bind(BlobDirectoryAccess.class, DefaultBlobDirectoryAccess.class);
    bind(RepositoryUpdateIterator.class, FileRepositoryUpdateIterator.class);
    bind(NamespaceUpdateIterator.class, FileNamespaceUpdateIterator.class);
    bind(StoreUpdateStepUtilFactory.class, FileStoreUpdateStepUtilFactory.class);
    bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class);
    bind(new TypeLiteral<UpdateStepRepositoryMetadataAccess<Path>>() {}).to(new TypeLiteral<MetadataStore>() {});
    bind(RepositoryPermissionUpdater.class, DefaultRepositoryPermissionUpdater.class);

    // bind metrics
    bind(MeterRegistry.class).toProvider(MeterRegistryProvider.class).asEagerSingleton();
    Multibinder.newSetBinder(binder(), MonitoringSystem.class);
    Multibinder.newSetBinder(binder(), RepositoryLocationOverride.class);

    // bind cache
    bind(CacheManager.class, GuavaCacheManager.class);
    bind(org.apache.shiro.cache.CacheManager.class, GuavaCacheManager.class);
  }

  private <T> void bind(Class<T> clazz, Class<? extends T> defaultImplementation) {
    Class<? extends T> implementation = find(clazz, defaultImplementation);
    LOG.debug("bind {} to {}", clazz, implementation);
    bind(clazz).to(implementation);
  }

  private <T> Class<? extends T> find(Class<T> clazz, Class<? extends T> defaultImplementation) {
    Class<? extends T> implementation = overrides.getOverride(clazz);

    if (implementation != null) {
      LOG.info("found override {} for {}", implementation, clazz);
    } else {
      implementation = defaultImplementation;

      LOG.trace(
        "no override available for {}, using default implementation {}",
        clazz, implementation);
    }

    return implementation;
  }

  private static class NoOpConfigurationStoreDecoratorFactory implements ConfigurationStoreDecoratorFactory {
    @Override
    public <T> ConfigurationStore<T> createDecorator(ConfigurationStore<T> object, Context context) {
      return object;
    }
  }
}
