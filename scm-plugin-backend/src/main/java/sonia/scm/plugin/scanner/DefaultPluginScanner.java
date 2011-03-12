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



package sonia.scm.plugin.scanner;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginBackend;
import sonia.scm.plugin.PluginException;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;

import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultPluginScanner implements PluginScanner
{

  /** Field description */
  public static final String PLUGIN_DESCRIPTOR = "META-INF/scm/plugin.xml";

  /** Field description */
  public static final String PLUGIN_EXTENSION = ".jar";

  /** the logger for DefaultPluginScanner */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginScanner.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public DefaultPluginScanner()
  {
    try
    {
      pluginContext = JAXBContext.newInstance(Plugin.class);
    }
    catch (JAXBException ex)
    {
      throw new PluginException(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param backend
   * @param directory
   */
  @Override
  public void scannDirectory(PluginBackend backend, File directory)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("scann directory {}", directory.getPath());
    }

    File[] files = directory.listFiles();

    for (File file : files)
    {
      if (file.isDirectory())
      {
        scannDirectory(backend, file);
      }
      else if (file.getName().endsWith(PLUGIN_EXTENSION))
      {
        scannFile(backend, file);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param backend
   * @param file
   */
  private void scannFile(PluginBackend backend, File file)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("scann file {}", file.getPath());
    }

    JarInputStream inputStream = null;

    try
    {
      inputStream = new JarInputStream(new FileInputStream(file));

      JarEntry entry = inputStream.getNextJarEntry();

      while (entry != null)
      {
        if (logger.isTraceEnabled())
        {
          logger.trace("scann entry {}", entry.getName());
        }

        if (PLUGIN_DESCRIPTOR.equals(entry.getName()))
        {
          Plugin plugin =
            (Plugin) pluginContext.createUnmarshaller().unmarshal(inputStream);

          if (plugin != null)
          {
            if (logger.isInfoEnabled())
            {
              logger.info("add plugin {} to backend", file.getPath());
            }

            backend.addPlugin(plugin.getInformation());
          }

          break;
        }
        else
        {
          inputStream.closeEntry();
          entry = inputStream.getNextJarEntry();
        }
      }
    }
    catch (Exception ex)
    {
      logger.error("could not read plugin descriptor ".concat(file.getPath()),
                   ex);
    }
    finally
    {
      IOUtil.close(inputStream);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext pluginContext;
}
