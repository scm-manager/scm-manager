/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.lifecycle.classloading;

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

  public static ClassLoaderLifeCycle create() {
    ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
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
