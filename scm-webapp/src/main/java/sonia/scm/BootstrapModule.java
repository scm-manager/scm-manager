package sonia.scm;

import com.google.inject.AbstractModule;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.plugin.DefaultPluginLoader;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.security.CipherUtil;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.FileBlobStoreFactory;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;
import sonia.scm.store.JAXBConfigurationStoreFactory;
import sonia.scm.store.JAXBDataStoreFactory;
import sonia.scm.util.ScmConfigurationUtil;

public class BootstrapModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(BootstrapModule.class);

  private final ClassOverrides overrides;
  private final DefaultPluginLoader pluginLoader;

  public BootstrapModule(ClassOverrides overrides, DefaultPluginLoader pluginLoader) {
    this.overrides = overrides;
    this.pluginLoader = pluginLoader;
  }

  @Override
  protected void configure() {
    install(ThrowingProviderBinder.forModule(this));

    ScmConfiguration config = getScmConfiguration();

    CipherUtil cu = CipherUtil.getInstance();

    SCMContextProvider context = SCMContext.getContext();

    bind(SCMContextProvider.class).toInstance(context);

    bind(KeyGenerator.class).to(DefaultKeyGenerator.class);

    bind(RepositoryLocationResolver.class).to(PathBasedRepositoryLocationResolver.class);

    bind(RepositoryDAO.class, XmlRepositoryDAO.class);

    bind(FileSystem.class, DefaultFileSystem.class);


    // bind core
    bind(ConfigurationStoreFactory.class, JAXBConfigurationStoreFactory.class);
    bind(ConfigurationEntryStoreFactory.class, JAXBConfigurationEntryStoreFactory.class);
    bind(DataStoreFactory.class, JAXBDataStoreFactory.class);
    bind(BlobStoreFactory.class, FileBlobStoreFactory.class);

    //    bind(ScmConfiguration.class).toInstance(config);
//    bind(PluginLoader.class).toInstance(pluginLoader);
//    bind(PluginManager.class, DefaultPluginManager.class);

    // note CipherUtil uses an other generator
//    bind(KeyGenerator.class).to(DefaultKeyGenerator.class);
//    bind(CipherHandler.class).toInstance(cu.getCipherHandler());
//    bind(FileSystem.class, DefaultFileSystem.class);
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

  private ScmConfiguration getScmConfiguration() {
    ScmConfiguration configuration = new ScmConfiguration();

    ScmConfigurationUtil.getInstance().load(configuration);

    return configuration;
  }
}
