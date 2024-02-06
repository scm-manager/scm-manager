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
    
package sonia.scm.plugin;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.Stage;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Default implementation of the {@link UberWebResourceLoader}.
 *
 * @since 2.0.0
 */
public class DefaultUberWebResourceLoader implements UberWebResourceLoader
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultUberWebResourceLoader.class);

  private final Cache<String, URL> cache;

  private final Iterable<InstalledPlugin> plugins;

  private final ServletContext servletContext;

  public DefaultUberWebResourceLoader(ServletContext servletContext, Iterable<InstalledPlugin> plugins) {
    this(servletContext, plugins, SCMContext.getContext().getStage());
  }

  public DefaultUberWebResourceLoader(ServletContext servletContext, Iterable<InstalledPlugin> plugins, Stage stage) {
    this.servletContext = servletContext;
    this.plugins = plugins;
    this.cache = createCache(stage);
  }

  private Cache<String, URL> createCache(Stage stage) {
    if (stage == Stage.DEVELOPMENT) {
      return CacheBuilder.newBuilder().maximumSize(0).build(); // Disable caching
    }
    return CacheBuilder.newBuilder().build();
  }



  @Override
  public URL getResource(String path)
  {
    URL resource = getFromCache(path);

    if (resource == null)
    {
      resource = find(path);

      if (resource != null)
      {
        addToCache(path, resource);
      }
    }
    else
    {
      logger.trace("return web resource {} from cache", path);
    }

    return resource;
  }

  private URL getFromCache(String path) {
    return cache.getIfPresent(path);
  }

  private void addToCache(String path, URL url) {
    cache.put(path, url);
  }


  @Override
  public List<URL> getResources(String path)
  {

    // caching ???
    Builder<URL> resources = ImmutableList.builder();

    try
    {
      URL ctxResource = nonDirectory(servletContext.getResource(path));

      if (ctxResource != null)
      {
        logger.trace("found path {} at ServletContext", path);
        resources.add(ctxResource);
      }

      for (InstalledPlugin wrapper : plugins)
      {
        URL resource = nonDirectory(wrapper.getWebResourceLoader().getResource(path));

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

  
  @VisibleForTesting
  Cache<String, URL> getCache()
  {
    return cache;
  }



  private URL find(String path)
  {
    URL resource;

    try
    {
      resource = nonDirectory(servletContext.getResource(path));

      if (resource == null)
      {
        for (InstalledPlugin wrapper : plugins)
        {
          resource = nonDirectory(wrapper.getWebResourceLoader().getResource(path));

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

  private URL nonDirectory(URL url) {
    if (url == null) {
      return null;
    }

    if (isDirectory(url)) {
      return null;
    }

    return url;
  }

  private boolean isDirectory(URL url) {
    if ("file".equals(url.getProtocol())) {
      try {
        return Files.isDirectory(Paths.get(url.toURI()));
      } catch (URISyntaxException ex) {
        throw Throwables.propagate(ex);
      }
    }
    return false;
  }

}
