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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ext.ExtensionObject;
import sonia.scm.plugin.ext.ExtensionProcessor;
import sonia.scm.plugin.ext.JARExtensionScanner;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultPluginManager implements PluginManager
{

  /** Field description */
  public static final String DEFAULT_PACKAGE = "sonia.scm";

  /** Field description */
  public static final String PATH_PLUGINCONFIG = "META-INF/scm/plugin.xml";

  /** the logger for DefaultPluginManager */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public DefaultPluginManager()
  {
    ClassLoader classLoader = getClassLoader();

    try
    {
      load(classLoader);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param processor
   */
  @Override
  public void processExtensions(ExtensionProcessor processor)
  {
    Set<ExtensionObject> extensions = new HashSet<ExtensionObject>();
    ClassLoader classLoader = getClassLoader();
    JARExtensionScanner scanner = new JARExtensionScanner();

    for (Plugin plugin : plugins)
    {
      InputStream input = null;

      try
      {
        Set<String> packageSet = plugin.getPackageSet();

        if (packageSet == null)
        {
          packageSet = new HashSet<String>();
        }

        packageSet.add(DEFAULT_PACKAGE);
        input = new FileInputStream(plugin.getPath());
        scanner.processExtensions(classLoader, extensions, input, packageSet);
      }
      catch (IOException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
      finally
      {
        IOUtil.close(input);
      }
    }

    for (ExtensionObject exo : extensions)
    {
      processor.processExtension(exo.getExtension(), exo.getExtensionClass());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Plugin> getPlugins()
  {
    return plugins;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param classLoader
   *
   * @throws IOException
   */
  private void load(ClassLoader classLoader) throws IOException
  {
    Enumeration<URL> urlEnum = classLoader.getResources(PATH_PLUGINCONFIG);

    if (urlEnum != null)
    {
      while (urlEnum.hasMoreElements())
      {
        URL url = urlEnum.nextElement();

        loadPlugin(url);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param url
   */
  private void loadPlugin(URL url)
  {
    try
    {

      // jar:file:/some/path/file.jar!/META-INF/scm/plugin.xml
      String path = url.toExternalForm();

      path = path.substring("jar:file:".length(), path.lastIndexOf("!"));

      if (logger.isInfoEnabled())
      {
        logger.info("load plugin {}", path);
      }

      Plugin plugin = JAXB.unmarshal(url, Plugin.class);

      plugin.setPath(path);
      plugins.add(plugin);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage(), ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private ClassLoader getClassLoader()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      classLoader = DefaultPluginManager.class.getClassLoader();
    }

    return classLoader;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<Plugin> plugins = new HashSet<Plugin>();
}
