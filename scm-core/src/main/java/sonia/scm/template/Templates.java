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

import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

/**
 * Util class for the template framework of scm-manager.
 *
 * @author Sebastian Sdorra
 * @since 1.22
 */
public final class Templates
{

  /**
   * the logger for Templates
   */
  private static final Logger logger = LoggerFactory.getLogger(Templates.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private Templates() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Renders the given template file with the {@link TemplateEngine} which is
   * associated to extension of the file. If there is no {@link TemplateEngine}
   * for the extension of the file, then the default {@link TemplateEngine}
   * ({@link TemplateEngineFactory#getDefaultEngine()}) is used.
   *
   *
   * @param factory template engine factory
   * @param template file with template content
   * @param ouput writer for the template output
   * @param env environment for the template
   *
   * @throws IOException
   */
  public static void renderFileTemplate(TemplateEngineFactory factory,
    File template, Writer ouput, Object env)
    throws IOException
  {
    TemplateEngine engine = factory.getEngineByExtension(template.getPath());

    if (engine == null)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn(
          "could not find template engine for path {}, use default engine",
          template.getPath());
      }

      engine = factory.getDefaultEngine();
    }

    FileReader reader = null;

    try
    {
      reader = new FileReader(template);

      Template tpl = engine.getTemplate(template.getAbsolutePath(), reader);

      if (tpl != null)
      {
        tpl.execute(ouput, env);
      }
      else
      {

        // this should never happen
        throw new IOException("engine get template returned null");
      }
    }
    finally
    {
      Closeables.closeQuietly(reader);
    }

  }
}
