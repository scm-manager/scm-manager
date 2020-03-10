/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

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

//~--- JDK imports ------------------------------------------------------------

/**
 * {@link ClassLoader} which is able to load classes and resources from all
 * plugins.
 *
 * @author Sebastian Sdorra
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
