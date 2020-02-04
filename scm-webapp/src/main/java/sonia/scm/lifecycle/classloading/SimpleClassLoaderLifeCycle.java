package sonia.scm.lifecycle.classloading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Creates and shutdown SCM-Manager ClassLoaders with ClassLoader leak detection.
 */
class SimpleClassLoaderLifeCycle extends ClassLoaderLifeCycle {

  static final String NAME = "simple";

  private static final Logger LOG = LoggerFactory.getLogger(SimpleClassLoaderLifeCycle.class);

  private Deque<ClassLoader> classLoaders = new ArrayDeque<>();

  SimpleClassLoaderLifeCycle(ClassLoader webappClassLoader) {
    super(webappClassLoader);
  }

  @Override
  protected <T extends ClassLoader> T initAndAppend(T classLoader) {
    LOG.debug("init classloader {}", classLoader);
    classLoaders.push(classLoader);
    return classLoader;
  }

  @Override
  protected void shutdownClassLoaders() {
    ClassLoader classLoader = classLoaders.poll();
    while (classLoader != null) {
      shutdown(classLoader);
      classLoader = classLoaders.poll();
    }
    // be sure it is realy empty
    classLoaders.clear();
    classLoaders = new ArrayDeque<>();
  }

  private void shutdown(ClassLoader classLoader) {
    LOG.debug("shutdown classloader {}", classLoader);
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
