/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.lang.ref.WeakReference;

import java.net.URL;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link ClassLoader} which is able to load classes and resources from all
 * plugins.
 *
 * @author Sebastian Sdorra
 */
public final class UberClassLoader extends ClassLoader
{

  /**
   * Constructs ...
   *
   *
   * @param parent
   * @param plugins
   */
  public UberClassLoader(ClassLoader parent, Iterable<PluginWrapper> plugins)
  {
    super(parent);
    this.plugins = plugins;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   *
   * @throws ClassNotFoundException
   */
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException
  {
    Class<?> clazz = getFromCache(name);

    if (clazz == null)
    {
      for (PluginWrapper plugin : plugins)
      {
        ClassLoader cl = plugin.getClassLoader();

        // load class could be slow, perhaps we should call
        // find class via reflection ???
        clazz = cl.loadClass(name);

        if (clazz != null)
        {
          cache.put(name, new WeakReference<Class<?>>(clazz));

          break;
        }
      }
    }

    return clazz;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @Override
  protected URL findResource(String name)
  {
    URL url = null;

    for (PluginWrapper plugin : plugins)
    {
      ClassLoader cl = plugin.getClassLoader();

      url = cl.getResource(name);

      if (url != null)
      {
        break;
      }
    }

    return url;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  protected Enumeration<URL> findResources(String name) throws IOException
  {
    List<URL> urls = Lists.newArrayList();

    for (PluginWrapper plugin : plugins)
    {
      ClassLoader cl = plugin.getClassLoader();

      urls.addAll(Collections.list(cl.getResources(name)));
    }

    return Collections.enumeration(urls);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  private Class<?> getFromCache(String name)
  {
    Class<?> clazz = null;
    WeakReference<Class<?>> ref = cache.get(name);

    if (ref != null)
    {
      clazz = ref.get();

      if (clazz == null)
      {
        cache.remove(name);
      }
    }

    return clazz;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ConcurrentMap<String, WeakReference<Class<?>>> cache =
    Maps.newConcurrentMap();

  /** Field description */
  private final Iterable<PluginWrapper> plugins;
}
