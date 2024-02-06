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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

/**
 * Util class for the template framework of scm-manager.
 *
 * @since 1.22
 */
public final class Templates
{
  private static final Logger logger = LoggerFactory.getLogger(Templates.class);


  private Templates() {}


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
      IOUtil.close(reader);
    }

  }
}
