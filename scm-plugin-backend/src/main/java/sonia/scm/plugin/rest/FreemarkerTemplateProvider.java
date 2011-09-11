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



package sonia.scm.plugin.rest;

//~--- non-JDK imports --------------------------------------------------------

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.ServletContext;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Sebastian Sdorra
 */
@Provider
public class FreemarkerTemplateProvider implements ViewProcessor<String>
{

  /** Field description */
  public static final String DIRECTORY_TEMPLATES = "/";

  /** Field description */
  private static final String EXTENSION = ".ftl";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  public FreemarkerTemplateProvider(@Context ServletContext servletContext)
  {
    configuration = new Configuration();
    configuration.setServletContextForTemplateLoading(servletContext,
            DIRECTORY_TEMPLATES);
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
  @Override
  public String resolve(String path)
  {
    if (!path.endsWith(EXTENSION))
    {
      path = path.concat(EXTENSION);
    }

    return path;
  }

  /**
   * Method description
   *
   *
   *
   * @param resolvedPath
   * @param viewable
   * @param out
   *
   * @throws IOException
   */
  @Override
  public void writeTo(String resolvedPath, Viewable viewable, OutputStream out)
          throws IOException
  {

    // Commit the status and headers to the HttpServletResponse
    out.flush();

    final Template template = configuration.getTemplate(resolvedPath);

    try
    {
      template.process(viewable.getModel(), new OutputStreamWriter(out));
    }
    catch (TemplateException te)
    {
      throw new ContainerException(te);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Configuration configuration;

  /** Field description */
  private String basePath;

  /** Field description */
  private @Context
  UriInfo uriInfo;
}
