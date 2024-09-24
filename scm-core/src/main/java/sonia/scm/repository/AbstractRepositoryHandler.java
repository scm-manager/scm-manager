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

package sonia.scm.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.FeatureNotSupportedException;
import sonia.scm.SCMContextProvider;
import sonia.scm.event.ScmEventBus;

import java.io.File;
import java.io.IOException;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

public abstract class AbstractRepositoryHandler<C extends RepositoryConfig>
  implements RepositoryHandler
{

  
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractRepositoryHandler.class);

  protected File baseDirectory;

  protected C config;

  protected ConfigurationStore<C> store;
 
  protected AbstractRepositoryHandler(ConfigurationStoreFactory storeFactory) {
    this.store = storeFactory
      .withType(getConfigClass())
      .withName(getType().getName())
      .build();
  }


  
  protected abstract Class<C> getConfigClass();


  @Override
  public void close() throws IOException
  {

    // do nothing
  }


  @Override
  public void init(SCMContextProvider context)
  {
    baseDirectory = context.getBaseDirectory();
    loadConfig();
  }

   public void loadConfig()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("load config from store");
    }

    config = store.get();
  }

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


  
  public C getConfig()
  {
    return config;
  }


  @Override
  public ImportHandler getImportHandler()
  {
    throw new FeatureNotSupportedException("import");
  }

  /**
   * Returns true if the plugin is configured and enabled.
   */
  @Override
  public boolean isConfigured()
  {
    return (config != null) && config.isValid() &&!config.isDisabled();
  }



  public void setConfig(C config)
  {
    this.config = config;
    fireConfigChanged();
  }


   private void fireConfigChanged()
  {
    ScmEventBus.getInstance().post(
      new RepositoryHandlerConfigChangedEvent<C>(config));
  }

}
