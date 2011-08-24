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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultPluginBackend extends AbstractPluginBackend
{

  /** Field description */
  public static final String FILE_SCANNED = "scanned-files";

  /** Field description */
  public static final String FILE_STORE = "store.xml";

  /** Field description */
  private static final Object LOCK_PLUGINS = new Object();

  /** the logger for DefaultPluginBackend */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginBackend.class);

  /** Field description */
  private static final Object LOCK_SCANNEDFILE = new Object();

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param baseDirectory
   * @param configuration
   */
  @Inject
  public DefaultPluginBackend(
          @Named(ScmBackendModule.DIRECTORY_PROPERTY) File baseDirectory,
          BackendConfiguration configuration)
  {
    this.baseDirectory = baseDirectory;
    this.configuration = configuration;
    this.storeFile = new File(baseDirectory, FILE_STORE);

    try
    {
      storeContext = JAXBContext.newInstance(PluginBackendStore.class);

      if (storeFile.exists())
      {
        pluginStore =
          (PluginBackendStore) storeContext.createUnmarshaller().unmarshal(
            storeFile);
      }
      else
      {
        pluginStore = new PluginBackendStore();
      }

      readScannedFiles();
    }
    catch (JAXBException ex)
    {
      throw new ConfigurationException("could not read store", ex);
    }
    catch (IOException ex)
    {
      throw new ConfigurationException("could not read scanned files", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param plugins
   */
  @Override
  public void addPlugins(Collection<PluginInformation> plugins)
  {
    synchronized (LOCK_PLUGINS)
    {
      boolean changed = false;

      for (PluginInformation plugin : plugins)
      {
        if (!pluginStore.contains(plugin))
        {
          pluginStore.add(plugin);
          changed = true;
        }
      }

      if (changed)
      {
        fireEvent(plugins);
        store();
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param scannedFiles
   */
  @Override
  public void addScannedFiles(Collection<File> scannedFiles)
  {
    synchronized (LOCK_SCANNEDFILE)
    {
      boolean changed = false;

      for (File file : scannedFiles)
      {
        if (!this.scannedFiles.contains(file))
        {
          changed = true;
          this.scannedFiles.add(file);
        }
      }

      if (changed)
      {
        writeScannedFiles();
      }
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
  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<String> getExcludes()
  {
    return configuration.getExcludes();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<PluginInformation> getPlugins()
  {
    return pluginStore.getPlugins();
  }

  /**
   * Method description
   *
   *
   * @param filter
   *
   * @return
   */
  @Override
  public List<PluginInformation> getPlugins(PluginFilter filter)
  {
    List<PluginInformation> filteredPlugins =
      new ArrayList<PluginInformation>();

    for (PluginInformation plugin : pluginStore)
    {
      if (filter.accept(plugin))
      {
        filteredPlugins.add(plugin);
      }
    }

    return filteredPlugins;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<File> getScannedFiles()
  {
    return scannedFiles;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @throws IOException
   */
  private void readScannedFiles() throws IOException
  {
    File file = new File(baseDirectory, FILE_SCANNED);

    if (file.exists())
    {
      BufferedReader reader = null;

      try
      {
        reader = new BufferedReader(new FileReader(file));

        String line = reader.readLine();

        while (line != null)
        {
          if (Util.isNotEmpty(line))
          {
            File jar = new File(line);

            if (jar.exists())
            {
              scannedFiles.add(jar);
            }
          }

          line = reader.readLine();
        }
      }
      finally
      {
        IOUtil.close(reader);
      }
    }
  }

  /**
   * Method description
   *
   */
  private void store()
  {
    if (logger.isInfoEnabled())
    {
      logger.info("store plugins");
    }

    try
    {
      storeContext.createMarshaller().marshal(pluginStore, storeFile);
    }
    catch (JAXBException ex)
    {
      logger.error("could not store pluginStore", ex);
    }
  }

  /**
   * Method description
   *
   */
  private void writeScannedFiles()
  {
    if (logger.isInfoEnabled())
    {
      logger.info("store scanned files");
    }

    File file = new File(baseDirectory, FILE_SCANNED);
    PrintWriter writer = null;

    try
    {
      writer = new PrintWriter(file);

      for (File f : scannedFiles)
      {
        writer.println(f.getAbsolutePath());
      }
    }
    catch (IOException ex)
    {
      logger.error("could not write scanned files", ex);
    }
    finally
    {
      IOUtil.close(writer);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  private BackendConfiguration configuration;

  /** Field description */
  private PluginBackendStore pluginStore;

  /** Field description */
  private Set<File> scannedFiles = new HashSet<File>();

  /** Field description */
  private JAXBContext storeContext;

  /** Field description */
  private File storeFile;
}
