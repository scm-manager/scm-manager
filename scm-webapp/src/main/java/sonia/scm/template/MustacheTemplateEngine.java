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

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Reader;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
@DefaultEngine
public class MustacheTemplateEngine implements TemplateEngine
{

  /** Field description */
  public static final TemplateType TYPE = new TemplateType("mustache",
                                            "Mustache", "mustache");

  /** Field description */
  private static final String THREAD_NAME = "Mustache-%s";

  /**
   * the logger for MustacheTemplateEngine
   */
  private static final Logger logger =
    LoggerFactory.getLogger(MustacheTemplateEngine.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   */
  @Inject
  public MustacheTemplateEngine(ServletContext context)
  {
    factory = new ServletMustacheFactory(context);

    ThreadFactory threadFactory =
      new ThreadFactoryBuilder().setNameFormat(THREAD_NAME).build();

    factory.setExecutorService(Executors.newCachedThreadPool(threadFactory));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param templateIdentifier
   * @param reader
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public Template getTemplate(String templateIdentifier, Reader reader)
    throws IOException
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("try to create mustache template from reader with id {}",
        templateIdentifier);
    }

    Mustache mustache = factory.compile(reader, templateIdentifier);

    return new MustacheTemplate(templateIdentifier, mustache);
  }

  /**
   * Method description
   *
   *
   * @param templatePath
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public Template getTemplate(String templatePath) throws IOException
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("try to find mustache template at {}", templatePath);
    }

    Template template = null;

    try
    {

      Mustache mustache = factory.compile(templatePath);

      if (mustache != null)
      {
        if (logger.isTraceEnabled())
        {
          logger.trace("return mustache template for {}", templatePath);
        }

        template = new MustacheTemplate(templatePath, mustache);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find mustache template at {}", templatePath);
      }

    }
    catch (MustacheTemplateNotFoundException ex)
    {
      logger.warn("could not find mustache template at {}", templatePath);
    }
    catch (UncheckedExecutionException ex)
    {
      handleWrappedException(ex, templatePath);
    }
    catch (MustacheException ex)
    {
      handleWrappedException(ex, templatePath);
    }
    catch (Exception ex)
    {
      Throwables.propagateIfInstanceOf(ex, IOException.class);

      throw new TemplateParseException(
        "could not parse template for resource ".concat(templatePath), ex);
    }

    return template;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public TemplateType getType()
  {
    return TYPE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ex
   * @param templatePath
   *
   * @throws IOException
   */
  private void handleWrappedException(Exception ex, String templatePath)
    throws IOException
  {
    Throwable cause = ex.getCause();

    if (cause instanceof MustacheTemplateNotFoundException)
    {
      logger.warn("could not find mustache template at {}", templatePath);
    }
    else
    {
      Throwables.propagateIfInstanceOf(cause, IOException.class);

      throw new TemplateParseException(
        "could not parse template for resource ".concat(templatePath), cause);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ServletMustacheFactory factory;
}
