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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.SCMContextProvider;
import sonia.scm.event.ScmEventBus;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;


/**
 *
 * @author Sebastian Sdorra
 *
 * @param <C>
 */
public abstract class AbstractRepositoryHandler<C extends SimpleRepositoryConfig>
  implements RepositoryHandler
{

  /** the logger for AbstractRepositoryHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractRepositoryHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param storeFactory
   */
  protected AbstractRepositoryHandler(ConfigurationStoreFactory storeFactory)
  {
    this.store = storeFactory.getStore(getConfigClass(), getType().getName());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract Class<C> getConfigClass();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    baseDirectory = context.getBaseDirectory();
    loadConfig();
  }

  /**
   * Method description
   *
   */
  public void loadConfig()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("load config from store");
    }

    config = store.get();
  }

  /**
   * Method description
   *
   */
  public void storeConfig()
  {
    if (config != null)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("store config");
      }

      store.set(config);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public C getConfig()
  {
    return config;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws NotSupportedFeatuerException
   */
  @Override
  public ImportHandler getImportHandler() throws NotSupportedFeatuerException
  {
    throw new NotSupportedFeatuerException(
      "import handler is not supported by this repository handler");
  }

  /**
   * Returns true if the plugin is configured and enabled.
   *
   *
   * @return true if the plugin is configured and enabled
   */
  @Override
  public boolean isConfigured()
  {
    return (config != null) && config.isValid() &&!config.isDisabled();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param config
   */
  public void setConfig(C config)
  {
    this.config = config;
    fireConfigChanged();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void fireConfigChanged()
  {
    ScmEventBus.getInstance().post(
      new RepositoryHandlerConfigChangedEvent<C>(config));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected File baseDirectory;

  /** Field description */
  protected C config;

  /** Field description */
  protected ConfigurationStore<C> store;
}
