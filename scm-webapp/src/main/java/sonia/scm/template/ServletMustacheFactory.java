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
    
package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.github.mustachejava.DefaultMustacheFactory;
import com.google.common.base.Charsets;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

//~--- JDK imports ------------------------------------------------------------

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
   * @param classLoader
   */
  public ServletMustacheFactory(ServletContext servletContext, ClassLoader classLoader)
  {
    this.servletContext = servletContext;
    this.classLoader = classLoader;
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

      if (resourceName.startsWith("/"))
      {
        resourceName = resourceName.substring(1);
      }

      is = classLoader.getResourceAsStream(resourceName);
    }

    if (is != null)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("found resource for {}, return reader", resourceName);
      }

      reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find resource {}", resourceName);
    }

    if (reader == null)
    {
      throw new MustacheTemplateNotFoundException(
        "could not find template for resource ".concat(resourceName));
    }

    return reader;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ServletContext servletContext;

  private ClassLoader classLoader;
}
