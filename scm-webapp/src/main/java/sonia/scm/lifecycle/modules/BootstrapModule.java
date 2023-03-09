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

package sonia.scm.lifecycle.modules;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.lifecycle.DefaultRestarter;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.DefaultRepositoryExportingCheck;
import sonia.scm.repository.EventDrivenRepositoryArchiveCheck;
import sonia.scm.repository.RepositoryArchivedCheck;
import sonia.scm.repository.RepositoryExportingCheck;
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
import sonia.scm.store.DefaultBlobDirectoryAccess;
import sonia.scm.store.FileBlobStoreFactory;
import sonia.scm.store.FileRepositoryUpdateIterator;
import sonia.scm.store.FileStoreUpdateStepUtilFactory;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;
import sonia.scm.store.JAXBConfigurationStoreFactory;
import sonia.scm.store.JAXBDataStoreFactory;
import sonia.scm.store.JAXBPropertyFileAccess;
import sonia.scm.update.BlobDirectoryAccess;
import sonia.scm.update.PropertyFileAccess;
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
    bind(PluginLoader.class).toInstance(pluginLoader);
    bind(V1PropertyDAO.class, XmlV1PropertyDAO.class);
    bind(PropertyFileAccess.class, JAXBPropertyFileAccess.class);
    bind(BlobDirectoryAccess.class, DefaultBlobDirectoryAccess.class);
    bind(RepositoryUpdateIterator.class, FileRepositoryUpdateIterator.class);
    bind(StoreUpdateStepUtilFactory.class, FileStoreUpdateStepUtilFactory.class);
    bind(new TypeLiteral<UpdateStepRepositoryMetadataAccess<Path>>() {}).to(new TypeLiteral<MetadataStore>() {});
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
