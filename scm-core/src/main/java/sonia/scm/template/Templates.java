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
