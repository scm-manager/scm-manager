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



package sonia.scm.api.rest;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.ws.rs.ext.Provider;

/**
 *
 * @author Sebastian Sdorra
 */
@Provider
public class TemplateEngineViewable implements ViewProcessor<String>
{

  /**
   * Constructs ...
   *
   *
   * @param templateEngineFactory
   */
  @Inject
  public TemplateEngineViewable(TemplateEngineFactory templateEngineFactory)
  {
    this.templateEngineFactory = templateEngineFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @Override
  public String resolve(String name)
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @param path
   * @param viewable
   * @param out
   *
   * @throws IOException
   */
  @Override
  public void writeTo(String path, Viewable viewable, OutputStream out)
    throws IOException
  {
    TemplateEngine engine = templateEngineFactory.getEngineByExtension(path);

    if (engine == null)
    {
      throw new IOException("could not find template engine for ".concat(path));
    }

    Template template = engine.getTemplate(path);

    if (template == null)
    {
      throw new IOException("could not find template for ".concat(path));
    }

    PrintWriter writer = new PrintWriter(out);

    template.execute(writer, viewable.getModel());
    writer.flush();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private TemplateEngineFactory templateEngineFactory;
}
