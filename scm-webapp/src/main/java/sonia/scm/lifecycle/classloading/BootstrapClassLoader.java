package sonia.scm.lifecycle.classloading;

/**
 * This ClassLoader is mainly a wrapper around the web application class loader and its goal is to make it easier to
 * find it in a heap dump.
 */
class BootstrapClassLoader extends ClassLoader {

  /**
   * Marker to find a BootstrapClassLoader, which is already shutdown.
   */
  private boolean shutdown = false;

  BootstrapClassLoader(ClassLoader webappClassLoader) {
    super(webappClassLoader);
  }

  /**
   * Returns {@code true} if the classloader was shutdown.
   *
   * @return {@code true} if the classloader was shutdown
   */
  boolean isShutdown() {
    return shutdown;
  }

  /**
   * Mark the class loader as shutdown.
   */
  void markAsShutdown() {
    shutdown = true;
  }
}
