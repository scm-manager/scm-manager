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
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultPluginBackend implements PluginBackend
{

  /** Field description */
  public static final String FILE_STORE = "store.xml";

  /** Field description */
  private static final Object LOCK_OBJECT = new Object();

  /** the logger for DefaultPluginBackend */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginBackend.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param baseDirectory
   */
  @Inject
  public DefaultPluginBackend(
          @Named(ScmBackendModule.DIRECTORY_PROPERTY) File baseDirectory)
  {
    this.baseDirectory = baseDirectory;
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
    }
    catch (JAXBException ex)
    {
      throw new ConfigurationException(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param plugin
   */
  @Override
  public void addPlugin(PluginInformation plugin)
  {
    if (!pluginStore.contains(plugin))
    {
      synchronized (LOCK_OBJECT)
      {
        pluginStore.add(plugin);
        store();
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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void store()
  {
    try
    {
      storeContext.createMarshaller().marshal(pluginStore, storeFile);
    }
    catch (JAXBException ex)
    {
      logger.error("could not store pluginStore", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  private PluginBackendStore pluginStore;

  /** Field description */
  private JAXBContext storeContext;

  /** Field description */
  private File storeFile;
}
