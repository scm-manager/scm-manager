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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import static se.jiderhamn.classloader.leak.prevention.cleanup.ShutdownHookCleanUp.SHUTDOWN_HOOK_WAIT_MS_DEFAULT;

/**
 * Creates and shutdown SCM-Manager ClassLoaders with ClassLoader leak detection.
 */
final class ClassLoaderLifeCycleWithLeakPrevention extends ClassLoaderLifeCycle {

  private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderLifeCycleWithLeakPrevention.class);

  private Deque<ClassLoaderAndPreventor> classLoaders = new ArrayDeque<>();

  private final ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory;

  private ClassLoaderAppendListener classLoaderAppendListener = new ClassLoaderAppendListener() {
    @Override
    public <C extends ClassLoader> C apply(C classLoader) {
      return classLoader;
    }
  };

  ClassLoaderLifeCycleWithLeakPrevention(ClassLoader webappClassLoader) {
    this(webappClassLoader, createClassLoaderLeakPreventorFactory(webappClassLoader));
  }

  ClassLoaderLifeCycleWithLeakPrevention(ClassLoader webappClassLoader, ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory) {
    super(webappClassLoader);
    this.classLoaderLeakPreventorFactory = classLoaderLeakPreventorFactory;
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

    LOG.info("Settings for {} (CL: 0x{}):", ClassLoaderLifeCycleWithLeakPrevention.class.getName(), Integer.toHexString(System.identityHashCode(webappClassLoader)) );
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

  @VisibleForTesting
  void setClassLoaderAppendListener(ClassLoaderAppendListener classLoaderAppendListener) {
    this.classLoaderAppendListener = classLoaderAppendListener;
  }

  @Override
  protected void shutdownClassLoaders() {
    ClassLoaderAndPreventor clap = classLoaders.poll();
    while (clap != null) {
      clap.shutdown();
      clap = classLoaders.poll();
    }
    // be sure it is realy empty
    classLoaders.clear();
    classLoaders = new ArrayDeque<>();
  }

  @Override
  protected <T extends ClassLoader> T initAndAppend(T originalClassLoader) {
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

  private static class ClassLoaderAndPreventor {

    private final ClassLoader classLoader;
    private final ClassLoaderLeakPreventor preventor;

    private ClassLoaderAndPreventor(ClassLoader classLoader, ClassLoaderLeakPreventor preventor) {
      this.classLoader = classLoader;
      this.preventor = preventor;
    }

    void shutdown() {
      LOG.debug("shutdown classloader {}", classLoader);
      preventor.runCleanUps();
      close();
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
