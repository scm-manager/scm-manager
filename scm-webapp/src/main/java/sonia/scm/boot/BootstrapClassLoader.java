package sonia.scm.boot;

/**
 * This ClassLoader is mainly a wrapper around the web application class loader and its goal is to make it easier to
 * find it in a heap dump.
 */
class BootstrapClassLoader extends ClassLoader {
  BootstrapClassLoader(ClassLoader webappClassLoader) {
    super(webappClassLoader);
  }
}
