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



package sonia.scm.resources;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.boot.BootstrapUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

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
   *
   * @param servletContext
   * @param resources
   * @param resourceHandlers
   */
  public AbstractResource(ServletContext servletContext,
                          List<String> resources,
                          List<ResourceHandler> resourceHandlers)
  {
    this.servletContext = servletContext;
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
      Collections.sort(resourceHandlers, new ResourceHandlerComparator());

      for (ResourceHandler resourceHandler : resourceHandlers)
      {
        if (resourceHandler.getType() == ResourceType.SCRIPT)
        {
          appendResource(resourceHandler.getResource(), stream);
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param stream
   * @param resource
   *
   * @throws IOException
   */
  private void appendResource(OutputStream stream, String resource)
          throws IOException
  {
    InputStream input = getResourceAsStream(resource);

    if (input != null)
    {
      appendResource(input, stream);
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find resource {}", resource);
    }
  }

  /**
   * Method description
   *
   *
   * @param input
   * @param stream
   *
   * @throws IOException
   */
  private void appendResource(InputStream input, OutputStream stream)
          throws IOException
  {
    if (input != null)
    {
      try
      {
        IOUtil.copy(input, stream);
      }
      finally
      {
        IOUtil.close(input);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param resource
   *
   * @return
   */
  private InputStream getResourceAsStream(String resource)
  {
    InputStream input = null;
    ClassLoader classLoader = BootstrapUtil.getClassLoader(servletContext);

    if (classLoader != null)
    {
      String classLoaderResource = resource;

      if (classLoaderResource.startsWith("/"))
      {
        classLoaderResource = classLoaderResource.substring(1);
      }

      input = classLoader.getResourceAsStream(classLoaderResource);
    }

    if (input == null)
    {
      input = ScriptResourceServlet.class.getResourceAsStream(resource);
    }

    return input;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected List<ResourceHandler> resourceHandlers;

  /** Field description */
  protected List<String> resources;

  /** Field description */
  private ServletContext servletContext;
}
