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



package sonia.scm.boot;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.ScmContextListener;
import sonia.scm.plugin.PluginException;
import sonia.scm.plugin.PluginLoadException;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.plugin.Plugins;
import sonia.scm.util.ClassLoaders;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;

import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author Sebastian Sdorra
 */
public class BootstrapContextListener implements ServletContextListener
{

  /** Field description */
  private static final String DIRECTORY_PLUGINS = "plugins";

  /** Field description */
  private static final String PLUGIN_DIRECTORY = "/WEB-INF/plugins/";

  /**
   * the logger for BootstrapContextListener
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BootstrapContextListener.class);

  /** Field description */
  private static final String PLUGIN_COREINDEX =
    PLUGIN_DIRECTORY.concat("plugin.idx");

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param sce
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {
    contextListener.contextDestroyed(sce);

    for (PluginWrapper plugin : contextListener.getPlugins())
    {
      ClassLoader pcl = plugin.getClassLoader();

      if (pcl instanceof Closeable)
      {
        try
        {
          ((Closeable) pcl).close();
        }
        catch (IOException ex)
        {
          logger.warn("could not close plugin classloader", ex);
        }
      }
    }

    contextListener = null;
  }

  /**
   * Method description
   *
   *
   * @param sce
   */
  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    ServletContext context = sce.getServletContext();
    List<String> lines = readCorePluginIndex(context);

    File pluginDirectory = getPluginDirectory();

    copyCorePlugins(context, pluginDirectory, lines);

    ClassLoader cl =
      ClassLoaders.getContextClassLoader(BootstrapContextListener.class);

    try
    {
      Set<PluginWrapper> plugins = Plugins.collectPlugins(cl,
                                     pluginDirectory.toPath());

      contextListener = new ScmContextListener(cl, plugins);
    }
    catch (IOException ex)
    {
      throw new PluginLoadException("could not load plugins", ex);
    }

    contextListener.contextInitialized(sce);
  }

  /**
   * Method description
   *
   *
   * @param context
   * @param pluginDirectory
   * @param name
   *
   * @throws IOException
   */
  private void copyCorePlugin(ServletContext context, File pluginDirectory,
    String name)
    throws IOException
  {
    URL url = context.getResource(PLUGIN_DIRECTORY.concat(name));
    File file = new File(pluginDirectory, name);

    try (OutputStream output = new FileOutputStream(file))
    {
      Resources.copy(url, output);
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   * @param pluginDirectory
   * @param lines
   */
  private void copyCorePlugins(ServletContext context, File pluginDirectory,
    List<String> lines)
  {
    IOUtil.mkdirs(pluginDirectory);

    for (String line : lines)
    {
      line = line.trim();

      if (!Strings.isNullOrEmpty(line))
      {
        try
        {
          copyCorePlugin(context, pluginDirectory, line);
        }
        catch (IOException ex)
        {
          logger.error("could not copy core plugin", ex);
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  private List<String> readCorePluginIndex(ServletContext context)
  {
    List<String> lines;

    try
    {
      URL index = context.getResource(PLUGIN_COREINDEX);

      if (index == null)
      {
        throw new PluginException("no core plugin index found");
      }

      lines = Resources.readLines(index, Charsets.UTF_8);
    }
    catch (IOException ex)
    {
      throw new PluginException("could not load core plugin index", ex);
    }

    return lines;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private File getPluginDirectory()
  {
    File baseDirectory = SCMContext.getContext().getBaseDirectory();

    return new File(baseDirectory, DIRECTORY_PLUGINS);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmContextListener contextListener;
}
