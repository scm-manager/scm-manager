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

package sonia.scm.template;


import com.github.mustachejava.DefaultMustacheFactory;
import com.google.common.base.Charsets;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


public class ServletMustacheFactory extends DefaultMustacheFactory
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(ServletMustacheFactory.class);

  private ServletContext servletContext;

  private ClassLoader classLoader;
 
  public ServletMustacheFactory(ServletContext servletContext, ClassLoader classLoader)
  {
    this.servletContext = servletContext;
    this.classLoader = classLoader;
  }



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

}
