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

package sonia.scm.plugin;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link ClassLoader} which is able to load classes and resources from all
 * plugins.
 *
 */
public final class UberClassLoader extends ClassLoader {

  private final Set<ClassLoader> pluginClassLoaders;
  private final ConcurrentMap<String, WeakReference<Class<?>>> cache = Maps.newConcurrentMap();

  public UberClassLoader(ClassLoader parent, Iterable<InstalledPlugin> plugins) {
    this(parent, collectClassLoaders(plugins));
  }

  private static Set<ClassLoader> collectClassLoaders(Iterable<InstalledPlugin> plugins) {
    ImmutableSet.Builder<ClassLoader> classLoaders = ImmutableSet.builder();
    plugins.forEach(plugin -> classLoaders.add(plugin.getClassLoader()));
    return classLoaders.build();
  }

  @VisibleForTesting
  UberClassLoader(ClassLoader parent, Set<ClassLoader> pluginClassLoaders) {
    super(parent);
    this.pluginClassLoaders = pluginClassLoaders;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    Class<?> clazz = getFromCache(name);

    if (clazz == null) {
      clazz = findClassInPlugins(name);
      cache.put(name, new WeakReference<>(clazz));
    }

    return clazz;
  }

  private Class<?> findClassInPlugins(String name) throws ClassNotFoundException {
    for (ClassLoader pluginClassLoader : pluginClassLoaders) {
      Class<?> clazz = findClass(pluginClassLoader, name);
      if (clazz != null) {
        return clazz;
      }
    }
    throw new ClassNotFoundException("could not find class " + name + " in any of the installed plugins");
  }

  private Class<?> findClass(ClassLoader classLoader, String name) {
    try {
      // load class could be slow, perhaps we should call
      // find class via reflection ???
      return classLoader.loadClass(name);
    } catch (ClassNotFoundException ex) {
      return null;
    }
  }

  @Override
  protected URL findResource(String name) {
    URL url = null;

    for (ClassLoader pluginClassLoader : pluginClassLoaders) {
      url = pluginClassLoader.getResource(name);

      if (url != null) {
        break;
      }
    }

    return url;
  }

  @Override
  @SuppressWarnings("squid:S2112")
  protected Enumeration<URL> findResources(String name) throws IOException {
    Set<URL> urls = new LinkedHashSet<>();

    for (ClassLoader pluginClassLoader : pluginClassLoaders) {
      urls.addAll(Collections.list(pluginClassLoader.getResources(name)));
    }

    return Collections.enumeration(urls);
  }

  private Class<?> getFromCache(String name) {
    Class<?> clazz = null;
    WeakReference<Class<?>> ref = cache.get(name);

    if (ref != null) {
      clazz = ref.get();

      if (clazz == null) {
        cache.remove(name);
      }
    }

    return clazz;
  }

}
