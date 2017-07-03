/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractResource implements Resource
{

  /**
   * the logger for AbstractResource
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param pluginLoader
   * @param resources
   * @param resourceHandlers
   */
  public AbstractResource(PluginLoader pluginLoader, List<String> resources,
    List<ResourceHandler> resourceHandlers)
  {
    this.pluginLoader = pluginLoader;
    this.resources = resources;
    this.resourceHandlers = resourceHandlers;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   *
   * @throws IOException
   */
  protected void appendResources(OutputStream stream) throws IOException
  {
    if (Util.isNotEmpty(resources))
    {
      for (String resource : resources)
      {
        appendResource(stream, resource);
      }
    }

    if (Util.isNotEmpty(resourceHandlers))
    {
      resourceHandlers.sort(new ResourceHandlerComparator());

      for (ResourceHandler resourceHandler : resourceHandlers)
      {
        processResourceHandler(stream, resourceHandler);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param stream
   * @param path
   *
   * @throws IOException
   */
  private void appendResource(OutputStream stream, String path)
    throws IOException
  {
    URL resource = getResourceAsURL(path);

    if (resource != null)
    {
      Resources.copy(resource, stream);
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find resource {}", path);
    }
  }

  /**
   * Method description
   *
   *
   * @param stream
   * @param resourceHandler
   *
   * @throws IOException
   */
  private void processResourceHandler(OutputStream stream,
    ResourceHandler resourceHandler)
    throws IOException
  {
    if (resourceHandler.getType() == getType())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("process resource handler {}", resourceHandler.getClass());
      }

      URL resource = resourceHandler.getResource();

      if (resource != null)
      {
        Resources.copy(resource, stream);
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("resource handler {} does not return a resource",
          resourceHandler.getClass());
      }
    }
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
  private URL getResourceAsURL(String path)
  {
    URL resource = null;
    ClassLoader classLoader = pluginLoader.getUberClassLoader();

    if (classLoader != null)
    {
      String classLoaderResource = path;

      if (classLoaderResource.startsWith("/"))
      {
        classLoaderResource = classLoaderResource.substring(1);
      }

      resource = classLoader.getResource(classLoaderResource);
    }

    return resource;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected final List<ResourceHandler> resourceHandlers;

  /** Field description */
  protected final List<String> resources;

  /** Field description */
  private final PluginLoader pluginLoader;
}
