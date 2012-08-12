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



package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.github.mustachejava.DefaultMustacheFactory;

import com.google.common.base.Charsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class ServletMustacheFactory extends DefaultMustacheFactory
{

  /**
   * the logger for ServletMustacheFactory
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ServletMustacheFactory.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  public ServletMustacheFactory(ServletContext servletContext)
  {
    this.servletContext = servletContext;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param resourceName
   *
   * @return
   */
  @Override
  public Reader getReader(String resourceName)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("try to find resource for {}", resourceName);
    }

    Reader reader = null;
    InputStream is = servletContext.getResourceAsStream(resourceName);

    if (is == null)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("could not find resource {} in ServletContext",
          resourceName);
      }

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      if (classLoader == null)
      {
        classLoader = ServletMustacheFactory.class.getClassLoader();
      }

      is = classLoader.getResourceAsStream(resourceName);
    }

    if (is != null)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("found resoruce for {}, return reader", resourceName);
      }

      reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find resource {}", resourceName);
    }

    return reader;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ServletContext servletContext;
}
