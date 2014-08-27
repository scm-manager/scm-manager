/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;

import javax.servlet.ServletContext;

/**
 * Default implementation of the {@link UberWebResourceLoader}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public class DefaultUberWebResourceLoader implements UberWebResourceLoader
{

  /**
   * the logger for DefaultUberWebResourceLoader
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultUberWebResourceLoader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   * @param plugins
   */
  public DefaultUberWebResourceLoader(ServletContext servletContext,
    Iterable<PluginWrapper> plugins)
  {
    this.servletContext = servletContext;
    this.plugins = plugins;
    this.cache = CacheBuilder.newBuilder().build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  @Override
  public URL getResource(String path)
  {
    URL resource = cache.getIfPresent(path);

    if (resource == null)
    {
      resource = find(path);

      if (resource != null)
      {
        cache.put(path, resource);
      }
    }
    else
    {
      logger.trace("return web resource {} from cache", path);
    }

    return resource;
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  @Override
  public List<URL> getResources(String path)
  {

    // caching ???
    Builder<URL> resources = ImmutableList.builder();

    try
    {
      URL ctxResource = servletContext.getResource(path);

      if (ctxResource != null)
      {
        logger.trace("found path {} at ServletContext", path);
        resources.add(ctxResource);
      }

      for (PluginWrapper wrapper : plugins)
      {
        URL resource = wrapper.getWebResourceLoader().getResource(path);

        if (resource != null)
        {
          resources.add(resource);
        }
      }
    }
    catch (MalformedURLException ex)
    {

      // TODO define an extra exception
      throw Throwables.propagate(ex);
    }

    return resources.build();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @VisibleForTesting
  Cache<String, URL> getCache()
  {
    return cache;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private URL find(String path)
  {
    URL resource = null;

    try
    {
      resource = servletContext.getResource(path);

      if (resource == null)
      {
        for (PluginWrapper wrapper : plugins)
        {
          resource = wrapper.getWebResourceLoader().getResource(path);

          if (resource != null)
          {
            break;
          }
        }
      }
      else
      {
        logger.trace("found path {} at ServletContext", path);
      }
    }
    catch (MalformedURLException ex)
    {

      // TODO define an extra exception
      throw Throwables.propagate(ex);
    }

    return resource;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Cache<String, URL> cache;

  /** Field description */
  private final Iterable<PluginWrapper> plugins;

  /** Field description */
  private final ServletContext servletContext;
}
