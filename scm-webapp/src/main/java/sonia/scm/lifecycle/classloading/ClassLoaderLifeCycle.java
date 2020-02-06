package sonia.scm.lifecycle.classloading;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.lifecycle.LifeCycle;
import sonia.scm.plugin.ChildFirstPluginClassLoader;
import sonia.scm.plugin.DefaultPluginClassLoader;

import java.net.URL;

import static com.google.common.base.Preconditions.checkState;

/**
 * Base class for ClassLoader LifeCycle implementation in SCM-Manager.
 */
public abstract class ClassLoaderLifeCycle implements LifeCycle {

  private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderLifeCycle.class);

  @VisibleForTesting
  static final String PROPERTY = "sonia.scm.classloading.lifecycle";

  public static ClassLoaderLifeCycle create() {
    ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
    String implementation = System.getProperty(PROPERTY);
    if (SimpleClassLoaderLifeCycle.NAME.equalsIgnoreCase(implementation)) {
      LOG.info("create new simple ClassLoaderLifeCycle");
      return new SimpleClassLoaderLifeCycle(webappClassLoader);
    }
    LOG.info("create new ClassLoaderLifeCycle with leak prevention");
    return new ClassLoaderLifeCycleWithLeakPrevention(webappClassLoader);
  }

  private final ClassLoader webappClassLoader;

  private BootstrapClassLoader bootstrapClassLoader;

  ClassLoaderLifeCycle(ClassLoader webappClassLoader) {
    this.webappClassLoader = webappClassLoader;
  }

  @Override
  public void initialize() {
    bootstrapClassLoader = initAndAppend(new BootstrapClassLoader(webappClassLoader));
  }

  protected abstract <T extends ClassLoader> T initAndAppend(T classLoader);

  public ClassLoader getBootstrapClassLoader() {
    checkState(bootstrapClassLoader != null, "%s was not initialized", ClassLoaderLifeCycle.class.getName());
    return bootstrapClassLoader;
  }

  public ClassLoader createChildFirstPluginClassLoader(URL[] urls, ClassLoader parent, String plugin) {
    LOG.debug("create new ChildFirstPluginClassLoader for {}", plugin);
    ChildFirstPluginClassLoader pluginClassLoader = new ChildFirstPluginClassLoader(urls, parent, plugin);
    return initAndAppend(pluginClassLoader);
  }

  public ClassLoader createPluginClassLoader(URL[] urls, ClassLoader parent, String plugin) {
    LOG.debug("create new PluginClassLoader for {}", plugin);
    DefaultPluginClassLoader pluginClassLoader = new DefaultPluginClassLoader(urls, parent, plugin);
    return initAndAppend(pluginClassLoader);
  }

  @Override
  public void shutdown() {
    LOG.info("shutdown classloader infrastructure");
    shutdownClassLoaders();

    bootstrapClassLoader.markAsShutdown();
    bootstrapClassLoader = null;
  }

  protected abstract void shutdownClassLoaders();
}
