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
  static final String PROPERTY = "sonia.scm.lifecycle.classloading";

  public static ClassLoaderLifeCycle create() {
    ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
    String implementation = System.getProperty(PROPERTY);
    if (ClassLoaderLifeCycleWithLeakPrevention.NAME.equalsIgnoreCase(implementation)) {
      LOG.info("create new ClassLoaderLifeCycle with leak prevention");
      return new ClassLoaderLifeCycleWithLeakPrevention(webappClassLoader);
    }
    LOG.info("create new simple ClassLoaderLifeCycle");
    return new SimpleClassLoaderLifeCycle(webappClassLoader);
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
