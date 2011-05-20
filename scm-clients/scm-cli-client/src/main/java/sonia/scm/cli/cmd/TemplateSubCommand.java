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



package sonia.scm.cli.cmd;

//~--- non-JDK imports --------------------------------------------------------

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.kohsuke.args4j.Option;

import sonia.scm.ConfigurationException;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class TemplateSubCommand extends SubCommand
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getTemplate()
  {
    return template;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public File getTemplateFile()
  {
    return templateFile;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param template
   */
  public void setTemplate(String template)
  {
    this.template = template;
  }

  /**
   * Method description
   *
   *
   * @param templateFile
   */
  public void setTemplateFile(File templateFile)
  {
    this.templateFile = templateFile;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param env
   * @param defaultTemplate
   */
  protected void renderTemplate(Map<String, Object> env, String defaultTemplate)
  {
    Configuration configuration = new Configuration();
    Reader reader = null;

    try
    {
      if ((templateFile != null) && templateFile.exists())
      {
        reader = new FileReader(templateFile);
      }
      else if (Util.isNotEmpty(template))
      {
        reader = new StringReader(template);
      }
      else
      {
        reader = new InputStreamReader(
            TemplateSubCommand.class.getResourceAsStream(defaultTemplate));
      }

      Template tpl = new Template("default-template", reader, configuration);

      tpl.process(env, output);
    }
    catch (TemplateException ex)
    {
      throw new ConfigurationException("could not render template", ex);
    }
    catch (IOException ex)
    {
      throw new ConfigurationException("could not render template", ex);
    }
    finally
    {
      IOUtil.close(reader);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Option(
    name = "--template",
    usage = "optionTemplate",
    aliases = { "-t" }
  )
  private String template;

  /** Field description */
  @Option(
    name = "--template-file",
    usage = "optionTemplateFile",
    aliases = { "-f" }
  )
  private File templateFile;
}
