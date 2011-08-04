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



package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class FreemarkerTemplateHandler implements TemplateHandler
{

  /** Field description */
  public static final String DIRECTORY_TEMPLATES = "/";

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  public static final String EXTENSION = ".html";

  /** the logger for FreemarkerTemplateHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(FreemarkerTemplateHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  @Inject
  public FreemarkerTemplateHandler(ServletContext servletContext)
  {
    configuration = new Configuration();
    configuration.setServletContextForTemplateLoading(servletContext,
            DIRECTORY_TEMPLATES);
    configuration.setEncoding(Locale.ENGLISH, ENCODING);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param templateName
   * @param writer
   * @param params
   *
   * @throws IOException
   */
  @Override
  public void render(String templateName, Writer writer,
                     Map<String, ? extends Object> params)
          throws IOException
  {
    if (!templateName.endsWith(EXTENSION))
    {
      templateName = templateName.concat(EXTENSION);
    }

    Template template = configuration.getTemplate(templateName, ENCODING);

    if (template == null)
    {
      throw new FileNotFoundException(
          "file ".concat(templateName).concat(" not found"));
    }
    else
    {
      try
      {
        template.process(params, writer);
      }
      catch (TemplateException ex)
      {
        logger.error("could not render template ".concat(templateName), ex);

        throw new IOException(ex);
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Configuration configuration;
}
