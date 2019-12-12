package sonia.scm.lifecycle.classloading;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventor;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventorFactory;
import se.jiderhamn.classloader.leak.prevention.cleanup.IIOServiceProviderCleanUp;
import se.jiderhamn.classloader.leak.prevention.cleanup.MBeanCleanUp;
import se.jiderhamn.classloader.leak.prevention.cleanup.ShutdownHookCleanUp;
import se.jiderhamn.classloader.leak.prevention.cleanup.StopThreadsCleanUp;
import se.jiderhamn.classloader.leak.prevention.preinit.AwtToolkitInitiator;
import se.jiderhamn.classloader.leak.prevention.preinit.Java2dDisposerInitiator;
import se.jiderhamn.classloader.leak.prevention.preinit.Java2dRenderQueueInitiator;
import se.jiderhamn.classloader.leak.prevention.preinit.SunAwtAppContextInitiator;
import sonia.scm.lifecycle.LifeCycle;
import sonia.scm.plugin.ChildFirstPluginClassLoader;
import sonia.scm.plugin.DefaultPluginClassLoader;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

import static com.google.common.base.Preconditions.checkState;
import static se.jiderhamn.classloader.leak.prevention.cleanup.ShutdownHookCleanUp.SHUTDOWN_HOOK_WAIT_MS_DEFAULT;

/**
 * Creates and shutdown SCM-Manager ClassLoaders.
 */
public final class ClassLoaderLifeCycle implements LifeCycle {

  private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderLifeCycle.class);

  private Deque<ClassLoaderAndPreventor> classLoaders = new ArrayDeque<>();

  private final ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory;
  private final ClassLoader webappClassLoader;

  private BootstrapClassLoader bootstrapClassLoader;

  private ClassLoaderAppendListener classLoaderAppendListener = new ClassLoaderAppendListener() {
    @Override
    public <C extends ClassLoader> C apply(C classLoader) {
      return classLoader;
    }
  };

  @VisibleForTesting
  public static ClassLoaderLifeCycle create() {
    ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
    ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory = createClassLoaderLeakPreventorFactory(webappClassLoader);
    return new ClassLoaderLifeCycle(webappClassLoader, classLoaderLeakPreventorFactory);
  }

  ClassLoaderLifeCycle(ClassLoader webappClassLoader, ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory) {
    this.classLoaderLeakPreventorFactory = classLoaderLeakPreventorFactory;
    this.webappClassLoader = initAndAppend(webappClassLoader);
  }

  private static ClassLoaderLeakPreventorFactory createClassLoaderLeakPreventorFactory(ClassLoader webappClassLoader) {
    // Should threads tied to the web app classloader be forced to stop at application shutdown?
    boolean stopThreads = Boolean.getBoolean("ClassLoaderLeakPreventor.stopThreads");

    // Should Timer threads tied to the web app classloader be forced to stop at application shutdown?
    boolean stopTimerThreads = Boolean.getBoolean("ClassLoaderLeakPreventor.stopTimerThreads");

    // Should shutdown hooks registered from the application be executed at application shutdown?
    boolean executeShutdownHooks = Boolean.getBoolean("ClassLoaderLeakPreventor.executeShutdownHooks");

    // No of milliseconds to wait for threads to finish execution, before stopping them.
    int threadWaitMs = Integer.getInteger("ClassLoaderLeakPreventor.threadWaitMs", ClassLoaderLeakPreventor.THREAD_WAIT_MS_DEFAULT);

    /*
     * No of milliseconds to wait for shutdown hooks to finish execution, before stopping them.
     * If set to -1 there will be no waiting at all, but Thread is allowed to run until finished.
     */
    int shutdownHookWaitMs = Integer.getInteger("ClassLoaderLeakPreventor.shutdownHookWaitMs", SHUTDOWN_HOOK_WAIT_MS_DEFAULT);

    LOG.info("Settings for {} (CL: 0x{}):", ClassLoaderLifeCycle.class.getName(), Integer.toHexString(System.identityHashCode(webappClassLoader)) );
    LOG.info("  stopThreads = {}", stopThreads);
    LOG.info("  stopTimerThreads = {}", stopTimerThreads);
    LOG.info("  executeShutdownHooks = {}", executeShutdownHooks);
    LOG.info("  threadWaitMs = {} ms", threadWaitMs);
    LOG.info("  shutdownHookWaitMs = {} ms", shutdownHookWaitMs);

    // use webapp classloader as safe base? or system?
    ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory = new ClassLoaderLeakPreventorFactory(webappClassLoader);
    classLoaderLeakPreventorFactory.setLogger(new LoggingAdapter());

    final ShutdownHookCleanUp shutdownHookCleanUp = classLoaderLeakPreventorFactory.getCleanUp(ShutdownHookCleanUp.class);
    shutdownHookCleanUp.setExecuteShutdownHooks(executeShutdownHooks);
    shutdownHookCleanUp.setShutdownHookWaitMs(shutdownHookWaitMs);

    final StopThreadsCleanUp stopThreadsCleanUp = classLoaderLeakPreventorFactory.getCleanUp(StopThreadsCleanUp.class);
    stopThreadsCleanUp.setStopThreads(stopThreads);
    stopThreadsCleanUp.setStopTimerThreads(stopTimerThreads);
    stopThreadsCleanUp.setThreadWaitMs(threadWaitMs);

    // remove awt and imageio cleanup
    classLoaderLeakPreventorFactory.removePreInitiator(AwtToolkitInitiator.class);
    classLoaderLeakPreventorFactory.removePreInitiator(SunAwtAppContextInitiator.class);
    classLoaderLeakPreventorFactory.removeCleanUp(IIOServiceProviderCleanUp.class);
    classLoaderLeakPreventorFactory.removePreInitiator(Java2dRenderQueueInitiator.class);
    classLoaderLeakPreventorFactory.removePreInitiator(Java2dDisposerInitiator.class);

    // the MBeanCleanUp causes a Exception and we use no mbeans
    classLoaderLeakPreventorFactory.removeCleanUp(MBeanCleanUp.class);

    return classLoaderLeakPreventorFactory;
  }

  public void initialize() {
    bootstrapClassLoader = initAndAppend(new BootstrapClassLoader(webappClassLoader));
  }

  @VisibleForTesting
  void setClassLoaderAppendListener(ClassLoaderAppendListener classLoaderAppendListener) {
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
    // be sure it is realy empty
    classLoaders.clear();
    classLoaders = new ArrayDeque<>();

    bootstrapClassLoader.markAsShutdown();
    bootstrapClassLoader = null;
  }

  private <T extends ClassLoader> T initAndAppend(T originalClassLoader) {
    LOG.debug("init classloader {}", originalClassLoader);
    T classLoader = classLoaderAppendListener.apply(originalClassLoader);

    ClassLoaderLeakPreventor preventor = classLoaderLeakPreventorFactory.newLeakPreventor(classLoader);
    preventor.runPreClassLoaderInitiators();
    classLoaders.push(new ClassLoaderAndPreventor(classLoader, preventor));

    return classLoader;
  }

  interface ClassLoaderAppendListener {
    <C extends ClassLoader> C apply(C classLoader);
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
