package sonia.scm.lifecycle.classloading;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventor;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventorFactory;
import se.jiderhamn.classloader.leak.prevention.cleanup.MBeanCleanUp;
import sonia.scm.lifecycle.LifeCycle;
import sonia.scm.plugin.ChildFirstPluginClassLoader;
import sonia.scm.plugin.DefaultPluginClassLoader;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.UnaryOperator;

import static com.google.common.base.Preconditions.checkState;

/**
 * Creates and shutdown SCM-Manager ClassLoaders.
 */
public final class ClassLoaderLifeCycle implements LifeCycle {

  private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderLifeCycle.class);

  private final Deque<ClassLoaderAndPreventor> classLoaders = new ArrayDeque<>();

  private final ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory;
  private final ClassLoader webappClassLoader;

  private ClassLoader bootstrapClassLoader;
  private UnaryOperator<ClassLoader> classLoaderAppendListener = c -> c;

  @VisibleForTesting
  public static ClassLoaderLifeCycle create() {
    ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory = new ClassLoaderLeakPreventorFactory();
    classLoaderLeakPreventorFactory.setLogger(new LoggingAdapter());
    classLoaderLeakPreventorFactory.removeCleanUp(MBeanCleanUp.class);
    return new ClassLoaderLifeCycle(Thread.currentThread().getContextClassLoader(), classLoaderLeakPreventorFactory);
  }

  ClassLoaderLifeCycle(ClassLoader webappClassLoader, ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory) {
    this.classLoaderLeakPreventorFactory = classLoaderLeakPreventorFactory;
    this.webappClassLoader = initAndAppend(webappClassLoader);
  }

  public void initialize() {
    bootstrapClassLoader = initAndAppend(new BootstrapClassLoader(webappClassLoader));
  }

  @VisibleForTesting
  void setClassLoaderAppendListener(UnaryOperator<ClassLoader> classLoaderAppendListener) {
    this.classLoaderAppendListener = classLoaderAppendListener;
  }

  public ClassLoader getBootstrapClassLoader() {
    checkState(bootstrapClassLoader != null, "%s was not initialized", ClassLoaderLifeCycle.class.getName());
    return bootstrapClassLoader;
  }

  public ClassLoader createPluginClassLoader(URL[] urls, ClassLoader parent, String plugin) {
    LOG.debug("create new PluginClassLoader for {}", plugin);
    DefaultPluginClassLoader pluginClassLoader = new DefaultPluginClassLoader(urls, parent, plugin);
    return initAndAppend(pluginClassLoader);
  }

  public ClassLoader createChildFirstPluginClassLoader(URL[] urls, ClassLoader parent, String plugin) {
    LOG.debug("create new ChildFirstPluginClassLoader for {}", plugin);
    ChildFirstPluginClassLoader pluginClassLoader = new ChildFirstPluginClassLoader(urls, parent, plugin);
    return initAndAppend(pluginClassLoader);
  }

  public void shutdown() {
    LOG.info("shutdown classloader infrastructure");
    ClassLoaderAndPreventor clap = classLoaders.poll();
    while (clap != null) {
      clap.shutdown();
      clap = classLoaders.poll();
    }
    bootstrapClassLoader = null;
  }

  private ClassLoader initAndAppend(ClassLoader originalClassLoader) {
    LOG.debug("init classloader {}", originalClassLoader);
    ClassLoader classLoader = classLoaderAppendListener.apply(originalClassLoader);

    ClassLoaderLeakPreventor preventor = classLoaderLeakPreventorFactory.newLeakPreventor(classLoader);
    preventor.runPreClassLoaderInitiators();
    classLoaders.push(new ClassLoaderAndPreventor(classLoader, preventor));

    return classLoader;
  }

  private class ClassLoaderAndPreventor {

    private final ClassLoader classLoader;
    private final ClassLoaderLeakPreventor preventor;

    private ClassLoaderAndPreventor(ClassLoader classLoader, ClassLoaderLeakPreventor preventor) {
      this.classLoader = classLoader;
      this.preventor = preventor;
    }

    void shutdown() {
      LOG.debug("shutdown classloader {}", classLoader);
      preventor.runCleanUps();

      if (classLoader != webappClassLoader) {
        close();
      }
    }

    private void close() {
      if (classLoader instanceof Closeable) {
        LOG.trace("close classloader {}", classLoader);
        try {
          ((Closeable) classLoader).close();
        } catch (IOException e) {
          LOG.warn("failed to close classloader", e);
        }
      }
    }
  }
}
